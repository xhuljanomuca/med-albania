package org.medlearn.cert;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class CertificatePolicyTest {
 @Test void exactlySeventyPasses(){assertTrue(CertificatePolicy.eligible(true,true,true,70,70));}
 @Test void sixtyNineFails(){assertFalse(CertificatePolicy.eligible(true,true,true,69,70));}
 @Test void chapterQuizCannotIssue(){assertFalse(CertificatePolicy.eligible(false,true,true,100,70));}
 @Test void missingSubmissionCannotIssue(){assertFalse(CertificatePolicy.eligible(true,false,true,100,70));}
 @Test void verificationCodesUnique(){assertNotEquals(CertificatePolicy.code(),CertificatePolicy.code());}
}
