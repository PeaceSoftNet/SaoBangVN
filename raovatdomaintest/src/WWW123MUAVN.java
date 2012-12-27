
import java.util.List;
import net.htmlparser.jericho.Element;

public class WWW123MUAVN extends BaseDomain {

    public WWW123MUAVN()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/123mua.vn.txt";
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
                    elements = element.getAllElements("span");
                }
            }
            if ((elements != null) && (!elements.isEmpty())) {
                categories = new String[2];
                categories[0] = ((Element) elements.get(1)).getTextExtractor().toString();
                categories[1] = ((Element) elements.get(2)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + this.url + " error:" + ex.toString(), ex);
        }
        return categories;
    }

    public String getLocation() {
        try {
            List elements = null;
            String value = getValue(this.LOCATION_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElementsByClass("option");
                }
            }
            if ((elements != null) && (!elements.isEmpty())) {
                String tmp = ((Element) elements.get(0)).getTextExtractor().toString();
                if (tmp.indexOf("Phạm vi bán") > -1) {
                    return tmp.substring(11);
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
            Element element = null;
            if ((params != null) && (params.length == 2)) {
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                }
            }
            if (element != null) {
                return extractValue(getValue(this.MOBILE_REGEX), element.getTextExtractor().toString());
            }
        } catch (Exception ex) {
            LOG.warn("Parse mobile from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public String getAddress() {
        try {
            List elements = null;
            String value = getValue(this.ADDRESS_PARAM);
            String[] params = value.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = this.source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElementsByClass("info");
                }
            }
            if (elements != null) {
                Element link = (Element) elements.get(2);
                return link.getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse address from " + this.url + " error:" + ex.toString(), ex);
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
                    String content = element.getFirstElementByClass("price").getTextExtractor().toString();
                    String price = extractValue(getValue(this.PRICE_REGEX), content);
                    try {
                        price = price.replaceAll("\\.", "");
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