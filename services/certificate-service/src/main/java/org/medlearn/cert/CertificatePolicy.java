package org.medlearn.cert;

import java.util.*;

public final class CertificatePolicy {
    private CertificatePolicy() {
    }

    public static boolean eligible(boolean finalExam, boolean submitted, boolean passed, int score, int threshold) {
        return finalExam && submitted && passed && threshold >= 1 && threshold <= 100 && score >= threshold;
    }

    public static String code() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }
}
