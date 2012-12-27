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

import java.util.List;
import net.htmlparser.jericho.Element;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.util.NutchConfiguration;

public class MANGTIMVIECCOM extends BaseDomain {

    public MANGTIMVIECCOM()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/mangtimviec.com.txt";
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
                    elements = element.getAllElements("table");
                    element = (Element) extractText((Element) elements.get(2), true, "Ngành nghề");
                    elements = element.getAllElements("a");
                    categories = new String[2];
                    categories[0] = "Default";
                    categories[1] = ((Element) elements.get(0)).getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + this.url + " error:" + ex.toString(), ex);
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
                    elements = element.getAllElements("table");
                    return (String) extractText((Element) elements.get(2), false, "Nơi làm việc");
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
            return extractValue(getValue(this.EMAIL_PARAM), getContent());
        } catch (Exception ex) {
            LOG.warn("Parse email from " + this.url + " error:" + ex.toString(), ex);
        }
        return "noreply@saobang.vn";
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();

        String url = "http://www.careerlink.vn/tim-viec-lam/nhan-vien-kinh-doanh-bat-dong-san-thu-nhap-hap-dan-on-dinh-15-nguoi/279410";//http://muaban.com.vn/index/frame/detail/id/1896530/IPHONE-5-32gb-moi-ve-khuyen-mai-50-gia-cuc-soc.html";

        url = "http://mangtimviec.com/tuyendungvieclam/business-manager/18363?cat=199";
        url = "http://mangtimviec.com/tuyendungvieclam/c%C3%B4ng-nh%C3%A2n-k%E1%BB%B9-thu%E1%BA%ADt/18630";
        
        MANGTIMVIECCOM mangtiemvieccom = new MANGTIMVIECCOM();
        mangtiemvieccom.setConf(conf);
        mangtiemvieccom.parse(url);
        String[] tmp = mangtiemvieccom.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(mangtiemvieccom.getLocation());
        System.out.println(mangtiemvieccom.getTitle());
        System.out.println(mangtiemvieccom.getMobile());
        System.out.println(mangtiemvieccom.getEmail());
        System.out.println(
                StringEscapeUtils.unescapeHtml(mangtiemvieccom.getContent()));
        Outlink[] links = mangtiemvieccom.getOutlinks();

        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}
