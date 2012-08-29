package domain.error;

import domain.BaseDomain;

/**
 *
 * @author Tran Anh Tuan <tuanta2@peacesoft.net>
 */
public class MUABANNET extends BaseDomain {

    public MUABANNET() throws Exception {
        FILE_CONFIG = "/crawl-plugins/muaban.net.txt";
    }
}
