package scraper;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.gson.*;
import org.apache.commons.io.*;
import org.json.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

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
    
    // Grab course data from a course page and return as a Course object
    Course scrapeCoursePage(HtmlPage coursePage) {
        Course course = new Course();
        
        // Interpret text in the title for information
        String title = coursePage.getTitleText();
        
        // Get department
        Pattern titleDepartmentPattern = Pattern.compile("^.*?(?=[A-Z]+\\s+)");
        Matcher titleDepartmentMatcher = titleDepartmentPattern.matcher(title);
        titleDepartmentMatcher.find();
        
        course.department = titleDepartmentMatcher.group();
        
        // Get course ID
        String titleReverse = new StringBuilder(title).reverse().toString();
        Pattern titleIDPattern = Pattern.compile("\\d{3}.*?(?=[a-z])");
        Matcher titleIDMatcher = titleIDPattern.matcher(titleReverse);
        titleIDMatcher.find();
        
        course.courseID =
                removeBlankSpace(new StringBuilder(titleIDMatcher.group()).reverse()).toString();
        
        // Get instructor / extra info
        Pattern titleSecondHalfPattern = Pattern.compile("(?<=\\d{3}).*");
        Matcher titleSecondHalfMatcher = titleSecondHalfPattern.matcher(title);
        titleSecondHalfMatcher.find();
        
        String[] secondHalfInformation = titleSecondHalfMatcher.group().trim().split("\\s+");
        course.section = secondHalfInformation[0];
        course.instructor = secondHalfInformation[1] + " " + secondHalfInformation[2];
        course.instructorTitle = secondHalfInformation[3];
        for (int i = 4; i < secondHalfInformation.length - 1; i++) {
            course.instructorTitle = " " + secondHalfInformation[i];
            //TODO this is broken, only returns one word
        }
        course.quarter = secondHalfInformation[secondHalfInformation.length - 1];
        
        
        return course;
    }
    
    private StringBuilder removeBlankSpace(StringBuilder sb) {
        int j = 0;
        for (int i = 0; i < sb.length(); i++) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                sb.setCharAt(j++, sb.charAt(i));
            }
        }
        sb.delete(j, sb.length());
        return sb;
    }
    
    public static void main(String[] args) throws Exception {
        new Scraper();
    }
}
