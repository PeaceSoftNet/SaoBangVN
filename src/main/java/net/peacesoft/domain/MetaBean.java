package net.peacesoft.domain;

import java.io.Serializable;

/**
 * 
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class MetaBean implements Serializable {

    public long id;
    public long remoteid;
    public long domainId;
    public String title;
    public String des;
    public String image;
    public String time;
    public String sourceTime;
    public String url;
    public boolean status;
}
