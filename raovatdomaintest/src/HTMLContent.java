
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import net.htmlparser.jericho.Source;

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
public class HTMLContent {

    static int CONNECT_TIMEOUT = 3000;
    static int READ_TIMEOUT = 3000;
    static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11";//Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0";
    static String url = "http://muaban.com.vn/index/frame/detail/id/1896530/IPHONE-5-32gb-moi-ve-khuyen-mai-50-gia-cuc-soc.html";

    public static void main(String[] args) throws Exception {
        URL base = new URL(url);
        URLConnection conn = base.openConnection();
        conn.setConnectTimeout(HTMLContent.CONNECT_TIMEOUT);
        conn.setReadTimeout(HTMLContent.READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", HTMLContent.USER_AGENT);
        Source source = new Source(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        System.out.println(source);
    }
}
