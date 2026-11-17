package org.medlearn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CourseRulesTest {
 @Test void languageFallback(){assertEquals("sq",CourseController.language(null));assertEquals("en",CourseController.language("en"));assertEquals("sq",CourseController.language("fr"));}
 @Test void eligibilityList(){assertTrue(CourseController.contains("Physician,Nurse","Nurse"));assertFalse(CourseController.contains("Medicine,Pharmacy","Dentistry"));assertTrue(CourseController.contains("*","Anything"));}
}
