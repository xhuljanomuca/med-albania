package org.medlearn;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.junit.jupiter.api.Assertions.*;
class InstructorAnalyticsSecurityTest {
 @Test void instructorRoutesHaveSeparatePrefix(){
  assertEquals("/api/courses/instructor",InstructorAnalyticsController.class.getAnnotation(RequestMapping.class).value()[0]);
 }
}
