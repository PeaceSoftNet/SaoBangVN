package domain.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class CURL {

    private URL base;
    private boolean isSOCK5;

    public CURL(URL base, boolean isSOCK5) {
        this.base = base;
        this.isSOCK5 = isSOCK5;
    }

    public byte[] getContent()
            throws Exception {
        String query = "curl --compressed ";
        if (this.isSOCK5) {
            query = new StringBuilder().append(query).append(" --socks5 127.0.0.1:9050").toString();
        }

        query = new StringBuilder().append(query).append(" -H \"User-Agent:Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1\" ").toString();
        query = new StringBuilder().append(query).append(" ").append(this.base.toURI()).toString();

        Process p = Runtime.getRuntime().exec(query);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder value = new StringBuilder();
        String tmp;
        while ((tmp = stdInput.readLine()) != null) {
            value.append(tmp);
        }
        p.destroy();
        return value.toString().getBytes();
    }
}