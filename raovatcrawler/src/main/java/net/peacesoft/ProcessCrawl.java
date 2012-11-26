/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.peacesoft;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.util.NutchConfiguration;

/**
 *
 * @author Tran Anh tuan <tuanta2@peacesoft.net>
 */
public class ProcessCrawl extends Thread {

    public ProcessCrawl() {
    }

    @Override
    public void run() {
        try {
            Configuration conf = NutchConfiguration.create();
            int res = ToolRunner.run(conf, new net.peacesoft.nutch.crawl.ReCrawl(), null);
            System.exit(res);
        } catch (Exception ex) {
        }
    }
}
