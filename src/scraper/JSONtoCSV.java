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
        Type setType = new TypeToken<Set<Course>>(){}.getType();
        for (int i = 0; i < 23; i++) {
            String fileDir = new StringBuilder("D:\\Programming Projects\\CECScraper\\res\\json\\")
                    .append((char) ('a' + i)).append(".json").toString();
            Set<Course> set = gson.fromJson(new JsonReader(new FileReader(fileDir)),
                    setType);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new StringBuilder("D:\\Programming " +
                    "Projects\\CECScraper\\res\\csv\\")
                    .append((char) ('a' + i)).append(".csv").toString()));
            
            for (Course c : set) {
                writer.write(c.toCSVLine());
                writer.newLine();
            }
            writer.flush();
        }
    }
    
}
