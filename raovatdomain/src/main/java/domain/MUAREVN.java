package domain;

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.nutch.parse.Outlink;
import org.slf4j.Logger;

public class MUAREVN extends BaseDomain {

    public MUAREVN()
            throws Exception {
        this.FILE_CONFIG = "crawl-plugins/muare.vn.txt";
    }

    public Outlink[] getOutlinks() {
        List outlinks = new ArrayList();
        try {
            List<Element> elements = null;
            String zoneid = getValue(this.ZONE_PARAM);
            String[] params = zoneid.split(":");
            if ((params != null) && (params.length == 2)) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = this.source.getElementById(params[1]);
                    if (element != null) {
                        elements = element.getAllElementsByClass("Subject");
                    }
                } else if (params[0].equalsIgnoreCase("allclass")) {
                    elements = this.source.getAllElementsByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("alltag")) {
                    elements = this.source.getAllElementsByClass(params[1]);
                }
            }
            if (elements != null) {
                for (Element link : elements) {
                    Element element = link.getFirstElement("a");
                    String href = element.getAttributeValue("href");
                    String hrefWithoutProtocol = "";
                    if (href != null) {
                        if (href.startsWith("http://")) {
                            hrefWithoutProtocol = href.substring(7);
                        }
                        if ((!hrefWithoutProtocol.contains("javascript:")) && (!hrefWithoutProtocol.contains("@"))) {
                            href = URLCanonicalizer.getCanonicalURL(href, this.contextURL);
                        }

                        if (href != null) {
                            Outlink outlink = new Outlink(href, element.getRenderer().toString());
                            outlinks.add(outlink);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + this.url + " error:" + ex.toString(), ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return (Outlink[]) outlinks.toArray(tmp);
    }
}