package net.peacesoft;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class ParamConfig {

    public static long timeLoad = 5; //Tinh theo phut
    public static long timeCheck = 500; //Minisecond
    public static String localURL = "";
    public static String localUser = "";
    public static String localPassword = "";

    public static boolean loadProperties(String fileName) {
        Properties properties = new Properties();
        try {
            FileInputStream propsFile = new FileInputStream(fileName);
            if (propsFile == null) {
                return false;
            }
            properties.load(propsFile);
            propsFile.close();

            timeLoad = Long.parseLong(properties.getProperty("time-load", "5"));
            timeCheck = Long.parseLong(properties.getProperty("time-check", "500"));

            localURL = properties.getProperty("local-url", "jdbc:mysql://localhost:3306/product_crawler");
            localUser = properties.getProperty("local-user", "root");
            localPassword = properties.getProperty("local-password", "rootroot");

        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
