package net.peacesoft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Tran Anh tuan <tuanta2@peacesoft.net>
 */
public class RaovatStart extends Thread {

    public static boolean isRunning = true;
    private static BufferedReader keyboard =
            new BufferedReader(new InputStreamReader(System.in));
    /**
     * Is the crawling of this session finished?
     */
    protected boolean finished;
    protected final Object waitingLock = new Object();

    private void menu() {
        String option = "";
        while (isRunning) {
            System.out.println();
            System.out.println("Cac tham so khi su dung chuong trinh. Click vao man hinh chon phim can dung");
            System.out.println("-  Q Ket thuc chuong tring");//. Khong duoc tat truc tiep phai dung Q de luu du lieu.");
//            System.out.println("-  R Load lai khi sua file param.cfg. Khong can chay lai chuong trinh.");
//            System.out.println("-  M Theo doi crawl san pham.");
            System.out.println();
            try {
                option = keyboard.readLine();
            } catch (Exception e) {
            }
            if ("Q".equals(option.toUpperCase())) {
                doExit();
            } else {
                System.out.println("Invalid option. Choose between Q to exit.");
            }
        }
    }

    @Override
    public void run() {

        

        //Thread monitor
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (waitingLock) {
                        while (true) {
                            waitMilisecond(1000 * 60);
                            int threadAlive = 0;
//                            for (int i = 0; i < threads.size(); i++) {
//                                Thread thread = threads.get(i);
//                                if (thread.isAlive()) {
//                                    threadAlive++;
//                                }
//                            }
                        }
                    }
                } catch (Exception ex) {
                }

            }
        });
        monitorThread.start();
    }

    private static void waitMilisecond(long minisecond) {
        try {
            Thread.sleep(minisecond);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Wait until this crawling session finishes.
     */
    public void waitUntilFinish() {
        while (!finished) {
            synchronized (waitingLock) {
                if (finished) {
                    return;
                }
                try {
                    waitingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doExit() {
        isRunning = false;
        waitUntilFinish();
        System.exit(0);
    }

    public static void main(String... args) {
        RaovatStart start = new RaovatStart();
        start.start();
    }
}
