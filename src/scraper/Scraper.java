package scraper;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.io.*;
import org.json.*;

import java.io.*;

public class Scraper {
    
    static final String CONFIG_PATH = "C:\\Users\\Lukas\\Documents\\Programming" +
            "-Technology\\CECScraper\\CECScraper\\res\\config.json";
    
    private JSONObject configFile;
    private WebClient browser;
    
    
    Scraper() throws Exception {
        // Parse config.json
        InputStream is = new FileInputStream(CONFIG_PATH);
        this.configFile = new JSONObject(IOUtils.toString(is, "UTF-8"));
        
        // Log in
        this.browser = new WebClient(BrowserVersion.FIREFOX_52);
        HtmlPage currentPage = browser.getPage(configFile.getString("goal_page"));
        // Check if we were redirected to login page
        if (!currentPage.getUrl().toString().equals(configFile.getString("goal_page"))) {
            currentPage = login(currentPage);
            if (!currentPage.getUrl().toString().equals(configFile.getString("goal_page"))) {
                throw new RuntimeException("Unable to login");
            }
        }
        System.out.println(currentPage.getPage().toString());
    }
    
    // Login to the weblogin page passed in
    HtmlPage login(HtmlPage currentPage) throws IOException {
        HtmlForm loginForm = currentPage.getFormByName("query");
        loginForm.getInputByName("user").setValueAttribute(this.configFile.getString("netid"));
        loginForm.getInputByName("pass").setValueAttribute(this.configFile.getString("password"));
        return loginForm.getInputByName("submit").click();
    }
    
    // Scrape all of the data
    void scrape(HtmlPage mainDirectoryPage) {
    
    }
    
    public static void main(String[] args) throws Exception {
        new Scraper();
    }
}
