package scraper;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.stream.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class JSONtoCSV {
    
    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        Type setType = new TypeToken<Set<Course>>() {
        }.getType();
        BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Programming " +
                "Projects\\CECScraper\\res\\csv\\course_data.csv"));
        
        StringBuilder firstRow = new StringBuilder("url,department,courseID,section,instructor,instructorTitle,quarter,IASystemForm,surveyed,enrolled,");
        String[] types = {"courseAsWhole", "courseContent", "instructorContribution",
                "instructorEffectiveness", "instructorInterest", "amountLearned",
                "gradingTechniques" };
        for (String type : types) {
            firstRow.append(type + "Excellent,");
            firstRow.append(type + "VeryGood,");
            firstRow.append(type + "Good,");
            firstRow.append(type + "Fair,");
            firstRow.append(type + "Poor,");
            firstRow.append(type + "VeryPoor,");
        }
        firstRow.deleteCharAt(firstRow.length() - 1);
        writer.write(firstRow.toString());
        writer.newLine();
        writer.flush();
        
        for (int i = 0; i < 23; i++) {
            String fileDir = new StringBuilder("D:\\Programming Projects\\CECScraper\\res\\json\\")
                    .append((char) ('a' + i)).append(".json").toString();
            Set<Course> set = gson.fromJson(new JsonReader(new FileReader(fileDir)),
                    setType);
            
            for (Course c : set) {
                writer.write(c.toCSVLine());
                writer.newLine();
            }
            writer.flush();
        }
    }
    
}
