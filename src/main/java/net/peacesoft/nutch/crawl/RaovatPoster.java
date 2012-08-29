/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.peacesoft.nutch.crawl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.indexer.IndexerMapReduce;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchField;
import org.apache.nutch.indexer.NutchIndexWriter;
import org.apache.nutch.indexer.solr.SolrConstants;
import org.apache.nutch.indexer.solr.SolrMappingReader;
import org.apache.nutch.indexer.solr.SolrUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.DateUtil;
import org.mortbay.jetty.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaovatPoster implements NutchIndexWriter {

    public static final Logger LOG = LoggerFactory.getLogger(RaovatPoster.class);
    private SolrServer solr;
    private SolrMappingReader solrMapping;
    private ModifiableSolrParams params;
    private final List<SolrInputDocument> inputDocs =
            new ArrayList<SolrInputDocument>();
    private int commitSize;
    private int numDeletes = 0;
    private boolean delete = false;

    public void open(JobConf job, String name) throws IOException {
        SolrServer server = SolrUtils.getCommonsHttpSolrServer(job);
        init(server, job);
    }

    // package protected for tests
    void init(SolrServer server, JobConf job) throws IOException {
        solr = server;
        commitSize = job.getInt(SolrConstants.COMMIT_SIZE, 1000);
        solrMapping = SolrMappingReader.getInstance(job);
        delete = job.getBoolean(IndexerMapReduce.INDEXER_DELETE, false);
        // parse optional params
        params = new ModifiableSolrParams();
        String paramString = job.get(SolrConstants.PARAMS);
        if (paramString != null) {
            String[] values = paramString.split("&");
            for (String v : values) {
                String[] kv = v.split("=");
                if (kv.length < 2) {
                    continue;
                }
                params.add(kv[0], kv[1]);
            }
        }
    }

    public void delete(String key) throws IOException {
        if (delete) {
            try {
                solr.deleteById(key);
                numDeletes++;
            } catch (final SolrServerException e) {
                throw makeIOException(e);
            }
        }
    }

    public void write(NutchDocument doc) throws IOException {
        final SolrInputDocument inputDoc = new SolrInputDocument();
//        for (final Entry<String, NutchField> e : doc) {
//            for (final Object val : e.getValue().getValues()) {
//                // normalise the string representation for a Date
//                Object val2 = val;
//
//                if (val instanceof Date) {
//                    val2 = DateUtil.getThreadLocalDateFormat().format(val);
//                }
//
//                if (e.getKey().equals("content")) {
//                    val2 = SolrUtils.stripNonCharCodepoints((String) val);
//                }
//
//                inputDoc.addField(solrMapping.mapKey(e.getKey()), val2, e.getValue().getWeight());
//                String sCopy = solrMapping.mapCopyKey(e.getKey());
//                if (sCopy != e.getKey()) {
//                    inputDoc.addField(sCopy, val);
//                }
//            }
//        }

        //postHttp("http://beta2.chodientu.vn/crawler/new", doc);
//        postHttp("http://192.168.10.96", doc);
        postHttp("http://map.saobang.vn", doc);


//        inputDoc.setDocumentBoost(doc.getWeight());
//        inputDocs.add(inputDoc);
//        if (inputDocs.size() + numDeletes >= commitSize) {
//            try {
//                LOG.info("RaovatPoster: Indexing " + Integer.toString(inputDocs.size()) + " documents");
//                LOG.info("RaovatPoster: Deleting " + Integer.toString(numDeletes) + " documents");
//                numDeletes = 0;
//                UpdateRequest req = new UpdateRequest();
//                req.add(inputDocs);
//                req.setParams(params);
//                req.process(solr);
//            } catch (final SolrServerException e) {
//                throw makeIOException(e);
//            }
//            inputDocs.clear();
//        }
    }

    public void close() throws IOException {
        try {
            if (!inputDocs.isEmpty()) {
                LOG.info("RaovatPoster: Indexing " + Integer.toString(inputDocs.size()) + " documents");
                if (numDeletes > 0) {
                    LOG.info("RaovatPoster: Deleting " + Integer.toString(numDeletes) + " documents");
                }
                UpdateRequest req = new UpdateRequest();
                req.add(inputDocs);
                req.setParams(params);
                req.process(solr);
                inputDocs.clear();
            }
        } catch (final SolrServerException e) {
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
//            tmp = doc.getFieldValue("contentHtml");
//            if (tmp != null) {
//                method.addParameter("ContentModel[contentHtml]", tmp.toString().trim());
//            }
            tmp = doc.getFieldValue("location");
            if (tmp != null) {
                method.addParameter("ContentModel[local]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
            }
            tmp = doc.getFieldValue("thumbImage");
            if (tmp != null) {
                method.addParameter("ContentModel[thumb]", tmp.toString().trim());
            }
//            tmp = doc.getFieldValue("tstamp");
//            tmp = doc.getFieldValue("created");
//            if (tmp != null) {
//                method.addParameter("ContentModel[createDate]", Base64.encodeBase64String(tmp.toString().trim().getBytes()));
//            }
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
            if (returnCode != HttpStatus.ORDINAL_200_OK) {
                toFile("logs-post/" + myid + "-error.html", data);
                toFile("logs-post/" + myid + "-post-error.html", Arrays.toString(method.getParameters()).getBytes());
//            } else if (returnCode == HttpStatus.ORDINAL_200_OK) {
//                toFile("logs-post/" + myid + ".html", data);
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
        final IOException ioe = new IOException();
        ioe.initCause(e);
        return ioe;
    }
}
