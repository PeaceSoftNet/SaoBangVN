
import java.util.List;
import net.htmlparser.jericho.Element;

public class WWW24HCOMVN extends BaseDomain {

    public WWW24HCOMVN()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/24h.com.vn.txt";
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
                } else {
                    Element e = this.source.getFirstElement(params[0], params[1], true);
                    element = e.getFirstElement("table");
                }
                if (element != null) {
                    if (type.equalsIgnoreCase("html")) {
                        return element.getTextExtractor().toString();
                    }
                    return element.getRenderer().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse content from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
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
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElementsByClass("tbInfo-row br-L");
                    if (elements.size() >= 5) {
                        categories = new String[2];
                        categories[0] = "Default";
                        categories[1] = ((Element) elements.get(2)).getTextExtractor().toString();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + this.url + " error:" + ex.toString(), ex);
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
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElementsByClass("tbInfo-row br-L");
                    if (elements.size() >= 5) {
                        return ((Element) elements.get(4)).getTextExtractor().toString();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public String getMobile() {
        try {
            return extractValue(getValue(this.MOBILE_REGEX), getContent());
        } catch (Exception ex) {
            LOG.warn("Parse mobile from " + this.url + " error:" + ex.toString(), ex);
        }
        return "";
    }

    public String getEmail() {
        try {
            return extractValue(getValue(this.EMAIL_REGEX), getContent());
        } catch (Exception ex) {
            LOG.warn("Parse email from " + this.url + " error:" + ex.toString(), ex);
        }
        return "noreply@saobang.vn";
    }
}