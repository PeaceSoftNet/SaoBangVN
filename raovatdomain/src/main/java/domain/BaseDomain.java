package domain;

import domain.common.CURL;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.peacesoft.nutch.parse.DomainParser;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.parse.Outlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDomain
        implements DomainParser {

    protected static Logger LOG = LoggerFactory.getLogger(BaseDomain.class);
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
    protected boolean isSOCK5 = false;

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConf() {
        return this.conf;
    }

    public BaseDomain() throws Exception {
    }

    protected void loadPropeties() throws Exception {
        this.fs = FileSystem.get(getConf());
        Path rootpath = new Path(getConf().get("crawl.plugins.path", "."));
        if (this.properties == null) {
            Path file = new Path(rootpath, this.FILE_CONFIG);
            this.properties = new Properties();
            this.properties.load(new InputStreamReader(this.fs.open(file)));
        }
        this.isSOCK5 = getConf().getBoolean("crawl.sock5", false);
        this.CONNECT_TIMEOUT = getConf().getInt("parser.timeout", this.CONNECT_TIMEOUT);
        this.READ_TIMEOUT = getConf().getInt("parser.timeout", this.READ_TIMEOUT);
    }

    protected String getValue(String key) throws Exception {
        if (this.properties == null) {
            loadPropeties();
        }
        return this.properties.getProperty(key, "");
    }

    protected String extractValue(String pattern, String html) {
        Pattern pattern_ = Pattern.compile(pattern);
        Matcher matcher_ = pattern_.matcher(html);
        String mobile = "";
        if (matcher_.find()) {
            return matcher_.group();
        }
        return mobile;
    }

    protected Object extractText(Element element, boolean isHTML, String key) {
        List<Element> elements = element.getAllElements("tr");
        for (Element e : elements) {
            List tds = e.getAllElements("td");
            String tmp = ((Element) tds.get(0)).getTextExtractor().toString();
            if ((tmp != null) && (!tmp.isEmpty()) && (tmp.indexOf(key) >= 0)) {
                if (isHTML) {
                    return tds.get(1);
                }
                return ((Element) tds.get(1)).getTextExtractor().toString();
            }
        }

        return "";
    }

    public String getDomain() {
        return this.domain;
    }

    public String getTitle() {
        try {
            String value = getValue(this.TITLE_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                }
                if (element != null) {
                    return element.getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse title from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public String getContent() {
        try {
            String value = getValue(this.CONTENT_PARAM);
            String type = getValue(this.CONTENT_TYPE_PARAM);

            StringBuilder sb = new StringBuilder("");
            String[] sourceElements = value.split(",");
            for (String tmp : sourceElements) {
                String[] params = tmp.split(":");
                if ((params != null) && (params.length == 2)) {
                    Element element;
                    if (params[0].equalsIgnoreCase("id")) {
                        element = this.source.getElementById(params[1]);
                    } else {
                        if (params[0].equalsIgnoreCase("class")) {
                            element = this.source.getFirstElementByClass(params[1]);
                        } else {
                            if (params[0].equalsIgnoreCase("tag")) {
                                element = this.source.getFirstElement(params[1]);
                            } else {
                                element = this.source.getFirstElement(params[0], params[1], true);
                            }
                        }
                    }
                    if (element != null) {
                        if (type.equalsIgnoreCase("html")) {
                            sb.append(element.toString());
                        } else {
                            sb.append(element.getTextExtractor().toString());
                        }
                    }
                }
            }
            return sb.toString();
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse content from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public String[] getCategory() {
        return new String[]{""};
    }

    public String getLocation() {
        return "";
    }

    public String getMobile() {
        return "";
    }

    public String getAddress() {
        return "";
    }

    public Outlink[] getOutlinks() {
        List outlinks = new ArrayList();
        try {
            List<Element> elements = null;
            String zoneid = getValue(this.ZONE_PARAM);

            String[] sourceElements = zoneid.split(",");
            for (String tmp : sourceElements) {
                String[] _sElement = tmp.split(":");
                Element element;
                if (_sElement[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(_sElement[1]);
                } else {
                    if (_sElement[0].equalsIgnoreCase("class")) {
                        element = this.source.getFirstElementByClass(_sElement[1]);
                    } else {
                        if (_sElement[0].equalsIgnoreCase("tag")) {
                            element = this.source.getFirstElement(_sElement[1]);
                        } else {
                            element = this.source.getFirstElement(_sElement[0], _sElement[1], true);
                        }
                    }
                }
                if (element != null) {
                    elements = element.getAllElements("a");
                }
                if (elements != null) {
                    for (Element _element : elements) {
                        String href = _element.getAttributeValue("href");
                        String hrefWithoutProtocol = "";
                        if (href != null) {
                            if (href.startsWith("http://")) {
                                hrefWithoutProtocol = href.substring(7);
                            }
                            if ((!hrefWithoutProtocol.contains("javascript:")) && (!hrefWithoutProtocol.contains("@"))) {
                                href = URLCanonicalizer.getCanonicalURL(href, this.contextURL);
                            }

                            if (href != null) {
                                Outlink outlink = new Outlink(href, _element.getRenderer().toString());
                                outlinks.add(outlink);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse out links from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return (Outlink[]) outlinks.toArray(tmp);
    }

    public void parse(String url) throws Exception {
        URL base = new URL(url);
        if (base.getHost().contains("timviecnhanh.com")) {
            this.source = getSourceBy(new CURL(base, this.isSOCK5));
        } else if (base.getHost().contains("batdongsan.com.vn")) {
            this.source = getSourceBy(new CURL(base, this.isSOCK5));
        } else {
            this.source = getSourceBy(base.openConnection());
        }
        this.url = url;
        this.contextURL = new StringBuilder().append("http://").append(base.getHost()).toString();
    }

    private Source getSourceBy(URLConnection conn) throws Exception {
        conn.setConnectTimeout(this.CONNECT_TIMEOUT);
        conn.setReadTimeout(this.READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", this.USER_AGENT);
        return new Source(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    }

    private Source getSourceBy(CURL base) throws Exception {
        return new Source(new ByteArrayInputStream(base.getContent()));
    }

    public String getThumb() {
        try {
            String value = getValue(this.CONTENT_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                }
                if (element != null) {
                    element = element.getFirstElement("img");
                    if (element != null) {
                        String href = element.getAttributeValue("src");
                        if (!href.startsWith("http://"));
                        return URLCanonicalizer.getCanonicalURL(href, this.contextURL);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse thumb from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public long getPrice() {
        return 0L;
    }

    public String getEmail() {
        return "noreply@saobang.vn";
    }
}