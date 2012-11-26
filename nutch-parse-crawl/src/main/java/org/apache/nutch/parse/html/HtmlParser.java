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
package org.apache.nutch.parse.html;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.*;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.StringUtils;
import org.apache.html.dom.*;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.*;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.*;
import org.cyberneko.html.parsers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HtmlParser implements Parser {

    public static final Logger LOG = LoggerFactory.getLogger("org.apache.nutch.parse.html");
    // I used 1000 bytes at first, but  found that some documents have 
    // meta tag well past the first 1000 bytes. 
    // (e.g. http://cn.promo.yahoo.com/customcare/music.html)
    private static final int CHUNK_SIZE = 2000;
    // NUTCH-1006 Meta equiv with single quotes not accepted
    private static Pattern metaPattern =
            Pattern.compile("<meta\\s+([^>]*http-equiv=(\"|')?content-type(\"|')?[^>]*)>",
            Pattern.CASE_INSENSITIVE);
    private static Pattern charsetPattern =
            Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)",
            Pattern.CASE_INSENSITIVE);
    private String parserImpl;
    private String CRAWL_REGEX_FILE = "crawl.regex.file";
    private Properties properties;

    /**
     * Given a
     * <code>byte[]</code> representing an html file of an <em>unknown</em>
     * encoding, read out 'charset' parameter in the meta tag from the first
     * <code>CHUNK_SIZE</code> bytes. If there's no meta tag for Content-Type or
     * no charset is specified,
     * <code>null</code> is returned. <br /> FIXME: non-byte oriented character
     * encodings (UTF-16, UTF-32) can't be handled with this. We need to do
     * something similar to what's done by mozilla
     * (http://lxr.mozilla.org/seamonkey/source/parser/htmlparser/src/nsParser.cpp#1993).
     * See also http://www.w3.org/TR/REC-xml/#sec-guessing <br />
     *
     * @param content <code>byte[]</code> representation of an html file
     */
    private static String sniffCharacterEncoding(byte[] content) {
        int length = content.length < CHUNK_SIZE
                ? content.length : CHUNK_SIZE;

        // We don't care about non-ASCII parts so that it's sufficient
        // to just inflate each byte to a 16-bit value by padding. 
        // For instance, the sequence {0x41, 0x82, 0xb7} will be turned into 
        // {U+0041, U+0082, U+00B7}. 
        String str = "";
        try {
            str = new String(content, 0, length,
                    Charset.forName("ASCII").toString());
        } catch (UnsupportedEncodingException e) {
            // code should never come here, but just in case... 
            return null;
        }

        Matcher metaMatcher = metaPattern.matcher(str);
        String encoding = null;
        if (metaMatcher.find()) {
            Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
            if (charsetMatcher.find()) {
                encoding = new String(charsetMatcher.group(1));
            }
        }

        return encoding;
    }
    private String defaultCharEncoding;
    private Configuration conf;
    private DOMContentUtils utils;
    private HtmlParseFilters htmlParseFilters;
    private String cachingPolicy;

    public ParseResult getParse(Content content) {
        HTMLMetaTags metaTags = new HTMLMetaTags();

        URL base;
        try {
            base = new URL(content.getBaseUrl());
        } catch (MalformedURLException e) {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        }

        String text = "";
        String title = "";
        Outlink[] outlinks = new Outlink[0];
        Metadata metadata = new Metadata();

        // parse the content
        Source source;
        try {
            source = new Source(base);
        } catch (Exception ex) {
            return new ParseStatus(ex).getEmptyParseResult(content.getUrl(), getConf());
        }
        DocumentFragment root;
        try {
            byte[] contentInOctets = content.getContent();
            InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));

            EncodingDetector detector = new EncodingDetector(conf);
            detector.autoDetectClues(content, true);
            detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
            String encoding = detector.guessEncoding(content, defaultCharEncoding);

            metadata.set(Metadata.ORIGINAL_CHAR_ENCODING, encoding);
            metadata.set(Metadata.CHAR_ENCODING_FOR_CONVERSION, encoding);

            input.setEncoding(encoding);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Parsing...");
            }
            root = parse(input);
        } catch (IOException e) {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        } catch (DOMException e) {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        } catch (SAXException e) {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        } catch (Exception e) {
            LOG.error("Error: ", e);
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        }

        // get meta directives
        HTMLMetaProcessor.getMetaTags(metaTags, root, base);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Meta tags for " + base + ": " + metaTags.toString());
        }
        // check meta directives
        if (!metaTags.getNoIndex()) {               // okay to index
            if (LOG.isTraceEnabled()) {
                LOG.trace("Getting content...");
            }
            text = getContent(source, getValue(base.getHost() + "-content"));          // extract text
            if (LOG.isTraceEnabled()) {
                LOG.trace("Getting title...");
            }
            StringBuffer sb = new StringBuffer();
            utils.getTitle(sb, root);          // extract title
            title = sb.toString();
        }

        if (!metaTags.getNoFollow()) {              // okay to follow links
            String contextURL = "http://" + base.getHost();
            outlinks = getOutlink(source, contextURL);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("found " + outlinks.length + " outlinks in " + content.getUrl());
        }

        ParseStatus status = new ParseStatus(ParseStatus.SUCCESS);
        if (metaTags.getRefresh()) {
            status.setMinorCode(ParseStatus.SUCCESS_REDIRECT);
            status.setArgs(new String[]{metaTags.getRefreshHref().toString(),
                        Integer.toString(metaTags.getRefreshTime())});
        }
        ParseData parseData = new ParseData(status, title, outlinks,
                content.getMetadata(), metadata);
        ParseResult parseResult = ParseResult.createParseResult(content.getUrl(),
                new ParseImpl(text, parseData));

        // run filters on parse
        ParseResult filteredParse = this.htmlParseFilters.filter(content, parseResult,
                metaTags, root);
        if (metaTags.getNoCache()) {             // not okay to cache
            for (Map.Entry<org.apache.hadoop.io.Text, Parse> entry : filteredParse) {
                entry.getValue().getData().getParseMeta().set(Nutch.CACHING_FORBIDDEN_KEY,
                        cachingPolicy);
            }
        }
        return filteredParse;
    }

    private Properties getProperties(Configuration conf) {
        Properties prop = new Properties();
        String regexFile = conf.get(CRAWL_REGEX_FILE);
        try {
            prop.load(conf.getConfResourceAsInputStream(regexFile));
        } catch (IOException ex) {
            LOG.warn("Error load crawl regex file: " + regexFile, ex);
        }
        return prop;
    }

    private String getValue(String key) {
        return properties.getProperty(key, "");
    }

//    private String getTitle(Source source) {
//        List<> source.getAllElements(HTMLElementName.TITLE);
//        
//        return "";
//    }
    private String getContent(Source source, String value) {
//        String content = "";
        String[] params = value.split(":");
        if (params != null && params.length == 2) {
            net.htmlparser.jericho.Element element = null;
            if (params[0].equalsIgnoreCase("id")) {
                element = source.getElementById(params[1]);
//                content = element.getContent().toString();
//                content = StringUtils.escapeHTML(content);
            } else if (params[0].equalsIgnoreCase("class")) {
                element = source.getFirstElementByClass(params[1]);
            }
            if (element != null) {
                return element.getContent().toString();
            }
        }
//        else {
//            content = source.getRenderer().toString();
//        }
        return source.getRenderer().toString();
    }

    private Outlink[] getOutlink(Source source, String contextURL) {
        List<Outlink> outlinks = new ArrayList<Outlink>();
        try {
            List<net.htmlparser.jericho.Element> elements = source.getAllElements(HTMLElementName.A);
            for (net.htmlparser.jericho.Element element : elements) {
                String href = element.getAttributeValue("href");
                String hrefWithoutProtocol = "";
                if (href != null) {
                    if (href.startsWith("http://")) {
                        hrefWithoutProtocol = href.substring(7);
                    }
                    if (!hrefWithoutProtocol.contains("javascript:") && !hrefWithoutProtocol.contains("@")) {
                        href = URLCanonicalizer.getCanonicalURL(href, contextURL);

                    }
                    if (href != null) {
                        Outlink outlink = new Outlink(href, element.getRenderer().toString());
                        outlinks.add(outlink);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Error when get out links", ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return outlinks.toArray(tmp);
    }

    private DocumentFragment parse(InputSource input) throws Exception {
        if (parserImpl.equalsIgnoreCase("tagsoup")) {
            return parseTagSoup(input);
        } else {
            return parseNeko(input);
        }
    }

    private DocumentFragment parseTagSoup(InputSource input) throws Exception {
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder builder = new DOMBuilder(doc, frag);
        org.ccil.cowan.tagsoup.Parser reader = new org.ccil.cowan.tagsoup.Parser();
        reader.setContentHandler(builder);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.ignoreBogonsFeature, true);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.bogonsEmptyFeature, false);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
        reader.parse(input);
        return frag;
    }

    private DocumentFragment parseNeko(InputSource input) throws Exception {
        DOMFragmentParser parser = new DOMFragmentParser();
        try {
            parser.setFeature("http://cyberneko.org/html/features/augmentations",
                    true);
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
                    defaultCharEncoding);
            parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset",
                    true);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
                    false);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
                    true);
            parser.setFeature("http://cyberneko.org/html/features/report-errors",
                    LOG.isTraceEnabled());
        } catch (SAXException e) {
        }
        // convert Document to DocumentFragment
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        doc.setErrorChecking(false);
        DocumentFragment res = doc.createDocumentFragment();
        DocumentFragment frag = doc.createDocumentFragment();
        parser.parse(input, frag);
        res.appendChild(frag);

        try {
            while (true) {
                frag = doc.createDocumentFragment();
                parser.parse(input, frag);
                if (!frag.hasChildNodes()) {
                    break;
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info(" - new frag, " + frag.getChildNodes().getLength() + " nodes.");
                }
                res.appendChild(frag);
            }
        } catch (Exception e) {
            LOG.error("Error: ", e);
        };
        return res;
    }

    public static void main(String[] args) throws Exception {
        //LOG.setLevel(Level.FINE);
        String name = args[0];
        String url = "file:" + name;
        File file = new File(name);
        byte[] bytes = new byte[(int) file.length()];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        in.readFully(bytes);
        Configuration conf = NutchConfiguration.create();
        HtmlParser parser = new HtmlParser();
        parser.setConf(conf);
        Parse parse = parser.getParse(
                new Content(url, url, bytes, "text/html", new Metadata(), conf)).get(url);
        System.out.println("data: " + parse.getData());

        System.out.println("text: " + parse.getText());

    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        this.htmlParseFilters = new HtmlParseFilters(getConf());
        this.parserImpl = getConf().get("parser.html.impl", "neko");
        this.defaultCharEncoding = getConf().get(
                "parser.character.encoding.default", "utf-8");
        this.utils = new DOMContentUtils(conf);
        this.properties = getProperties(conf);
        this.cachingPolicy = getConf().get("parser.caching.forbidden.policy",
                Nutch.CACHING_FORBIDDEN_CONTENT);
    }

    public Configuration getConf() {
        return this.conf;
    }
}
