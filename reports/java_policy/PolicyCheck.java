import org.medlearn.cert.CertificatePolicy;
public class PolicyCheck {
  public static void main(String[] args) {
    require(CertificatePolicy.eligible(true,true,true,70,70), "passing score accepted");
    require(!CertificatePolicy.eligible(true,true,true,69,70), "failing score rejected");
    require(!CertificatePolicy.eligible(false,true,true,100,70), "chapter quiz not a final exam");
    require(!CertificatePolicy.eligible(true,false,true,100,70), "unsubmitted rejected");
    require(!CertificatePolicy.eligible(true,true,true,100,0), "invalid threshold rejected");
    require(CertificatePolicy.code().matches("[A-F0-9]{32}"), "verification format");
    System.out.println("PASS 6/6 Java certificate policy assertions");
  }
  static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
