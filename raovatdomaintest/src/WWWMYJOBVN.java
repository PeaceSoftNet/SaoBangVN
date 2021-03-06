
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
/**
 *
 * @author Hadoop Vietnam <admin@hadoopvietnam.com>
 */
public class WWWMYJOBVN extends BaseDomain {

    public WWWMYJOBVN() throws Exception {
        this.FILE_CONFIG = "crawl-plugins/myjob.vn.txt";
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
                    element = element.getFirstElement(HTMLElementName.SPAN);
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
                    element = element.getFirstElement(HTMLElementName.SPAN);
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
        url = "http://www.myjob.vn/viec-lam/11028d/nhan-vien-kinh-doanh-quoc-te-oversea-nhan-vien-khai-thac.html";
        url = "http://www.myjob.vn/viec-lam/050100/ke-toan-kiem-toan.html";
        WWWMYJOBVN myjob = new WWWMYJOBVN();
        myjob.setConf(conf);
        myjob.parse(url);
        String[] tmp = myjob.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(myjob.getLocation());
        System.out.println(myjob.getTitle());
        System.out.println(myjob.getMobile());
        System.out.println(myjob.getEmail());
//        System.out.println(myjob.getContent());
        Outlink[] links = myjob.getOutlinks();

        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}
