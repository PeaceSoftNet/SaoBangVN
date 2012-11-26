package domain;

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.nutch.parse.Outlink;

public class WWW5GIAYVN extends BaseDomain {

    public WWW5GIAYVN()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/5giay.vn.txt";
    }

    public String[] getCategory() {
        String[] categories = null;
        try {
            List elements = null;
            String value = getValue(this.CATEGOY_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements("a");
                }
            }
            if ((elements != null) && (!elements.isEmpty())
                    && (elements.size() >= 3)) {
                categories = new String[2];
                categories[0] = ((Element) elements.get(elements.size() - 2)).getTextExtractor().toString();
                categories[1] = ((Element) elements.get(elements.size() - 1)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + this.url + " error:" + ex.toString(), ex);
        }
        return categories;
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
                    if ("below_threadlist".equals(_sElement[1])) {
                        elements = element.getAllElements("a");
                    } else {
                        elements = element.getAllElements("h3");
                    }
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
            LOG.warn("Parse out links from " + this.url + " error:" + ex.toString(), ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return (Outlink[]) outlinks.toArray(tmp);
    }

    public String getLocation() {
        try {
            String value = getValue(this.LOCATION_PARAM);
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
                    element = element.getFirstElement("b");
                    return element.getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public String getMobile() {
        try {
            List elements = null;
            String value = getValue(this.MOBILE_PARAM);
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
                    String content = element.getTextExtractor().toString();
                    return extractValue(this.MOBILE_REGEX, content);
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse mobile from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public String getEmail() {
        try {
            String value = getValue(this.EMAIL_PARAM);
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
                    String content = element.getTextExtractor().toString();
                    String email = extractValue(getValue(this.EMAIL_REGEX), content);
                    if (!email.isEmpty()) {
                        return email;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse email from " + this.url + " error:" + ex.toString(), ex);
        }
        return "noreply@saobang.vn";
    }
}