

import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.peacesoft.nutch.parse.URLCanonicalizer;
import org.apache.nutch.parse.Outlink;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class WWWRAOVATVN extends BaseDomain {

    public WWWRAOVATVN() throws Exception {
        FILE_CONFIG = "crawl-plugins/raovat.vn.txt";
    }

    @Override
    public String[] getCategory() {
        String[] categories = null;
        try {
            Element element = source.getFirstElement(HTMLElementName.H2);
            categories = new String[2];
            categories[0] = "";
            categories[1] = element.getTextExtractor().toString();
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + url + " error:" + ex.toString(), ex);
        }
        return categories;
    }

    @Override
    public Outlink[] getOutlinks() {
        List<Outlink> outlinks = new ArrayList<Outlink>();
        try {
            List<Element> elements = null;
            String zoneid = getValue(ZONE_PARAM);
            String[] params = zoneid.split(":");
            if (params != null && params.length == 2) {
                Element element = null;
                if (params[0].equalsIgnoreCase("id")) {
                    element = source.getElementById(params[1]);
                } else if (params[0].equalsIgnoreCase("class")) {
                    element = source.getFirstElementByClass(params[1]);
                } else if (params[0].equalsIgnoreCase("tag")) {
                    element = source.getFirstElementByClass(params[1]);
                }
                if (element != null) {
                    elements = element.getAllElementsByClass("title");
                }
            }
            if (elements != null) {
                for (Element link : elements) {
                    Element element = link.getFirstElement(HTMLElementName.A);

                    String href = element.getAttributeValue("href");
                    String hrefWithoutProtocol = "";
                    if (href != null) {
                        if (href.startsWith("http://")) {
                            hrefWithoutProtocol = href.substring(7);
                        }
                        if (!hrefWithoutProtocol.contains("javascript:") && !hrefWithoutProtocol.contains("@")) {
                            href = URLCanonicalizer.getCanonicalURL(href, contextURL);

                        }
                        if (href != null) {
                            Outlink outlink = new Outlink(href, element.getRenderer().toString());
                            outlinks.add(outlink);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Parse out links from " + url + " error:" + ex.toString(), ex);
        }
        Outlink[] tmp = new Outlink[outlinks.size()];
        return outlinks.toArray(tmp);
    }
}
