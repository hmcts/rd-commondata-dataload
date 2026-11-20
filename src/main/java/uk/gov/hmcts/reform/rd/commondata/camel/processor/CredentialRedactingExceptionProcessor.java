package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import org.apache.camel.Exchange;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;

@Component
@Primary
public class CredentialRedactingExceptionProcessor extends ExceptionProcessor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, ExceptionSanitizer.sanitize(exception));
        super.process(exchange);
    }
}
