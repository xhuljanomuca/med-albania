package org.medlearn;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ChapterConcurrencyTest {
 @Test void duplicatePositionIsHttp409EvenAfterPrecheck(){
  CourseRepo courses=mock(CourseRepo.class);ChapterRepo chapters=mock(ChapterRepo.class);
  Course course=new Course();course.id=UUID.randomUUID();
  when(courses.findById(course.id)).thenReturn(Optional.of(course));
  when(chapters.findByCourseIdOrderByPositionAsc(course.id)).thenReturn(List.of());
  when(chapters.saveAndFlush(any(Chapter.class))).thenThrow(new DataIntegrityViolationException("chapter_course_id_position_key"));
  CourseController controller=new CourseController(courses,chapters,mock(ProgressRepo.class),mock(JdbcTemplate.class));
  var error=assertThrows(ResponseStatusException.class,()->controller.addChapter(course.id,new CourseController.ChapterInput(1,"Kapitulli","Chapter","Teksti","Body")));
  assertEquals(HttpStatus.CONFLICT,error.getStatusCode());
 }
}
