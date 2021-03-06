package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

@Component
@Slf4j
public class CategoriesProcessor extends JsrValidationBaseProcessor<Categories> {



    @Override
    public void process(Exchange exchange) {
      // There is no need to do any validation and
       //  filteration here just starightforward to load the values from csv in azure portal to database

    }
}
