package domain;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.peacesoft.nutch.parse.DomainParser;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.parse.Outlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tran Anh tuan <tuanta2@peacesoft.net>
 */
public class BaseDomain implements DomainParser {

    protected static Logger LOG = LoggerFactory.getLogger(DomainParser.class);
    protected String FILE_CONFIG = "crawl-plugins/domain.default.txt";
    protected String ZONE_PARAM = "div-zone-tag";
    protected String TITLE_PARAM = "div-title-tag";
    protected String CONTENT_PARAM = "div-content-tag";
    protected String CONTENT_TYPE_PARAM = "div-content-type";
    protected String CATEGOY_PARAM = "div-category-tag";
    protected String MOBILE_PARAM = "div-mobile-tag";
    protected String MOBILE_REGEX = "div-mobile-regex";
    protected String ADDRESS_PARAM = "div-address-tag";
    protected String LOCATION_PARAM = "div-location-tag";
    protected String PRICE_PARAM = "div-price-tag";
    protected String PRICE_REGEX = "div-price-regex";
    protected String EMAIL_PARAM = "div-email-tag";
    protected String EMAIL_REGEX = "div-email-regex";
    protected int CONNECT_TIMEOUT = 3000;
    protected int READ_TIMEOUT = 3000;
    protected String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0";
    protected Properties properties = null;
    protected Source source = null;
    protected String contextURL = "";
    protected String domain;
    protected String url;
    protected Configuration conf;
    protected FileSystem fs;

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConf() {
        return this.conf;
    }

    public BaseDomain() throws Exception {
    }

    protected void loadPropeties() throws Exception {
        fs = FileSystem.get(getConf());
        Path rootpath = new Path(getConf().get("crawl.plugins.path", "."));
        if (properties == null) {
            Path file = new Path(rootpath, FILE_CONFIG);
            properties = new Properties();
            properties.load(new InputStreamReader(fs.open(file)));
        }
    }

    protected String getValue(String key) throws Exception {
        if (properties == null) {
            loadPropeties();
        }
        return properties.getProperty(key, "");
    }

    protected String extractValue(String pattern, final String html) {
        Pattern pattern_ = Pattern.compile(pattern);
        Matcher matcher_ = pattern_.matcher(html);
        String mobile = "";
        while (matcher_.find()) {
            return matcher_.group();
        }
        return mobile;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    /**
     * Lay noi dung title thong qua div-title-tag trong file FILE_CONFIG
     *
     * +id:<name id>
     *
     * +class:<name class>
     *
     * +tag:<tag name> (<title>....</title>)
     *
     * @return String Noi dung title
     */
    @Override
    public String getTitle() {
        try {
            String value = getValue(TITLE_PARAM);
            String[] params = value.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElement(params[1]);
                }
                if (element != null) {
                    return element.getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse title from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    /**
     * Lay noi dung thong qua div-content-tag trong file FILE_CONFIG
     *
     * +id:<name id>
     *
     * +class:<name class>
     *
     * +tag:<tag name> (<title>....</title>)
     *
     * @return
     */
    @Override
    public String getContent() {
        try {
            String value = getValue(CONTENT_PARAM);
            String type = getValue(CONTENT_TYPE_PARAM);
            String[] params = value.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElement(params[1]);
                }
                if (element != null) {
                    if (type.equalsIgnoreCase("html")) {
                        return element.getContent().toString();
                    } else {
                        return element.getRenderer().toString();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse content from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    @Override
    public String[] getCategory() {
        return new String[]{""};
    }

    @Override
    public String getLocation() {
        return "";
    }

    @Override
    public String getMobile() {
        return "";
    }

    @Override
    public String getAddress() {
        return "";
    }

    @Override
    public Outlink[] getOutlinks() {
        List<Outlink> outlinks = new ArrayList<Outlink>();
        try {
            List<Element> elements = null;
            String zoneid = getValue(ZONE_PARAM);
            String[] params = zoneid.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements(HTMLElementName.A);
                }
            }
            if (elements != null) {
                for (Element element : elements) {
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
            }
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + url + " error:" + ex.toString(), ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return outlinks.toArray(tmp);
    }

    @Override
    public void parse(String url) throws Exception {
        URL base = new URL(url);
        URLConnection conn = base.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        this.url = url;
        source = new Source(conn);
        contextURL = "http://" + base.getHost();
    }

    public String getThumb() {
        try {
            String value = getValue(CONTENT_PARAM);
            String[] params = value.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElement(params[1]);
                }
                if (element != null) {
                    element = element.getFirstElement(HTMLElementName.IMG);
                    if (element != null) {
                        return element.getAttributeValue("src");
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse thumb from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public long getPrice() {
        return 0;
    }

    public String getEmail() {
        return "noreply@saobang.vn";
    }
}
