
import java.util.List;
import net.htmlparser.jericho.Element;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.util.NutchConfiguration;

public class WWWBATDONGSANCOMVN extends BaseDomain {

    public WWWBATDONGSANCOMVN()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/batdongsan.com.vn.txt";
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
                categories[0] = ((Element) elements.get(1)).getTextExtractor().toString();
                categories[1] = ((Element) elements.get(2)).getTextExtractor().toString();
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + this.url + " error:" + ex.toString());
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
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = this.source.getFirstElement(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElements("a");
                }
                if ((elements != null)
                        && (elements.size() >= 4)) {
                    return ((Element) elements.get(3)).getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + this.url + " error:" + ex.toString());
        }
        return "";
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

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();
        String url = "http://www.careerlink.vn/tim-viec-lam/nhan-vien-kinh-doanh-bat-dong-san-thu-nhap-hap-dan-on-dinh-15-nguoi/279410";//http://muaban.com.vn/index/frame/detail/id/1896530/IPHONE-5-32gb-moi-ve-khuyen-mai-50-gia-cuc-soc.html";
        url = "http://batdongsan.com.vn/ban-can-ho-chung-cu-khu-phuc-hop-cao-tang-my-dinh/ac-c-thue-o-ct1-me-tri-tu-liem-pr2747179";
        WWWBATDONGSANCOMVN batdongsan = new WWWBATDONGSANCOMVN();
        batdongsan.setConf(conf);
        batdongsan.parse(url);
        String[] tmp = batdongsan.getCategory();
        System.out.println(tmp[0] + " - " + tmp[1]);
        System.out.println(batdongsan.getLocation());

        System.out.println(batdongsan.getTitle());
        System.out.println(batdongsan.getMobile());
        System.out.println(batdongsan.getEmail());
        Outlink[] links = batdongsan.getOutlinks();
        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
//        for
    }
}