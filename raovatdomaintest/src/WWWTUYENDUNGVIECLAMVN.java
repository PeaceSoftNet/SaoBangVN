
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
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
public class WWWTUYENDUNGVIECLAMVN extends BaseDomain {

    public WWWTUYENDUNGVIECLAMVN() throws Exception {
        this.FILE_CONFIG = "crawl-plugins/tuyendungvieclam.vn.txt";
    }

    public String[] getCategory() {
        String[] categories = null;
        try {
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
                    List<Element> elements = element.getAllElementsByClass("thongtin_tuyendung");
                    element = elements.get(2).getFirstElement(HTMLElementName.A);
                    categories = new String[2];
                    categories[0] = "Default";
                    categories[1] = element.getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse category from " + this.url + " error:" + ex.toString());
        }
        return categories;
    }

    public String getLocation() {
        try {
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
                    List<Element> elements = element.getAllElementsByClass("thongtin_tuyendung");
                    element = elements.get(4).getFirstElement(HTMLElementName.A);
                    return element.getTextExtractor().toString();
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse location from " + this.url + " error:" + ex.toString());
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();

        String url = "http://www.careerlink.vn/tim-viec-lam/nhan-vien-kinh-doanh-bat-dong-san-thu-nhap-hap-dan-on-dinh-15-nguoi/279410";//http://muaban.com.vn/index/frame/detail/id/1896530/IPHONE-5-32gb-moi-ve-khuyen-mai-50-gia-cuc-soc.html";
        url = "http://www.tuyendungvieclam.vn/chi-tiet-tuyen-dung/9658/Tuyen-dung-viec-lam-tai-Singapore-lam-viec-trong-3-thang.html";
        url = "http://www.tuyendungvieclam.vn/tim-viec-theo/nganh-nghe/3/Ban-hang.html";

        WWWTUYENDUNGVIECLAMVN tuyendungvieclam = new WWWTUYENDUNGVIECLAMVN();
        tuyendungvieclam.setConf(conf);
        tuyendungvieclam.parse(url);
        String[] tmp = tuyendungvieclam.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(tuyendungvieclam.getLocation());
        System.out.println(tuyendungvieclam.getTitle());
        System.out.println(tuyendungvieclam.getMobile());
        System.out.println(tuyendungvieclam.getEmail());
        System.out.println(tuyendungvieclam.getContent());

        Outlink[] links = tuyendungvieclam.getOutlinks();

        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}
