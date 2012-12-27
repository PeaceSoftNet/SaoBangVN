
import java.util.List;
import net.htmlparser.jericho.Element;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.util.NutchConfiguration;

/*
 * Copyright 2012 Hadoop Vietnam <admin@hadoopvietnam.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CHOXEVIETCOM extends BaseDomain {

    public CHOXEVIETCOM()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/choxeviet.com.txt";
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
                categories[0] = "Default";
                categories[1] = ((Element) elements.get(2)).getTextExtractor().toString();
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
                    return extractValue(getValue(MOBILE_REGEX), content);
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
                    String price = extractValue(getValue(this.PRICE_REGEX), element.getContent().toString());
                    try {
                        price = price.replaceAll("\\.", "");
                        //price = price.substring(0, price.indexOf("(Tr"));
                        return Long.valueOf(price).longValue() * 1000000L;
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
    
    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();

        String url = "http://www.careerlink.vn/tim-viec-lam/nhan-vien-kinh-doanh-bat-dong-san-thu-nhap-hap-dan-on-dinh-15-nguoi/279410";//http://muaban.com.vn/index/frame/detail/id/1896530/IPHONE-5-32gb-moi-ve-khuyen-mai-50-gia-cuc-soc.html";
        url = "http://choxeviet.com/cho-oto/chevrolet/captiva/chevrolet-captiva-2013-c3891.aspx";
        url = "http://choxeviet.com/toyota-fm10.aspx";
        
        CHOXEVIETCOM choxevietcom = new CHOXEVIETCOM();
        choxevietcom.setConf(conf);
        choxevietcom.parse(url);
        String[] tmp = choxevietcom.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(choxevietcom.getLocation());
        System.out.println(choxevietcom.getTitle());
        System.out.println(choxevietcom.getMobile());
        System.out.println(choxevietcom.getPrice());
        System.out.println(choxevietcom.getEmail());
        //System.out.println(
        //        StringEscapeUtils.unescapeHtml(choxevietcom.getContent()));
        Outlink[] links = choxevietcom.getOutlinks();

        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}
