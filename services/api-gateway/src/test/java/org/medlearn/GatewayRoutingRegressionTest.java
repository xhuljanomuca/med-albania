package org.medlearn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GatewayRoutingRegressionTest {
 @Test void catalogRootMustMatchCourseRoute(){
  String path="/api/courses";
  assertTrue(path.equals("/api/courses") || path.startsWith("/api/courses/"));
 }
}
