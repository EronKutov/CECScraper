package scraper;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.*;
import com.google.gson.*;
import org.apache.commons.io.*;
import org.json.*;

import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.concurrent.*;

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
            assert currentPage.getUrl().toString().equals(configFile.getString("goal_page")) :
                    "Unable to login";
        }
        
        scrape(currentPage);
    }
    
    // Login to the weblogin page passed in
    HtmlPage login(HtmlPage currentPage) throws IOException {
        HtmlForm loginForm = currentPage.getFormByName("query");
        loginForm.getInputByName("user").setValueAttribute(this.configFile.getString("netid"));
        loginForm.getInputByName("pass").setValueAttribute(this.configFile.getString("password"));
        return loginForm.getInputByName("submit").click();
    }
    
    // Scrape all of the data
    void scrape(HtmlPage mainDirectoryPage) throws IOException, InterruptedException {
        Gson gson = new Gson();
        Set<Course> evals = new HashSet<>();
        
        for (int i = 0; i < 23; i++) { // a through w
            TimeUnit.MILLISECONDS.sleep(configFile.getInt("delay_milliseconds"));
            
            // Get to letter directory
            StringBuilder href = new StringBuilder().append((char) ('a' + i)).append("-toc.html");
            HtmlAnchor tocAnchor = mainDirectoryPage.getAnchorByHref(href.toString());
            HtmlPage tocPage = tocAnchor.click();
            System.out.println(tocPage.toString());
            // Click on courses in directory
            List<HtmlAnchor> anchors = tocPage.getAnchors();
            for (HtmlAnchor anchor : anchors) {
                // If link starts with "a/" for example
                if (anchor.getHrefAttribute().startsWith(href.toString().charAt(0) + "/")) {
                    TimeUnit.MILLISECONDS.sleep(configFile.getInt("delay_milliseconds"));
                    HtmlPage course = anchor.click();
                    System.out.println("Scraping " + course.getUrl());
                    evals.add(scrapeCoursePage(course));
                }
            }
        }
        
        gson.toJson(evals, new FileWriter(configFile.getString("scrape_output_dir")));
    }
    
    Course scrapeCoursePage(HtmlPage coursePage) {
        Course course = new Course();
    
        String title = coursePage.getTitleText();
        
        
        return course;
    }
    
    public static void main(String[] args) throws Exception {
        new Scraper();
    }
}
