package scraper;

public class Course implements Comparable<Course> {
    
    String url; // The evaluation URL
    String department; // The department this class is in
    String courseID; // The course ID: eg. CSE143, AA101
    String section; // The section: eg. A, B
    String instructor; // The instructor for the course
    String instructorTitle; // The title of the instructor: eg. Assistant Professor, Lecturer
    String quarter; // The quarter this course was held: eg. AU18, SP17
    String IASystemForm; // https://www.washington.edu/assessment/course-evaluations/forms/
    int surveyed; // The number of students who completed the evaluation
    int enrolled; // The total number of students enrolled in the class
    
    // The next fields are 6 length arrays representing the decimal format of the responses,
    // eg.                      Excellent 	Very Good 	Good 	Fair 	Poor 	Very Poor
    // The course as a whole: 	33%	        50%	        17%	    0%	    0%	    0%
    // Corresponding array:     0.33        0.5         0.17    0.0     0.0     0.0
    double[] courseAsWhole;
    double[] courseContent;
    double[] instructorContribution;
    double[] instructorEffectiveness;
    double[] instructorInterest;
    double[] amountLearned;
    double[] gradingTechniques;
    
    public Course() {
    
    }
    
    @Override
    public String toString() {
        return courseID + " " + quarter + " " + instructor;
    }
    
    public String toCSVLine() {
        StringBuilder out =
                new StringBuilder(url + "," +
                        department + "," +
                        courseID + "," +
                        section + "," +
                        instructor + "," +
                        instructorTitle + "," +
                        quarter + "," +
                        IASystemForm + "," +
                        surveyed + "," +
                        enrolled + ",");
        
        double[][] arrays = {courseAsWhole, courseContent, instructorContribution,
                instructorEffectiveness, instructorInterest, amountLearned, gradingTechniques};
        
        for (double[] arr : arrays) {
            if (arr == null) {
                for (int i = 0; i < 6; i++) {
                    out.append("null,");
                }
            } else {
                for (double d : arr) {
                    out.append(d + ",");
                }
            }
        }
        out.deleteCharAt(out.length() - 1);
        
        return out.toString();
    }
    
    @Override
    public int compareTo(Course course) {
        return url.compareTo(course.url);
    }
}

