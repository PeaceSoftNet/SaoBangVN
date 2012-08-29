package domain;

import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

/**
 *
 * @author Tran Anh tuan <tuanta2@peacesoft.net>
 */
public class RONGBAYCOM extends BaseDomain {

    public RONGBAYCOM() throws Exception {
        FILE_CONFIG = "crawl-plugins/rongbay.com.txt";
    }

    @Override
    public String[] getCategory() {
        String[] categories = null;
        try {
            List<Element> elements = null;
            String value = getValue(LOCATION_PARAM);
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
                categories = new String[2];
                categories[0] = elements.get(elements.size() - 2).getTextExtractor().toString();
                categories[1] = elements.get(elements.size() - 1).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + url + " error:" + ex.toString(), ex);
        }
        return categories;
    }

    @Override
    public String getLocation() {
        try {
            List<Element> elements = null;
            String value = getValue(LOCATION_PARAM);
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
                return elements.get(0).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + url + " error:" + ex.toString(), ex);
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
