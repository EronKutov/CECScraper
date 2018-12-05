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
    
    static final String CONFIG_PATH = "C:\\Users\\Lukas\\Documents\\Programming-Technology\\CECScraper\\CECScraper\\res\\config.json";
    
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
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        
        for (int i = 0; i < 23; i++) { // a through w
            Set<Course> evals = new HashSet<>();
            
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
            
            // Write current set to output
            String out = configFile.getString("scrape_output_dir") + href.toString() + ".json";
            System.out.println("Writing to " + out);
            FileWriter writer = new FileWriter(out);
            gson.toJson(evals, writer);
            writer.flush();
        }
    }
    
    // Grab course data from a course page and return as a Course object
    Course scrapeCoursePage(HtmlPage coursePage) {
        Course course = new Course();
        
        // Get URL
        course.url = coursePage.getUrl().toString();
        
        // Interpret text in the title for information
        String title = coursePage.getTitleText();
        
        // Get department
        Matcher titleDepartmentMatcher = Pattern.compile("^.*?(?=[A-Z]+\\s+)").matcher(title);
        titleDepartmentMatcher.find();
        
        course.department = titleDepartmentMatcher.group().trim();
        
        // Get course ID
        String titleReverse = new StringBuilder(title).reverse().toString();
        Matcher titleIDMatcher = Pattern.compile("\\d{3}.*?(?=[a-z])").matcher(titleReverse);
        titleIDMatcher.find();
        
        course.courseID =
                removeBlankSpace(new StringBuilder(titleIDMatcher.group()).reverse()).toString();
        
        // Get instructor / extra info
        Matcher titleSecondHalfMatcher = Pattern.compile("(?<=\\d{3}).*").matcher(title);
        titleSecondHalfMatcher.find();
        
        String[] secondHalfInformation = titleSecondHalfMatcher.group().trim().split("\\s+");
        course.section = secondHalfInformation[0];
        course.instructor = secondHalfInformation[1] + " " + secondHalfInformation[2];
        course.instructorTitle = secondHalfInformation[3];
        for (int i = 4; i < secondHalfInformation.length - 1; i++) {
            course.instructorTitle += " " + secondHalfInformation[i];
        }
        course.quarter = secondHalfInformation[secondHalfInformation.length - 1];
        
        // Grab the rest of the page
        String pageText = coursePage.asText();
        
        // Get form
        Matcher formMatcher = Pattern.compile("(?<=Form )[A-Z](?=:)").matcher(pageText);
        formMatcher.find();
        
        course.IASystemForm = formMatcher.group();
        
        // Get surveyed and enrolled
        Matcher surveyedMatcher = Pattern.compile("(?<=\")\\d+(?=\" surveyed)").matcher(pageText);
        surveyedMatcher.find();
        
        course.surveyed = Integer.parseInt(surveyedMatcher.group());
        
        Matcher enrolledMatcher = Pattern.compile("(?<=\")\\d+(?=\" enrolled)").matcher(pageText);
        enrolledMatcher.find();
        
        course.enrolled = Integer.parseInt(enrolledMatcher.group());
        
        // Get course evaluation arrays
        Matcher wholeMatcher =
                Pattern.compile("(?<=as a whole:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher contentMatcher =
                Pattern.compile("(?<=content:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher contributionMatcher =
                Pattern.compile("(?<=Instructor's contribution:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher effectivenessMatcher =
                Pattern.compile("(?<=Instructor's effectiveness:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher interestMatcher =
                Pattern.compile("(?<=Instuctor's interest:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher learnedMatcher =
                Pattern.compile("(?<=Amount learned:).+(?=\\d\\.\\d{2})").matcher(pageText);
        Matcher gradingMatcher =
                Pattern.compile("(?<=Grading techniques:).+(?=\\d\\.\\d{2})").matcher(pageText);
        
        if (wholeMatcher.find())
            course.courseAsWhole = parsePercentArray(wholeMatcher.group().trim());
        
        if (contentMatcher.find())
            course.courseContent = parsePercentArray(contentMatcher.group().trim());
        
        if (contributionMatcher.find())
            course.instructorContribution = parsePercentArray(contributionMatcher.group().trim());
        
        if (effectivenessMatcher.find())
            course.instructorEffectiveness = parsePercentArray(effectivenessMatcher.group().trim());
        
        if (interestMatcher.find())
            course.instructorInterest = parsePercentArray(interestMatcher.group().trim());
        
        if (learnedMatcher.find())
            course.amountLearned = parsePercentArray(learnedMatcher.group().trim());
        
        if (gradingMatcher.find())
            course.gradingTechniques = parsePercentArray(gradingMatcher.group().trim());
        
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
    
    private double[] parsePercentArray(String arrayString) {
        String[] array = arrayString.split("\\s");
        double[] out = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = Double.parseDouble(array[i].substring(0, array[i].length() - 1));
        }
        return out;
    }
    
    public static void main(String[] args) throws Exception {
        new Scraper();
    }
}
