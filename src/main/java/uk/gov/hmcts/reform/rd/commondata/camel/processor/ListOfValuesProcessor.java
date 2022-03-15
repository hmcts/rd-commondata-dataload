package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.ListOfValues;

@Component
@Slf4j
public class ListOfValuesProcessor extends JsrValidationBaseProcessor<ListOfValues> {


    @Override
    public void process(Exchange exchange) throws Exception {
      // There is no need to do any validation and
       //  filteration here just starightforward to load the values from csv in azure portal to database

    }
}
