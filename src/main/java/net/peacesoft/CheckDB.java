package net.peacesoft;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.peacesoft.commons.DateUtils;
import net.peacesoft.commons.Queue;
import net.peacesoft.domain.ContentBean;
import net.peacesoft.domain.MetaBean;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class CheckDB extends Thread {

    private Queue linkQueue;

    public CheckDB(Queue linkQueue) {
        this.linkQueue = linkQueue;
    }

    @Override
    public void run() {
        while (SynchronizeDatabase.isRunning) {
            MetaBean metaLocal = (MetaBean) linkQueue.remove();
            try {
                SynchronizeDatabase.checkDB();
                //System.out.println("Link check: " + bean.url);
                //Kiem tra link tren remote db
                List<MetaBean> metaBeans = SynchronizeDatabase.metaRemoteRepository.getByURLAndDomain(metaLocal.url, metaLocal.domainId);
                //System.out.println("Get content by meta id: " + bean.id);
                //Lay du lieu o local db
                ContentBean content = SynchronizeDatabase.contentLocalRepository.getById(metaLocal.id);
                if (metaBeans.isEmpty()) {
                    //Neu chua co thi them du lieu vao remote db
                    try {
                        SynchronizeDatabase.metaRemoteRepository.save(metaLocal);
                        content.id = metaLocal.remoteid;
                        SynchronizeDatabase.contentRemoteRepository.save(content);
                        System.out.println(DateUtils.Date2Format(new Date(), "yyyyMMdd HH:mm:ss") + ":\tInsert du lieu thanh cong: " + metaLocal.url);
                        SynchronizeDatabase.totalInsert++;
                    } catch (Exception ex) {
                        System.out.println(DateUtils.Date2Format(new Date(), "yyyyMMdd HH:mm:ss") + ":\tInsert du lieu trung khoa: " + metaLocal.url + ". Chi tiet loi: " + ex.toString());
//                        SynchronizeDatabase.monitor.error("Application", "Synchronize Database", "Error save data to remote db with meta id:" + bean.id + ". " + ex);
                        metaLocal.remoteid++;
                        linkQueue.add(metaLocal);
                    }
                } else {
                    //Neu co thi cap nhap du lieu remote db
                    for (MetaBean metaRemote : metaBeans) {
                        //Neu da ton tai link kia boc roi
                        //1.Tro link da boc ve luong moi de tien lam luon vi moi ng dang lam truc tiep voi luong du lieu do
                        //P/S:Viec nay co the bi trung du lieu
//                        SynchronizeDatabase.metaRemoteRepository.updateByIDAndDomain(metaRemote.id, metaLocal.domainId);
                        //2.Cap nhat du lieu moi crawl ve
                        SynchronizeDatabase.metaRemoteRepository.updateByIDAndTitleAndDescription(metaRemote.id, metaLocal.title, metaLocal.des);
                        //3.Cap nhat lai du lieu cho link do ben phan content
                        SynchronizeDatabase.contentRemoteRepository.update(content.content, metaRemote.id);
                        System.out.println(DateUtils.Date2Format(new Date(), "yyyyMMdd HH:mm:ss") + ":\tUpdate du lieu url: " + metaLocal.url);
                        SynchronizeDatabase.totalUpdate++;
                    }
                }
            } catch (Exception ex) {
                linkQueue.add(metaLocal);
                SynchronizeDatabase.monitor.error("Application", "Synchronize Database", ex);
            }
            if (linkQueue.isEmpty()) {
                System.out.println("Tong so du lieu duoc load len la: " + SynchronizeDatabase.totalRecord);
                System.out.println("Tong so insert " + SynchronizeDatabase.totalInsert + "/" + SynchronizeDatabase.totalRecord);
                System.out.println("Tong so update " + SynchronizeDatabase.totalUpdate + "/" + SynchronizeDatabase.totalRecord);
                try {
                    SynchronizeDatabase.localDataSource.close();
                } catch (SQLException ex) {
                }
                SynchronizeDatabase.localDataSource = null;
                try {
                    SynchronizeDatabase.remoteDataSource.close();
                } catch (SQLException ex) {
                }
                SynchronizeDatabase.remoteDataSource = null;
            }
            waitMinisecond(ParamConfig.timeCheck);
        }
    }

    private void waitMinisecond(long minisecond) {
        try {
            sleep(minisecond);
        } catch (InterruptedException ex) {
        }
    }
}
