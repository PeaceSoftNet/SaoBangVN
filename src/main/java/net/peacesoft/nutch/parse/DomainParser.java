package net.peacesoft.nutch.parse;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.Outlink;

/**
 *
 * @author Tran Anh tuan <tuanta2@peacesoft.net>
 */
public interface DomainParser {

    public void parse(String url) throws Exception;

    public String getDomain();

    public String getTitle();

    public String getContent();

    public String[] getCategory();

    public String getLocation();

    public String getMobile();

    public String getAddress();

    public String getThumb();

    public long getPrice();
    
    public String getEmail();
    
    public Outlink[] getOutlinks();

    public void setConf(Configuration conf);

    public Configuration getConf();
}
