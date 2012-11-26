/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.peacesoft.nutch.crawl;

import java.io.OutputStream;
import java.text.*;
import java.util.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.indexer.solr.SolrDeleteDuplicates;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.util.HadoopFSUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReCrawl extends Configured implements Tool {

    public static final Logger LOG = LoggerFactory.getLogger(ReCrawl.class);
    private List<String> seeds;

    private static String getDate() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
    }


    /* Perform complete crawling and indexing (to Solr) given a set of root urls and the -solr
     parameter respectively. More information and Usage parameters can be found below. */
    public static void main(String args[]) throws Exception {
        Configuration conf = NutchConfiguration.create();
        int res = ToolRunner.run(conf, new net.peacesoft.nutch.crawl.ReCrawl(), args);
        System.exit(res);
    }

    public Path getPathUrls(Path dir, List<String> seeds, String urls) throws Exception {
        String tmpSeedDir = urls + "/seed-"
                + System.currentTimeMillis();
        FileSystem fs = FileSystem.get(getConf());
        Path p = new Path(dir, tmpSeedDir);
        fs.mkdirs(p);
        Path seedOut = new Path(p, urls);
        OutputStream os = fs.create(seedOut);
        for (String s : seeds) {
            os.write(s.getBytes());
            os.write('\n');
        }
        os.flush();
        os.close();
        return p;
    }

    public void addSeedUrls(List<String> seeds) {
        this.seeds = seeds;
    }

    public List<String> getSeedUrls() {
        if (seeds == null) {
            seeds = new ArrayList<String>();
        }
        return seeds;
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: recrawl <urlDir> -solr <solrURL> [-dir d] [-threads n] [-depth i] [-topN N] [-loop N]");
            return -1;
        }

        Path rootUrlDir = null;
        Path dir = new Path("crawl-" + getDate());
        int threads = getConf().getInt("fetcher.threads.fetch", 10);
        int depth = 5;
        int loop = 1;
        long topN = Long.MAX_VALUE;
        boolean index = false;

        String solrUrl = null; //getConf().get("solr.http.address");

        for (int i = 0; i < args.length; i++) {
            if ("-dir".equals(args[i])) {
                dir = new Path(args[i + 1]);
                i++;
            } else if ("-threads".equals(args[i])) {
                threads = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("-depth".equals(args[i])) {
                depth = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("-topN".equals(args[i])) {
                topN = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("-solr".equals(args[i])) {
                solrUrl = args[i + 1];
                i++;
            } else if ("-loop".equals(args[i])) {
                loop = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("-index".equals(args[i])) {
                index = true;
                i++;
            } else if (args[i] != null) {
                rootUrlDir = new Path(args[i]);
            }
        }

        JobConf job = new NutchJob(getConf());

        if (solrUrl == null) {
            LOG.warn("solrUrl is not set, indexing will be skipped...");
        }

        FileSystem fs = FileSystem.get(job);

        if (LOG.isInfoEnabled()) {
            LOG.info("crawl started in: " + dir);
            LOG.info("rootUrlDir = " + rootUrlDir);
            LOG.info("threads = " + threads);
            LOG.info("depth = " + depth);
            LOG.info("solrUrl=" + solrUrl);
            if (topN != Long.MAX_VALUE) {
                LOG.info("topN = " + topN);
            }
        }

        Path inputDb = new Path(dir + "/inputdb");
        Path crawlDb = new Path(dir + "/crawldb");
        Path linkDb = new Path(dir + "/linkdb");
        Path segments = new Path(dir + "/segments");
        ReInjector injector = new ReInjector(getConf());
        ReGenerator generator = new ReGenerator(getConf());
        ReFetcher fetcher = new ReFetcher(getConf());
        ParseSegment parseSegment = new ParseSegment(getConf());
        ReCrawlDb crawlDbTool = new ReCrawlDb(getConf());
        ReLinkDb linkDbTool = new ReLinkDb(getConf());

        int fetchNoMore = 0;
        Path[] segs = null;
        for (int l = 0; l < loop; l++) {
            //Xoa du lieu cu di
            fs.delete(inputDb, true);
            injector.inject(inputDb, rootUrlDir);
            segs = generator.generate(inputDb, segments, -1, topN, System.currentTimeMillis());
            if (segs != null) {
                fetcher.fetch(segs[0], threads);  // fetch it
                if (!Fetcher.isParsing(job)) {
                    parseSegment.parse(segs[0]);    // parse it, if needed
                }
                crawlDbTool.update(crawlDb, segs, true, true); // update crawldb
            } else {
                LOG.info("Stopping inject to inputDB - no more URLs to fetch.");
            }
            // initialize crawlDb
            // injector.inject(crawlDb, rootUrlDir);
            int i;
            for (i = 0; i < depth; i++) {             // generate new segment
                segs = generator.generate(crawlDb, segments, -1, topN, System.currentTimeMillis());
                if (segs == null) {
                    LOG.info("Stopping at depth=" + i + " - no more URLs to fetch.");
                    fetchNoMore++;
                    break;
                }
                fetcher.fetch(segs[0], threads);  // fetch it
                if (!Fetcher.isParsing(job)) {
                    parseSegment.parse(segs[0]);    // parse it, if needed
                }
                crawlDbTool.update(crawlDb, segs, true, true); // update crawldb
            }
            if (i > 0) {
                try {
                    linkDbTool.invert(linkDb, segments, true, true, false); // invert links
                } catch (Exception ex) {
                    LOG.warn("Errors when invert links: " + ex.toString());
                }
            } else {
                LOG.warn("No URLs to fetch - check your seed list and URL filters.");
            }

            if (index) {
                if (solrUrl != null) {
                    // index, dedup & merge
                    FileStatus[] fstats = fs.listStatus(segments, HadoopFSUtil.getPassDirectoriesFilter(fs));
                    RaovatIndexer indexer = new RaovatIndexer(getConf());
                    try {
                        indexer.indexSolr(solrUrl, inputDb, crawlDb, linkDb,
                                Arrays.asList(HadoopFSUtil.getPaths(fstats)));
                    } catch (Exception ex) {
                        LOG.error("Raovat Index error" + ex.toString(), ex.fillInStackTrace());
                    }
                }
                //Delete segments temp
                FileStatus[] files = fs.listStatus(segments, HadoopFSUtil.getPassDirectoriesFilter(fs));
                Path[] sPaths = HadoopFSUtil.getPaths(files);
                for (Path path : sPaths) {
                    try {
                        fs.delete(path, true);
                    } catch (Exception ex) {
                    }
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("crawl finished: " + dir);
                }
            }
        }
        return 0;
    }
}
