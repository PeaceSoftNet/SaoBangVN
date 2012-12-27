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
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.util.NutchConfiguration;

public class TUYENDUNGCOM extends BaseDomain {

    public TUYENDUNGCOM() throws Exception {
        this.FILE_CONFIG = "crawl-plugins/tuyendung.com.txt";
    }

    public String getContent() {
        try {
            String value = getValue(this.CONTENT_PARAM);
            String type = getValue(this.CONTENT_TYPE_PARAM);

            StringBuilder sb = new StringBuilder("");
            String[] sourceElements = value.split(",");
            for (String tmp : sourceElements) {
                String[] params = tmp.split(":");
                if ((params != null) && (params.length == 2)) {
                    Element element;
                    if (params[0].equalsIgnoreCase("id")) {
                        element = this.source.getElementById(params[1]);
                    } else {
                        if (params[0].equalsIgnoreCase("class")) {
                            element = this.source.getFirstElementByClass(params[1]);
                        } else {
                            if (params[0].equalsIgnoreCase("tag")) {
                                element = this.source.getFirstElement(params[1]);
                            } else {
                                element = this.source.getFirstElement(params[0], params[1], true);
                            }
                        }
                    }
                    if (element != null) {
                        List<Element> elements = element.getAllElementsByClass("custom_content");
                        int i = 0;
                        for (Element e : elements) {
                            if (i == 3) {
                                break;
                            }
                            if (type.equalsIgnoreCase("html")) {
                                sb.append(e.toString());
                            } else {
                                sb.append(e.getTextExtractor().toString());
                            }
                            i++;
                        }
                    }
                }
            }
            return sb.toString();
        } catch (Exception ex) {
            LOG.warn(new StringBuilder().append("Parse content from ").append(this.url).append(" error:").append(ex.toString()).toString(), ex);
        }
        return "";
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
        url = "http://tuyendung.com/viec-lam/xem-tin-viec-lam/chuyen-vien-marketing-1698.html";
        url = "http://tuyendung.com/viec-lam/nganh-nghe/xuat-nhap-khau-ngoai-thuong-39";
        
        TUYENDUNGCOM tuyendungcom = new TUYENDUNGCOM();
        tuyendungcom.setConf(conf);
        tuyendungcom.parse(url);
        String[] tmp = tuyendungcom.getCategory();
        if (tmp != null) {
            System.out.println(tmp[0] + " - " + tmp[1]);
        }
        System.out.println(tuyendungcom.getLocation());
        System.out.println(tuyendungcom.getTitle());
        System.out.println(tuyendungcom.getMobile());
        System.out.println(tuyendungcom.getEmail());
//        System.out.println(tuyendungcom.getContent());
        Outlink[] links = tuyendungcom.getOutlinks();

        for (Outlink outlink : links) {
            System.out.println(outlink.getToUrl());
        }
    }
}
