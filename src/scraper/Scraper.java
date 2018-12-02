package scraper;

import org.apache.commons.io.*;
import org.json.*;

import java.io.*;

public class Scraper {
    
    static final String CONFIG_PATH = "C:\\Users\\Lukas\\Documents\\Programming" +
            "-Technology\\CECScraper\\CECScraper\\res\\config.json";
    
    private String netid;
    private String password;
    
    
    public Scraper() {
        
        // Parse config.json
        try {
            InputStream is = new FileInputStream(CONFIG_PATH);
            JSONObject config = new JSONObject(IOUtils.toString(is, "UTF-8"));
            this.netid = config.getString("netid");
            this.password = config.getString("password");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println(netid + password);
    }
    
    public static void main(String[] args) {
        new Scraper();
    }
}
