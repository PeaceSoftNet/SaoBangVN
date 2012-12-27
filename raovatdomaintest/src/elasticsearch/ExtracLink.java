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
package elasticsearch;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class ExtracLink {

    static int CONNECT_TIMEOUT = 3000;
    static int READ_TIMEOUT = 3000;
    static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";//Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0";

    public static void main(String[] args) throws Exception {
        String url = "";
        url = "http://choxeviet.com/toyota-fm10.aspx";
        URL base = new URL(url);
        URLConnection conn = base.openConnection();
        conn.setConnectTimeout(ExtracLink.CONNECT_TIMEOUT);
        conn.setReadTimeout(ExtracLink.READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", ExtracLink.USER_AGENT);
        Source source = new Source(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        Element element = source.getFirstElementByClass("BrandContent topcontent");
        List<Element> es = element.getAllElements(HTMLElementName.A);
        for (Element e : es) {
            System.out.println(e.getAttributeValue("href"));
        }
    }
    private String contextURL = "";
}
