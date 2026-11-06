package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;

final class ExceptionSanitizer {

    private static final String REDACTED_VALUE = "$1[REDACTED]";
    private static final Pattern CREDENTIAL_PARAMETER = Pattern.compile(
        "(?i)([?&](?:credentials?|password|sharedaccesssignature)=)[^\\s&\"'<>]*"
    );
    private static final Pattern TOKEN_PARAMETER = Pattern.compile(
        "(?i)([?&](?:account[-_]?key|sas(?:token)?|sig(?:nature)?)=)[^\\s&\"'<>]*"
    );
    private static final Pattern TOKEN_PARAMETER_ALT = Pattern.compile(
        "(?i)([?&](?:access[_-]?token|client[_-]?secret)=)[^\\s&\"'<>]*"
    );
    private static final Pattern SHARED_KEY = Pattern.compile("(?i)(SharedKey\\s+)[^\\s,;]+");

    private ExceptionSanitizer() {
    }

    static Exception sanitize(Exception exception) {
        return copy(exception, new IdentityHashMap<>());
    }

    private static Exception copy(Throwable original, Map<Throwable, Exception> sanitized) {
        Exception existing = sanitized.get(original);
        if (existing != null) {
            return existing;
        }

        Exception copy = new SanitizedException(original.getClass().getName(), redact(original.getMessage()));
        sanitized.put(original, copy);
        copy.setStackTrace(original.getStackTrace());

        Throwable cause = original.getCause();
        if (cause != null && cause != original) {
            Exception safeCause = copy(cause, sanitized);
            if (safeCause != copy) {
                copy.initCause(safeCause);
            }
        }

        for (Throwable suppressed : original.getSuppressed()) {
            Exception safeSuppressed = copy(suppressed, sanitized);
            if (safeSuppressed != copy) {
                copy.addSuppressed(safeSuppressed);
            }
        }

        return copy;
    }

    private static String redact(String message) {
        if (message == null) {
            return null;
        }
        String redacted = CREDENTIAL_PARAMETER.matcher(message).replaceAll(REDACTED_VALUE);
        redacted = TOKEN_PARAMETER.matcher(redacted).replaceAll(REDACTED_VALUE);
        redacted = TOKEN_PARAMETER_ALT.matcher(redacted).replaceAll(REDACTED_VALUE);
        return SHARED_KEY.matcher(redacted).replaceAll(REDACTED_VALUE);
    }

    private static final class SanitizedException extends Exception {
        private final String originalType;

        private SanitizedException(String originalType, String message) {
            super(message);
            this.originalType = originalType;
        }

        @Override
        public String toString() {
            String message = getLocalizedMessage();
            return message == null ? originalType : originalType + ": " + message;
        }
    }
}
