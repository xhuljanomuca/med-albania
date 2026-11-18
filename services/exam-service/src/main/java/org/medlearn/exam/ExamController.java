package org.medlearn.exam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;import java.util.*;import org.springframework.jdbc.core.JdbcTemplate;
@RestController @RequestMapping("/api/exams") public class ExamController {
 private final QuizRepository quizzes;private final QuestionRepository questions;private final AttemptRepository attempts;private final AnswerRepository answers;
 private final RestClient http=RestClient.create();private final ServiceTokenProvider serviceTokens;private final ProgressOutboxRepository outbox;private final JdbcTemplate locks;
 @Value("${services.courses}") String courses;
 public ExamController(QuizRepository q,QuestionRepository n,AttemptRepository a,AnswerRepository b,ServiceTokenProvider serviceTokens,ProgressOutboxRepository outbox,JdbcTemplate locks){this.locks=locks;this.outbox=outbox;this.serviceTokens=serviceTokens;quizzes=q;questions=n;attempts=a;answers=b;}
 public record QuizInput(UUID courseId,UUID chapterId,String titleSq,String titleEn,Integer passingPercent,Integer timeLimitMinutes,Integer maxAttempts,boolean randomize){}
 public record QuestionInput(int position,String promptSq,String promptEn,List<String> choicesSq,List<String> choicesEn,int correctIndex,String explanationSq,String explanationEn){}
 public record PublicQuestion(UUID id,int position,String prompt,List<String> choices){}
 public record StartResult(UUID attemptId,Instant expiresAt,List<PublicQuestion> questions){}
 public record Submission(Map<UUID,Integer> answers){}
 public record GradeResult(UUID attemptId,int scorePercent,boolean passed,int correct,int total){}
 static ResponseStatusException forbidden(){return new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied");}
 static String locale(String value){return value!=null&&value.toLowerCase(Locale.ROOT).startsWith("en")?"en":"sq";}
 static int score(int correct,int total){return total==0?0:(int)Math.round(100.0*correct/total);}
 private UUID user(JwtAuthenticationToken auth){return UUID.fromString(auth.getName());}
 private Quiz quiz(UUID id){return quizzes.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private void ensureAccess(Quiz quiz,JwtAuthenticationToken auth){
  try{
   if(quiz.chapterId!=null){http.get().uri(courses+"/api/courses/chapters/"+quiz.chapterId).header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().toBodilessEntity();}
   else {
    Map<?,?> learning=http.get().uri(courses+"/api/courses/"+quiz.courseId+"/learn").header("Authorization","Bearer "+auth.getToken().getTokenValue()).retrieve().body(Map.class);
    Map<?,?> completion=http.get().uri(courses+"/api/courses/internal/courses/"+quiz.courseId+"/completion/"+user(auth)).headers(h -> h.setBearerAuth(serviceTokens.token())).retrieve().body(Map.class);
    if(learning==null||completion==null||!Boolean.TRUE.equals(completion.get("ready")))throw forbidden();
   }
  }catch(ResponseStatusException ex){throw ex;}catch(Exception ex){throw forbidden();}
 }
 @PostMapping("/admin/quizzes") public Quiz create(@RequestBody QuizInput input){
  if(input.courseId()==null||input.titleSq()==null||input.titleEn()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  Quiz q=new Quiz();q.id=UUID.randomUUID();q.courseId=input.courseId();q.chapterId=input.chapterId();q.titleSq=input.titleSq();q.titleEn=input.titleEn();q.passingPercent=input.passingPercent()==null?70:input.passingPercent();q.timeLimitMinutes=input.timeLimitMinutes()==null?30:input.timeLimitMinutes();q.maxAttempts=input.maxAttempts()==null?3:input.maxAttempts();q.randomize=input.randomize();
  if(q.passingPercent<1||q.passingPercent>100||q.timeLimitMinutes<1||q.maxAttempts<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  return quizzes.save(q);
 }
 @PostMapping("/admin/quizzes/{quizId}/questions") public Question addQuestion(@PathVariable UUID quizId,@RequestBody QuestionInput i){
  Quiz q=quiz(quizId);if(!"DRAFT".equals(q.status))throw new ResponseStatusException(HttpStatus.CONFLICT,"Published quizzes are immutable");
  if(i.choicesSq()==null||i.choicesSq().size()!=4||i.choicesEn()==null||i.choicesEn().size()!=4||i.correctIndex()<0||i.correctIndex()>3||i.position()<1||i.promptSq()==null||i.promptEn()==null||i.choicesSq().stream().anyMatch(String::isBlank)||i.choicesEn().stream().anyMatch(String::isBlank))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  Question n=new Question();n.id=UUID.randomUUID();n.quizId=quizId;n.position=i.position();n.promptSq=i.promptSq();n.promptEn=i.promptEn();n.choicesSq=String.join("\n",i.choicesSq());n.choicesEn=String.join("\n",i.choicesEn());n.correctIndex=i.correctIndex();n.explanationSq=i.explanationSq();n.explanationEn=i.explanationEn();return questions.save(n);
 }
 @PostMapping("/admin/quizzes/{quizId}/publish") public Quiz publish(@PathVariable UUID quizId){Quiz q=quiz(quizId);if(questions.findByQuizIdOrderByPositionAsc(quizId).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Add questions before publishing");q.status="PUBLISHED";return quizzes.save(q);}
 @GetMapping("/admin/quizzes/{quizId}/questions") public List<Question> adminQuestions(@PathVariable UUID quizId){quiz(quizId);return questions.findByQuizIdOrderByPositionAsc(quizId);}
 @GetMapping("/admin/analytics") public Map<String,Object> analytics(){var all=attempts.findAll().stream().filter(a->a.submittedAt!=null).toList();long passed=all.stream().filter(a->Boolean.TRUE.equals(a.passed)).count();return Map.of("attempts",all.size(),"passed",passed,"failed",all.size()-passed,"averageScore",all.stream().mapToInt(a->a.scorePercent).average().orElse(0));}
 @GetMapping("/course/{courseId}") public List<Map<String,Object>> courseQuizzes(@PathVariable UUID courseId,JwtAuthenticationToken auth){
  return quizzes.findByCourseId(courseId).stream().filter(q->"PUBLISHED".equals(q.status)).filter(q->{try{ensureAccess(q,auth);return true;}catch(Exception e){return false;}}).map(q->Map.<String,Object>of("id",q.id,"title",q.titleEn,"final",q.chapterId==null,"passingPercent",q.passingPercent)).toList();
 }
 @PostMapping("/{quizId}/start") @Transactional public StartResult start(@PathVariable UUID quizId,@RequestHeader(value="Accept-Language",required=false) String lang,JwtAuthenticationToken auth){
  Quiz q=quiz(quizId);if(!"PUBLISHED".equals(q.status))throw forbidden();ensureAccess(q,auth);UUID uid=user(auth);
  // Serialize attempt creation per quiz/learner across all application instances.
  locks.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))",Long.class,q.id+":"+uid);
  if(attempts.existsByQuizIdAndUserIdAndPassedTrue(q.id,uid))throw new ResponseStatusException(HttpStatus.CONFLICT,"Already passed");
  if(attempts.countByQuizIdAndUserId(q.id,uid)>=q.maxAttempts)throw new ResponseStatusException(HttpStatus.CONFLICT,"Attempt limit reached");
  List<Question> list=new ArrayList<>(questions.findByQuizIdOrderByPositionAsc(q.id));if(list.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT);
  if(q.randomize)Collections.shuffle(list,new java.security.SecureRandom());Attempt a=new Attempt();a.id=UUID.randomUUID();a.quizId=q.id;a.userId=uid;a.attemptNumber=(int)attempts.countByQuizIdAndUserId(q.id,uid)+1;a.startedAt=Instant.now();a.questionOrder=String.join(",",list.stream().map(n->n.id.toString()).toList());attempts.save(a);
  String language=locale(lang);return new StartResult(a.id,a.startedAt.plus(Duration.ofMinutes(q.timeLimitMinutes)),list.stream().map(n->new PublicQuestion(n.id,n.position,"en".equals(language)?n.promptEn:n.promptSq,List.of(("en".equals(language)?n.choicesEn:n.choicesSq).split("\n",-1)))).toList());
 }
 @PostMapping("/attempts/{attemptId}/submit") @Transactional public GradeResult submit(@PathVariable UUID attemptId,@RequestBody Submission submitted,JwtAuthenticationToken auth){
  Attempt a=attempts.findByIdForUpdate(attemptId).orElseThrow();if(!a.userId.equals(user(auth)))throw forbidden();if(a.submittedAt!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Already submitted");
  Quiz q=quiz(a.quizId);Instant now=Instant.now();if(now.isAfter(a.startedAt.plus(Duration.ofMinutes(q.timeLimitMinutes))))throw new ResponseStatusException(HttpStatus.CONFLICT,"Time limit exceeded");
  Map<UUID,Integer> supplied=submitted.answers()==null?Map.of():submitted.answers();List<UUID> order=Arrays.stream(a.questionOrder.split(",")).map(UUID::fromString).toList();
  if(!order.containsAll(supplied.keySet()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown question");
  int correct=0;for(UUID id:order){Question n=questions.findById(id).orElseThrow();Integer selected=supplied.get(id);if(selected!=null){if(selected<0||selected>3)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);Answer answer=new Answer();answer.attemptId=a.id;answer.questionId=id;answer.selectedIndex=selected;answer.correct=selected==n.correctIndex;answers.save(answer);if(answer.correct)correct++;}}
  a.scorePercent=score(correct,order.size());a.passed=a.scorePercent>=q.passingPercent;a.submittedAt=now;attempts.save(a);
  if(a.passed&&q.chapterId!=null){ProgressOutbox event=new ProgressOutbox();event.id=UUID.randomUUID();event.attemptId=a.id;event.chapterId=q.chapterId;event.userId=a.userId;event.createdAt=now;outbox.save(event);}
  return new GradeResult(a.id,a.scorePercent,a.passed,correct,order.size());
 }
 @GetMapping("/me/attempts") public List<Map<String,Object>> history(JwtAuthenticationToken auth){return attempts.findByUserIdOrderByStartedAtDesc(user(auth)).stream().map(a->{Map<String,Object> result=new LinkedHashMap<>();result.put("id",a.id);result.put("quizId",a.quizId);result.put("attemptNumber",a.attemptNumber);result.put("startedAt",a.startedAt);result.put("submittedAt",a.submittedAt);result.put("scorePercent",a.scorePercent);result.put("passed",a.passed);return result;}).toList();}
}
