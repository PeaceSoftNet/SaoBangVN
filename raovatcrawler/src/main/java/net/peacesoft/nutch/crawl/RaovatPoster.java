package net.peacesoft.nutch.crawl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchIndexWriter;
import org.apache.nutch.indexer.solr.SolrMappingReader;
import org.apache.nutch.indexer.solr.SolrUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaovatPoster
        implements NutchIndexWriter {

    public static final Logger LOG = LoggerFactory.getLogger(RaovatPoster.class);
    private SolrServer solr;
    private SolrMappingReader solrMapping;
    private ModifiableSolrParams params;
    private final List<SolrInputDocument> inputDocs = new ArrayList();
    private int commitSize;
    private int numDeletes = 0;
    private boolean delete = false;

    public void open(JobConf job, String name) throws IOException {
        SolrServer server = SolrUtils.getCommonsHttpSolrServer(job);
        init(server, job);
    }

    void init(SolrServer server, JobConf job) throws IOException {
        this.solr = server;
        this.commitSize = job.getInt("solr.commit.size", 1000);
        this.solrMapping = SolrMappingReader.getInstance(job);
        this.delete = job.getBoolean("indexer.delete", false);

        this.params = new ModifiableSolrParams();
        String paramString = job.get("solr.params");
        if (paramString != null) {
            String[] values = paramString.split("&");
            for (String v : values) {
                String[] kv = v.split("=");
                if (kv.length >= 2) {
                    this.params.add(kv[0], new String[]{kv[1]});
                }
            }
        }
    }

    public void delete(String key) throws IOException {
        if (this.delete) {
            try {
                this.solr.deleteById(key);
                this.numDeletes += 1;
            } catch (SolrServerException e) {
                throw makeIOException(e);
            }
        }
    }

    public void write(NutchDocument doc)
            throws IOException {
        postHttp("http://map.saobang.vn", doc);
        LOG.info("RaovatPoster: Post request to solr server " + new Timestamp(System.currentTimeMillis()));
    }

    public void close()
            throws IOException {
        LOG.info("RaovatPoster: Updating request to solr server " + new Timestamp(System.currentTimeMillis()));
        try {
            if (!this.inputDocs.isEmpty()) {
                LOG.info("RaovatPoster: Indexing " + Integer.toString(this.inputDocs.size()) + " documents");
                if (this.numDeletes > 0) {
                    LOG.info("RaovatPoster: Deleting " + Integer.toString(this.numDeletes) + " documents");
                }
                UpdateRequest req = new UpdateRequest();
                req.add(this.inputDocs);
                req.setParams(this.params);
                req.process(this.solr);
                this.inputDocs.clear();
            }
        } catch (SolrServerException e) {
            throw makeIOException(e);
        }
    }

    public void postHttp(String httpServer, NutchDocument doc) {
        try {
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(httpServer);
            Object tmp = doc.getFieldValue("myid");
            String myid = tmp.toString().trim();
            if (LOG.isInfoEnabled()) {
                LOG.info("RaovatPoster: Post content id " + tmp.toString().trim() + " to http server: " + httpServer);
            }
            if (tmp != null) {
                method.addParameter("ContentModel[id]", tmp.toString().trim());
            }
            tmp = doc.getFieldValue("title");
            if (tmp != null) {
                method.addParameter("ContentModel[title]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("categoryId");
            if (tmp != null) {
                method.addParameter("ContentModel[category]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("categoryChildId");
            if (tmp != null) {
                method.addParameter("ContentModel[childCategory]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("content");
            if (tmp != null) {
                method.addParameter("ContentDetail[content]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }

            tmp = doc.getFieldValue("location");
            if (tmp != null) {
                method.addParameter("ContentModel[local]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("thumbImage");
            if (tmp != null) {
                method.addParameter("ContentModel[thumb]", tmp.toString().trim());
            }

            tmp = doc.getFieldValue("domain");
            if (tmp != null) {
                method.addParameter("ContentModel[domain]", tmp.toString().trim());
            }
            tmp = doc.getFieldValue("url");
            if (tmp != null) {
                method.addParameter("ContentModel[url]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("mobile");
            if (tmp != null) {
                method.addParameter("ContentModel[mobile]", tmp.toString().trim());
            }
            tmp = doc.getFieldValue("address");
            if (tmp != null) {
                method.addParameter("ContentModel[address]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("price");
            if (tmp != null) {
                method.addParameter("ContentModel[price]", tmp.toString());
            }
            tmp = doc.getFieldValue("email");
            if (tmp != null) {
                method.addParameter("ContentModel[email]", tmp.toString());
            }

            int returnCode = client.executeMethod(method);
            byte[] data = method.getResponseBody();
            if (returnCode != 200) {
                toFile("logs-post/" + myid + "-error.html", data);
                toFile("logs-post/" + myid + "-post-error.html", Arrays.toString(method.getParameters()).getBytes());
            }
        } catch (Exception ex) {
            LOG.warn("Error when post data to server: " + httpServer, ex);
        }
    }

    public static void toFile(String fileName, byte[] data) {
        FileOutputStream fos = null;
        try {
            File f = new File(fileName);
            fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException ex) {
        } catch (Exception ex) {
            System.out.println("Loi khi xuat ra file: " + fileName);
        } finally {
            try {
                fos.close();
            } catch (Exception ex) {
            }
        }
    }

    public static IOException makeIOException(SolrServerException e) {
        IOException ioe = new IOException();
        ioe.initCause(e);
        return ioe;
    }
}