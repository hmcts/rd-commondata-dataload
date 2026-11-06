package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.FileResponseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.FileStatus;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IAuditService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CredentialRedactingExceptionProcessorTest {

    @Test
    void redactsExceptionBeforeLoggingAndAuditing() throws Exception {
        var camelContext = new DefaultCamelContext();
        var applicationContext = new GenericApplicationContext();
        var auditService = mock(IAuditService.class);
        var fileResponseProcessor = mock(FileResponseProcessor.class);
        var processor = new CredentialRedactingExceptionProcessor();
        var fileStatus = FileStatus.builder().fileName("data.csv").build();
        var routeProperties = new RouteProperties();
        routeProperties.setFileName("data.csv");
        routeProperties.setTableName("data_table");
        var exchange = new DefaultExchange(camelContext);
        var exception = new IllegalStateException(
            "Blob read failed for ?credentials=secret-token&operation=read"
        );
        camelContext.getRegistry().bind("data.csv", fileStatus);
        exchange.getIn().setHeader("routedetails", routeProperties);
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);

        ReflectionTestUtils.setField(processor, "camelContext", camelContext);
        ReflectionTestUtils.setField(processor, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(processor, "auditService", auditService);
        ReflectionTestUtils.setField(processor, "fileResponseProcessor", fileResponseProcessor);

        processor.process(exchange);

        Exception sanitized = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertTrue(sanitized.getMessage().contains("credentials=[REDACTED]"));
        assertFalse(sanitized.getMessage().contains("secret-token"));
        verify(auditService).auditException(camelContext, sanitized.getMessage());
        verify(fileResponseProcessor).process(exchange);
    }
}
