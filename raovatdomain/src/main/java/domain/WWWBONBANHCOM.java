package domain;

import java.util.List;
import net.htmlparser.jericho.Element;

public class WWWBONBANHCOM extends BaseDomain {

    public WWWBONBANHCOM()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/bonbanh.com.txt";
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
                categories[0] = "Oto";
                categories[1] = ((Element) elements.get(1)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + this.url + " error:" + ex.toString());
        }
        return categories;
    }

    public String getMobile() {
        try {
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

    public long getPrice() {
        try {
            String value = getValue(this.PRICE_PARAM);
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
                    try {
                        String price = element.getTextExtractor().toString();
                        int lastIndex = price.lastIndexOf("-");
                        price = price.substring(price.lastIndexOf("-") + 1, price.indexOf("(", lastIndex));
                        price = price.replaceAll(" ", "");
                        String[] tmp = price.split("Tỷ");
                        if (tmp.length == 2) {
                            tmp[1] = tmp[1].replaceAll("Triệu", "000000").trim();
                            price = Long.valueOf(tmp[0]).longValue() * 1000000000L + Long.valueOf(tmp[1]).longValue() + "";
                        } else {
                            price = price.replaceAll("Triệu", "000000");
                        }
                        return Long.valueOf(price).longValue();
                    } catch (Exception ex) {
                        return 0L;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse price from " + this.url + " error:" + ex.toString(), ex);
        }
        return 0L;
    }
}