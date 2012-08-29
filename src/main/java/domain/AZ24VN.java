package domain;

import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class AZ24VN extends BaseDomain {

    public AZ24VN() throws Exception {
        FILE_CONFIG = "crawl-plugins/az24.vn.txt";
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
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElement(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements(HTMLElementName.A);
                }
            }
            if (elements != null && !elements.isEmpty()) {
                if (elements.size() == 3) {
                    categories = new String[2];
                    categories[0] = elements.get(1).getTextExtractor().toString();
                    categories[1] = elements.get(2).getTextExtractor().toString();
                } else if (elements.size() == 4) {
                    categories = new String[2];
                    categories[0] = elements.get(2).getTextExtractor().toString();
                    categories[1] = elements.get(3).getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + url + " error:" + ex.toString());
        }
        return categories;
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
                    return element.getTextExtractor().toString();
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
                    return extractValue(getValue(MOBILE_REGEX), element.getTextExtractor().toString());
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse mobile from " + url + " error:" + ex.toString(), ex);
        }
        return "";
    }
}
