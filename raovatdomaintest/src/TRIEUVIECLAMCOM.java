
import java.util.List;
import net.htmlparser.jericho.Element;
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
public class TRIEUVIECLAMCOM extends BaseDomain {

    public TRIEUVIECLAMCOM() throws Exception {
        this.FILE_CONFIG = "crawl-plugins/trieuvieclam.com.txt";
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
                    String tmp = element.getContent().toString();
                    String[] cat = tmp.split("<br>");
                    categories = new String[2];
                    categories[0] = "Default";
                    categories[1] = cat[0].trim();
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
                    elements = element.getAllElements("ul");
                    if (elements.size() >= 3) {
                        element = (Element) elements.get(2);
                        elements = element.getAllElements("a");
                        return ((Element) elements.get(0)).getTextExtractor().toString();
                    }
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
        url = "http://trieuvieclam.com/viec-lam-tuyen-dung/X%C3%A2y%20d%E1%BB%B1ng/";
        url = "http://trieuvieclam.com/display-job/69516/Vi%E1%BB%87c-l%C3%A0m-Nh%C3%A2n-vi%C3%AAn-t%C3%A0i-x%E1%BA%BF.html";

        TRIEUVIECLAMCOM trieuvieclam = new TRIEUVIECLAMCOM();
        trieuvieclam.setConf(conf);
        trieuvieclam.parse(url);
        String[] tmp = trieuvieclam.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(trieuvieclam.getLocation());
        System.out.println(trieuvieclam.getTitle());
        System.out.println(trieuvieclam.getMobile());
        System.out.println(trieuvieclam.getEmail());
        System.out.println(trieuvieclam.getContent());

        Outlink[] links = trieuvieclam.getOutlinks();
        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}