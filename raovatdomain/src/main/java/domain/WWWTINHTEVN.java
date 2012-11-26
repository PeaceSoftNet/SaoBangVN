package domain;

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.nutch.parse.Outlink;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class WWWTINHTEVN extends BaseDomain {

    public WWWTINHTEVN() throws Exception {
        FILE_CONFIG = "crawl-plugins/tinhte.vn.txt";
    }

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
                    String title = element.getTextExtractor().toString();
                    return title.substring(title.indexOf("-") + 1);
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse title from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    @Override
    public String[] getCategory() {
        String[] categories = null;
        try {
            List<Element> elements = null;
            String value = getValue(CATEGOY_PARAM);
            String[] params = value.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);

                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements(HTMLElementName.A);
                }
            }
            if (elements != null && !elements.isEmpty()) {
                if (elements.size() >= 3) {
                    categories = new String[2];
                    categories[0] = elements.get(elements.size() - 2).getTextExtractor().toString();
                    categories[1] = elements.get(elements.size() - 1).getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + url + " error:" + ex.toString());
        }
        return categories;
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
                    elements = element.getAllElements(HTMLElementName.H3);
                }
            }
            if (elements != null) {
                for (Element link : elements) {
                    Element element = link.getFirstElementByClass("PreviewTooltip");

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
            LOG.warn("Parse out links from " + url + " error:" + ex.toString());
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return outlinks.toArray(tmp);
    }

    @Override
    public String getLocation() {
        try {
            String value = getValue(LOCATION_PARAM);
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
                    element = element.getFirstElement(HTMLElementName.SPAN);
                    if (element != null) {
                        return element.getTextExtractor().toString().replaceAll("-", "");
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + url + " error:" + ex.toString());
        }
        return "";
    }
    
    @Override
    public String getMobile() {
        try {
            List<Element> elements = null;
            String value = getValue(MOBILE_PARAM);
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
                    String content = element.getTextExtractor().toString();
                    return extractValue(MOBILE_REGEX, content);
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse mobile from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    @Override
    public String getEmail() {
        try {
            String value = getValue(EMAIL_PARAM);
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
                    String content = element.getTextExtractor().toString();
                    String email = extractValue(getValue(EMAIL_REGEX), content);
                    if (!email.isEmpty()) {
                        return email;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse email from " + url + " error:" + ex.toString(), ex);
        }
        return "noreply@saobang.vn";
    }
}
