package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionSanitizerTest {

    @Test
    void preservesExceptionWithNullMessage() {
        Exception sanitized = ExceptionSanitizer.sanitize(new Exception());

        assertNull(sanitized.getMessage());
    }

    @Test
    void preservesCyclicCauseReferences() {
        Exception first = new Exception("first");
        Exception second = new Exception("second");
        first.initCause(second);
        second.initCause(first);

        Exception sanitized = ExceptionSanitizer.sanitize(first);

        assertTrue(sanitized.getCause().getCause() == sanitized);
    }

    @Test
    void redactsCredentialParametersAndSharedKeysFromExceptionTree() {
        Exception exception = new Exception(
            "Blob request failed: azure-storage-blob://account/archive?credentials=super-secret&operation=upload"
                + "&sig=sas-signature",
            new IllegalStateException("Request failed with SharedKey account:shared-key")
        );
        exception.addSuppressed(new IllegalArgumentException("Invalid URI ?accountKey=another-secret"));

        Exception sanitized = ExceptionSanitizer.sanitize(exception);
        String stackTrace = ExceptionUtils.getStackTrace(sanitized);

        assertTrue(sanitized.getMessage().contains("credentials=[REDACTED]"));
        assertTrue(sanitized.getMessage().contains("operation=upload"));
        assertFalse(stackTrace.contains("super-secret"));
        assertFalse(stackTrace.contains("sas-signature"));
        assertFalse(stackTrace.contains("shared-key"));
        assertFalse(stackTrace.contains("another-secret"));
        assertTrue(stackTrace.contains("credentials=[REDACTED]"));
        assertTrue(stackTrace.contains("SharedKey [REDACTED]"));
        assertTrue(stackTrace.contains("accountKey=[REDACTED]"));
    }
}
