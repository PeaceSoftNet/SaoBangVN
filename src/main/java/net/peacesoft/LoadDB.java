package net.peacesoft;

import java.sql.SQLException;
import java.util.List;
import net.peacesoft.commons.Queue;
import net.peacesoft.domain.MetaBean;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class LoadDB extends Thread {

    private Queue linkQueue;

    public LoadDB(Queue linkQueue) {
        this.linkQueue = linkQueue;
    }

    @Override
    public void run() {
        while (SynchronizeDatabase.isRunning) {
            try {
                SynchronizeDatabase.checkDB();
                SynchronizeDatabase.updateDomain();
                List<MetaBean> metaBeans = SynchronizeDatabase.metaLocalRepository.getByStatus(false);
                SynchronizeDatabase.totalRecord = metaBeans.size();
                SynchronizeDatabase.totalInsert = 0;
                SynchronizeDatabase.totalUpdate = 0;
                System.out.println("Tong so du lieu duoc load len la:" + SynchronizeDatabase.totalRecord);
                for (MetaBean metaBean : metaBeans) {
                    linkQueue.add(metaBean);
                    SynchronizeDatabase.metaLocalRepository.updateByStatus(metaBean.id);
                }
            } catch (Exception ex) {
                SynchronizeDatabase.monitor.error(ex.toString(), ex);
            }
            if (linkQueue.isEmpty()) {
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
            waitMinute(ParamConfig.timeLoad);
        }
    }

    private void waitMinute(long minute) {
        try {
            sleep(minute * 60 * 1000);
        } catch (InterruptedException ex) {
        }
    }
}