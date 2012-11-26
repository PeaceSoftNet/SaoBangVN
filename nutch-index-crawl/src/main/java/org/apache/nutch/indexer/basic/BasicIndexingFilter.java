package org.apache.nutch.indexer.basic;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicIndexingFilter
        implements IndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(BasicIndexingFilter.class);
    private int MAX_TITLE_LENGTH;
    private int MAX_CONTENT_LENGTH;
    private boolean addDomain = false;
    private Configuration conf;

    public NutchDocument filter(NutchDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks)
            throws IndexingException {
        Text reprUrl = (Text) datum.getMetaData().get(Nutch.WRITABLE_REPR_URL_KEY);
        String reprUrlString = reprUrl != null ? reprUrl.toString() : null;
        String urlString = url.toString();

        String host = null;
        try {
            URL u;
            if (reprUrlString != null) {
                u = new URL(reprUrlString);
            } else {
                u = new URL(urlString);
            }

            if (this.addDomain) {
                doc.add("domain", URLUtil.getDomainName(u));
            }

            host = u.getHost();
        } catch (MalformedURLException e) {
            throw new IndexingException(e);
        }

        if (host != null) {
            doc.add("host", host);
        }

        doc.add("url", reprUrlString == null ? urlString : reprUrlString);

        String content = parse.getText();
        if ((this.MAX_CONTENT_LENGTH > -1) && (content.length() > this.MAX_CONTENT_LENGTH)) {
            content = content.substring(0, this.MAX_CONTENT_LENGTH);
        }
        doc.add("content", content);

        String title = parse.getData().getTitle();
        if (title.length() > this.MAX_TITLE_LENGTH) {
            title = title.substring(0, this.MAX_TITLE_LENGTH);
        }

        if (title.length() > 0) {
            doc.add("title", title);
        }

        String caching = parse.getData().getMeta("caching.forbidden");
        if ((caching != null) && (!caching.equals("none"))) {
            doc.add("cache", caching);
        }

        doc.add("tstamp", new Date(datum.getFetchTime()));

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String myid = formatter.format(new Date(datum.getFetchTime()));
        doc.add("created", myid);

        formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        myid = formatter.format(new Date());
        int randomNum = 10000 + (int) (Math.random() * 89999.0D);

        doc.add("myid", myid + randomNum);

        String mobile = parse.getData().getMeta("mobile");
        if ((mobile != null) && (!mobile.isEmpty())) {
            doc.add("mobile", mobile);
        }

        String address = parse.getData().getMeta("address");
        if ((address != null) && (!address.isEmpty())) {
            doc.add("address", address);
        }

        String location = parse.getData().getMeta("location");
        if ((location != null) && (!location.isEmpty())) {
            doc.add("location", location);
        }

        String categoryId = parse.getData().getMeta("categoryId");
        if ((categoryId != null) && (!categoryId.isEmpty())) {
            doc.add("categoryId", categoryId);
        }

        String categoryChildId = parse.getData().getMeta("categoryChildId");
        if ((categoryChildId != null) && (!categoryChildId.isEmpty())) {
            doc.add("categoryChildId", categoryChildId);
        }

        String thumb = parse.getData().getMeta("thumbImage");
        if ((thumb != null) && (!thumb.isEmpty())) {
            doc.add("thumbImage", thumb);
        }

        String price = parse.getData().getMeta("price");
        if ((price != null) && (!price.isEmpty())) {
            doc.add("price", price);
        }

        String email = parse.getData().getMeta("email");
        if ((email != null) && (!email.isEmpty())) {
            doc.add("email", email);
        }
        return doc;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        this.MAX_TITLE_LENGTH = conf.getInt("indexer.max.title.length", 100);
        this.addDomain = conf.getBoolean("indexer.add.domain", false);
        this.MAX_CONTENT_LENGTH = conf.getInt("indexer.max.content.length", -1);
    }

    public Configuration getConf() {
        return this.conf;
    }
}