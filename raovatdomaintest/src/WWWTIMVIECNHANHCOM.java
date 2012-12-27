

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.nutch.parse.Outlink;

public class WWWTIMVIECNHANHCOM extends BaseDomain {

    public WWWTIMVIECNHANHCOM()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/timviecnhanh.com.txt";
    }

    public String getContent() {
        try {
            String value = getValue(this.CONTENT_PARAM);
            String type = getValue(this.CONTENT_TYPE_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                } else if (params[0].equalsIgnoreCase("customize")) {
                    List elements = this.source.getAllElementsByClass(params[1]);
                    if (elements.size() == 11) {
                        StringBuilder sb = new StringBuilder("");
                        if (type.equalsIgnoreCase("html")) {
                            sb.append(((Element) elements.get(3)).getContent().toString());
                            sb.append(((Element) elements.get(6)).getContent().toString());
                        } else {
                            sb.append(((Element) elements.get(3)).getRenderer().toString());
                            sb.append(((Element) elements.get(6)).getRenderer().toString());
                        }
                        return sb.toString();
                    }
                }
                if (element != null) {
                    if (type.equalsIgnoreCase("html")) {
                        return element.getTextExtractor().toString();
                    }
                    return element.getRenderer().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse content from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public Outlink[] getOutlinks() {
        List outlinks = new ArrayList();
        try {
            List<Element> elements = null;
            String zoneid = getValue(this.ZONE_PARAM);
            String[] params = zoneid.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    List zones = this.source.getAllElementsByClass(params[1]);
                    elements = new ArrayList();
                    if (zones.size() >= 2) {
                        elements.addAll(((Element) zones.get(0)).getAllElements("a"));
                        elements.addAll(((Element) zones.get(1)).getAllElements("a"));
                    } else {
                        elements.addAll(((Element) zones.get(0)).getAllElements("a"));
                    }
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements("a");
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
                        if ((!hrefWithoutProtocol.contains("javascript:")) && (!hrefWithoutProtocol.contains("@"))) {
                            href = URLCanonicalizer.getCanonicalURL(href, this.contextURL);
                        }

                        if (href != null) {
                            Outlink outlink = new Outlink(href, element.getRenderer().toString());
                            outlinks.add(outlink);
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
                } else if (params[0].equalsIgnoreCase("customize")) {
                    elements = this.source.getAllElementsByClass(params[1]);
                    if (elements.size() >= 4) {
                        element = (Element) elements.get(3);
                    } else {
                        element = null;
                    }
                }
                if (element != null) {
                    elements = element.getAllElements("tr");
                }
            }
            if ((elements != null) && (!elements.isEmpty()) && (elements.size() >= 3)) {
                elements = ((Element) elements.get(2)).getAllElements("a");
                categories = new String[2];
                categories[0] = "Default";
                categories[1] = ((Element) elements.get(0)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse out links from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return categories;
    }

    public String getLocation() {
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
                } else if (params[0].equalsIgnoreCase("customize")) {
                    elements = this.source.getAllElementsByClass(params[1]);
                    if (elements.size() >= 4) {
                        element = (Element) elements.get(3);
                    } else {
                        element = null;
                    }
                }
                if (element != null) {
                    elements = element.getAllElements("tr");
                }
            }
            if ((elements != null) && (!elements.isEmpty()) && (elements.size() >= 4)) {
                elements = ((Element) elements.get(3)).getAllElements("a");
                return ((Element) elements.get(0)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse location from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public String getMobile() {
        try {
            return extractValue(getValue(this.MOBILE_REGEX), getContent());
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse mobile from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
    }

    public String getEmail() {
        try {
            return extractValue(getValue(this.EMAIL_REGEX), getContent());
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse email from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "noreply@saobang.vn";
    }
}