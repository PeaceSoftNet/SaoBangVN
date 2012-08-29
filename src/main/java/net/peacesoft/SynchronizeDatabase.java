package net.peacesoft;

import java.io.*;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.List;
import net.peacesoft.commons.Queue;
import net.peacesoft.domain.DomainBean;
import net.peacesoft.domain.MetaBean;
import net.peacesoft.repositories.ContentRepository;
import net.peacesoft.repositories.DomainRepository;
import net.peacesoft.repositories.MetaRepository;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Group R & D
 *
 * File main thuc hien dong bo hoa du lieu.
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class SynchronizeDatabase extends Thread {

    public static final Logger monitor = LoggerFactory.getLogger(SynchronizeDatabase.class);
    private Queue link_queue = null;
    public static boolean isRunning = true;
    private static BufferedReader keyboard =
            new BufferedReader(new InputStreamReader(System.in));
    private static AbstractApplicationContext context;
    public static MetaRepository metaLocalRepository;
    public static DomainRepository domainLocalRepository;
    public static ContentRepository contentLocalRepository;
    public static MetaRepository metaRemoteRepository;
    public static DomainRepository domainRemoteRepository;
    public static ContentRepository contentRemoteRepository;
    public static BasicDataSource localDataSource;
    public static BasicDataSource remoteDataSource;
    public static int totalRecord = 0;
    public static int totalInsert = 0;
    public static int totalUpdate = 0;

    public SynchronizeDatabase() {

        checkDB(); //Kiem tra connection

        link_queue = new Queue();

        if (!ParamConfig.loadProperties("param.cfg")) {
            monitor.error("Load config from param.cfg error.");
            System.exit(0);
        }
        if (!loadSpringConfig()) {
            monitor.error("Load config Spring Framework error.");
            System.exit(0);
        }
    }

    public static void checkDB() {
        if ((localDataSource == null || (localDataSource != null && localDataSource.isClosed())) && (remoteDataSource == null || (remoteDataSource != null && remoteDataSource.isClosed()))) {
            if (!loadSpringConfig()) {
                System.out.println("Reload config Spring Framework error.");
            }
        }
    }

    public static void updateDomain() throws Exception {
        //So sanh domain giua local va remote db
        //Lay du lieu tu local
        List<DomainBean> localDomains = domainLocalRepository.getAll();
        for (DomainBean localBean : localDomains) {
            System.out.println("Dang check luong:" + localBean.name);
            long localDomainId = localBean.id;
            List<DomainBean> remoteDomains = domainRemoteRepository.getByName(localBean.name);
            if (remoteDomains.isEmpty()) {
                //Khong giong nhau thi them vao phia remote.
                try {
                    domainRemoteRepository.save(localBean);
                } catch (Exception ex) {
                    monitor.error("Save domain: " + ex);
                    while (true) {
                        localBean.id++;
                        try {
                            domainRemoteRepository.save(localBean);
//                            metaLocalRepository.updateByDomain(localDomainId, 10);
                            domainLocalRepository.updateById(localBean.id, localDomainId);
//                            localBean.id = localDomainId;
//                            domainLocalRepository.save(localBean);
                            metaLocalRepository.updateByDomain(localBean.id, localDomainId);
                            break;
                        } catch (Exception exception) {
                            monitor.error("Save 2 domain: " + exception);
                        }
                    }
                }
            } else {
                //Neu giong nhau thi update phia local giong remote
//                try {
//                    //Save vi cai lay tu tren kia ve co the da ton tai
//                    domainRemoteRepository.save(localBean);
//                } catch (Exception ex) {
//                }
                try {
                    domainLocalRepository.updateById(localBean.id, remoteDomains.get(0).id);
                    metaLocalRepository.updateByDomain(localBean.id, remoteDomains.get(0).id);
                } catch (Exception ex) {
                    monitor.error("Update domain: " + ex);
                }
            }
            domainLocalRepository.updateStatusById(localDomainId);
        }
    }

    private static boolean loadSpringConfig() {
        try {
            if (context != null) {
                context.close();
                context = null;
            }
            context = new ClassPathXmlApplicationContext("classpath:META-INF/spring/synchronize-database.xml");
            context.registerShutdownHook();
            localDataSource = (BasicDataSource) context.getBean("localConnection");
            remoteDataSource = (BasicDataSource) context.getBean("remoteConnection");

            metaLocalRepository = (MetaRepository) context.getBean("metaLocalConnection");
            metaRemoteRepository = (MetaRepository) context.getBean("metaRemoteConnection");

            domainLocalRepository = (DomainRepository) context.getBean("domainLocalConnection");
            domainRemoteRepository = (DomainRepository) context.getBean("domainRemoteConnection");

            contentLocalRepository = (ContentRepository) context.getBean("contentLocalConnection");
            contentRemoteRepository = (ContentRepository) context.getBean("contentRemoteConnection");
            return true;
        } catch (Exception ex) {
            monitor.error(ex.toString(), ex);
        }
        return false;
    }

    private void menu() {
        String option = "";
        while (isRunning) {
            System.out.println();
            System.out.println("-  Q Quit");
            System.out.println("-  R Reload config param.cfg");
            System.out.println("-  S Reload config Spring Framework");
            System.out.print("> ");
            try {
                option = keyboard.readLine();
            } catch (Exception e) {
            }
            if ("Q".equals(option.toUpperCase())) {
                doExit();
            } else if ("S".equals(option.toUpperCase())) {
                if (!loadSpringConfig()) {
                    System.out.println("Reload config Spring Framework error.");
                }
            } else if ("R".equals(option.toUpperCase())) {
                if (!ParamConfig.loadProperties("param.cfg")) {
                    System.out.println("Reload config from param.cfg error.");
                }
            } else {
                System.out.println("Invalid option. Choose between Q to exit.");
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        SynchronizeDatabase execute = new SynchronizeDatabase();
        execute.start();
    }

    @Override
    public void run() {
        monitor.info("Synchronize Database start at " + new Timestamp(System.currentTimeMillis()));
        try {
            loadLinkBean("link_queue.dat", link_queue);

//            updateDomain();

            //Bat dau dong bo du lieu tu local len remote
            LoadDB loadDB = new LoadDB(link_queue);
            loadDB.start();
            //Lay du lieu tu local ra de kiem tra
            CheckDB checkDB = new CheckDB(link_queue);
            checkDB.start();
        } catch (Exception ex) {
            monitor.error("Start main error: " + ex.getMessage());
        }
        this.menu();//Hien thi menu
    }

    private void saveLinkBean(String fileName, Queue moqueue) {
        monitor.info("Saving " + moqueue.queueSize() + " msg to " + fileName + "...");
        FileOutputStream fout = null;
        ObjectOutputStream objOut = null;
        try {
            fout = new java.io.FileOutputStream(fileName, false); // append = false
            objOut = new ObjectOutputStream(fout);
            for (Enumeration e = moqueue.getVector().elements(); e.hasMoreElements();) {
                MetaBean object = (MetaBean) e.nextElement();
                objOut.writeObject(object);
                objOut.flush();
            }
            System.out.println("complete !");
        } catch (IOException ex) {
            monitor.error("Error save LinkBean: " + ex.getMessage(), ex);
        } finally {
            try {
                objOut.close();
            } catch (IOException ex) {
            }
        }
    }

    private void loadLinkBean(String fileName, Queue moqueue) {
        monitor.info("Loading data from file " + fileName + "...");
        boolean flag = true;
        try {
            FileInputStream fin = new java.io.FileInputStream(fileName);
            ObjectInputStream objIn = new ObjectInputStream(fin);
            while (flag) {
                try {
                    MetaBean object = (MetaBean) objIn.readObject();
                    moqueue.add(object);
                } catch (Exception ex) {
                    flag = false;
                }
            }
            fin.close();
            System.out.println(" successful!");
        } catch (IOException ex) {
            monitor.error("Error load LinkBean: " + ex.getMessage(), ex);
        }
    }

    private void doExit() {
        isRunning = false;
        monitor.debug("Waiting all threads to die");
        try {
            sleep(5000);
        } catch (InterruptedException ex) {
        }
        monitor.debug("Saving data to file ......");
        saveLinkBean("link_queue.dat", link_queue);
        monitor.error("Synchronize Database stopped at " + new Timestamp(System.currentTimeMillis()));
        System.exit(0);
    }
}
