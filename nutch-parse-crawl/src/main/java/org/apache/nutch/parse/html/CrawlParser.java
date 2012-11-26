package org.apache.nutch.parse.html;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.peacesoft.classloader.DomainClassLoader;
import net.peacesoft.nutch.parse.DomainParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilters;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.EncodingDetector;
import org.apache.nutch.util.NutchConfiguration;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CrawlParser
        implements org.apache.nutch.parse.Parser {

    public static final Logger LOG = LoggerFactory.getLogger(CrawlParser.class);
    private static final int CHUNK_SIZE = 2000;
    private static Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=(\"|')?content-type(\"|')?[^>]*)>", 2);
    private static Pattern charsetPattern = Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)", 2);
    private String parserImpl;
    private String CRAWL_LOCATION_MAPPING = "crawl-mapping/mapping.txt";
    private Properties properties = null;
    private String defaultCharEncoding;
    private Configuration conf;
    private DOMContentUtils utils;
    private HtmlParseFilters htmlParseFilters;
    private String cachingPolicy;

    private void loadPropeties()
            throws Exception {
        if (this.properties == null) {
            File f = new File(".");
            String path = new StringBuilder().append(f.getCanonicalPath()).append("/").append(this.CRAWL_LOCATION_MAPPING).toString();
            FileInputStream fin = new FileInputStream(path);
            this.properties = new Properties();
            this.properties.load(fin);
        }
    }

    protected String getValue(String key) throws Exception {
        if (this.properties == null) {
            loadPropeties();
        }
        String tmp = key.trim().replaceAll(" ", "-");
        return this.properties.getProperty(tmp, key);
    }

    private static String sniffCharacterEncoding(byte[] content) {
        int length = content.length < 2000 ? content.length : 2000;

        String str = "";
        try {
            str = new String(content, 0, length, Charset.forName("ASCII").toString());
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        Matcher metaMatcher = metaPattern.matcher(str);
        String encoding = null;
        if (metaMatcher.find()) {
            Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
            if (charsetMatcher.find()) {
                encoding = charsetMatcher.group(1);
            }
        }

        return encoding;
    }

    public ParseResult getParse(Content content) {
        try {
            loadPropeties();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        try {
            DomainClassLoader classLoader = new DomainClassLoader();
            String domain = base.getHost();
            String[] partDomain = domain.split("\\.");
            if ((partDomain.length == 3)
                    && (!partDomain[0].equals("www")) && (partDomain[1].length() > 3)) {
                domain = domain.replaceAll(partDomain[0], "www");
            }

            DomainParser domainParser = classLoader.getClass(new StringBuilder().append("domain.").append(domain.replaceAll("[^a-zA-Z0-9]+", "").toUpperCase()).toString());
            domainParser.setConf(getConf());
            domainParser.parse(content.getBaseUrl());

            title = domainParser.getTitle();
            text = domainParser.getContent();
            outlinks = domainParser.getOutlinks();
            if ((text.isEmpty())
                    && (LOG.isWarnEnabled())) {
                LOG.warn(new StringBuilder().append("CrawlParser: Empty content from ").append(content.getBaseUrl()).toString());
            }

            if (!domainParser.getMobile().isEmpty()) {
                metadata.set("mobile", domainParser.getMobile());
            }
            if (!domainParser.getAddress().isEmpty()) {
                metadata.set("address", domainParser.getAddress());
            }
            if (!domainParser.getThumb().isEmpty()) {
                metadata.set("thumbImage", domainParser.getThumb());
            }
            if (domainParser.getPrice() != 0L) {
                metadata.set("price", String.valueOf(domainParser.getPrice()));
            }
            if (!domainParser.getEmail().isEmpty()) {
                metadata.set("email", domainParser.getEmail());
            }
            String location = domainParser.getLocation();
            if (!location.isEmpty()) {
                location = location.replaceAll("-", "");
                location = getValue(location);
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder().append("CrawlParser: Location mapping ").append(location).toString());
                }
                metadata.set("location", location);
            } else {
                location = getValue("default");
                if (LOG.isInfoEnabled()) {
                    LOG.info(new StringBuilder().append("CrawlParser: Location mapping default value ").append(location).toString());
                }
                metadata.set("location", location);
            }

            String[] categories = domainParser.getCategory();
            if ((categories != null) && (categories.length == 2)) {
                if (categories[0].isEmpty()) {
                    categories[0] = "Default";
                }
                metadata.set("categoryId", categories[0]);
                metadata.set("categoryChildId", categories[1]);
            } else if (LOG.isWarnEnabled()) {
                LOG.warn(new StringBuilder().append("CrawlParser: Empty category from ").append(content.getBaseUrl()).toString());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder().append("CrawlParser: content crawl value from  ").append(content.getBaseUrl()).toString());
                for (String name : metadata.names()) {
                    LOG.debug(new StringBuilder().append("[").append(name).append(":").append(metadata.get(name).length() > 20 ? metadata.get(name).substring(0, 20) : metadata.get(name)).append("]").toString());
                }
            }
        } catch (Exception ex) {
            LOG.error("CrawlParser: ", ex);
            return new ParseStatus(ex).getEmptyParseResult(content.getUrl(), getConf());
        }
        DocumentFragment root;
        try {
            byte[] contentInOctets = content.getContent();
            InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));

            EncodingDetector detector = new EncodingDetector(this.conf);
            detector.autoDetectClues(content, true);
            detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
            String encoding = detector.guessEncoding(content, this.defaultCharEncoding);

            metadata.set("OriginalCharEncoding", encoding);
            metadata.set("CharEncodingForConversion", encoding);

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

        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder().append("Parser plugin found ").append(outlinks.length).append(" outlinks in ").append(content.getUrl()).toString());
        }

        ParseStatus status = new ParseStatus(1);
        if (metaTags.getRefresh()) {
            status.setMinorCode((short) 100);
            status.setArgs(new String[]{metaTags.getRefreshHref().toString(), Integer.toString(metaTags.getRefreshTime())});
        }

        ParseData parseData = new ParseData(status, title, outlinks, content.getMetadata(), metadata);

        ParseResult parseResult = ParseResult.createParseResult(content.getUrl(), new ParseImpl(text, parseData));

        ParseResult filteredParse = this.htmlParseFilters.filter(content, parseResult, metaTags, root);

        if (metaTags.getNoCache()) {             // not okay to cache
            for (Map.Entry<org.apache.hadoop.io.Text, Parse> entry : filteredParse) {
                entry.getValue().getData().getParseMeta().set(Nutch.CACHING_FORBIDDEN_KEY,
                        cachingPolicy);
            }
        }

        return filteredParse;
    }

    private DocumentFragment parse(InputSource input) throws Exception {
        if (this.parserImpl.equalsIgnoreCase("tagsoup")) {
            return parseTagSoup(input);
        }
        return parseNeko(input);
    }

    private DocumentFragment parseTagSoup(InputSource input) throws Exception {
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder builder = new DOMBuilder(doc, frag);
        org.ccil.cowan.tagsoup.Parser reader = new org.ccil.cowan.tagsoup.Parser();
        reader.setContentHandler(builder);
        reader.setFeature("http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons", true);
        reader.setFeature("http://www.ccil.org/~cowan/tagsoup/features/bogons-empty", false);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
        reader.parse(input);
        return frag;
    }

    private DocumentFragment parseNeko(InputSource input) throws Exception {
        DOMFragmentParser parser = new DOMFragmentParser();
        try {
            parser.setFeature("http://cyberneko.org/html/features/augmentations", true);

            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", this.defaultCharEncoding);

            parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);

            parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);

            parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);

            parser.setFeature("http://cyberneko.org/html/features/report-errors", LOG.isTraceEnabled());
        } catch (SAXException e) {
        }
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
                    LOG.info(new StringBuilder().append(" - new frag, ").append(frag.getChildNodes().getLength()).append(" nodes.").toString());
                }
                res.appendChild(frag);
            }
        } catch (Exception e) {
            LOG.error("Error: ", e);
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        String name = args[0];
        String url = new StringBuilder().append("file:").append(name).toString();
        File file = new File(name);
        byte[] bytes = new byte[(int) file.length()];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        in.readFully(bytes);
        Configuration conf = NutchConfiguration.create();
        HtmlParser parser = new HtmlParser();
        parser.setConf(conf);
        Parse parse = parser.getParse(new Content(url, url, bytes, "text/html", new Metadata(), conf)).get(url);

        System.out.println(new StringBuilder().append("data: ").append(parse.getData()).toString());

        System.out.println(new StringBuilder().append("text: ").append(parse.getText()).toString());
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        this.htmlParseFilters = new HtmlParseFilters(getConf());
        this.parserImpl = getConf().get("parser.html.impl", "neko");
        this.defaultCharEncoding = getConf().get("parser.character.encoding.default", "utf-8");

        this.utils = new DOMContentUtils(conf);

        this.cachingPolicy = getConf().get("parser.caching.forbidden.policy", "content");
    }

    public Configuration getConf() {
        return this.conf;
    }
}