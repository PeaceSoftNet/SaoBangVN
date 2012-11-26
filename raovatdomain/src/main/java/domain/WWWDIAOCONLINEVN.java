package domain;

import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class WWWDIAOCONLINEVN extends BaseDomain {

    public WWWDIAOCONLINEVN() throws Exception {
        FILE_CONFIG = "crawl-plugins/diaoconline.vn.txt";
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
                if (elements.size() >= 1) {
                    categories = new String[2];
                    categories[0] = "";
                    categories[1] = elements.get(0).getTextExtractor().toString();
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
                    String tmp = element.getTextExtractor().toString();
                    String[] location = tmp.split(",");
                    tmp = location[location.length - 1].replaceAll("\\.", "");
                    return tmp.trim();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + url + " error:" + ex.toString());
        }
        return "";
    }

    @Override
    public String getAddress() {
        try {
            String value = getValue(ADDRESS_PARAM);
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
                    String tmp = element.getTextExtractor().toString();
                    String[] location = tmp.split(",");
                    int size = location.length;
                    if (location.length >= 3) {
                        for (int i = 0; i < size; i++) {
                            location[i] = location[i].replaceAll("\\.", "");
                        }
                        tmp = location[size - 3] + " - " + location[size - 2] + " - " + location[size - 1];
                        return tmp.trim();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + url + " error:" + ex.toString());
        }
        return "";
    }
}
