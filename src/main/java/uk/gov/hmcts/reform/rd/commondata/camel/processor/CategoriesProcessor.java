package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IAuditService;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FLAG_CODE;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FLAG_CODE_NOT_EXISTS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.checkIfValueNotInListIfPresent;

@Component
@Slf4j
public class CategoriesProcessor extends JsrValidationBaseProcessor<Categories>
    implements IFlagCodeProcessor<Categories>{

    @Autowired
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer;

    @Value("${logging-component-name}")
    String logComponentName;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${flag-code-query}")
    String flagCodeQuery;

    @Value("${list-of-values-compkey-query}")
    String lovComKeyQuery;


    /**
     * validate CSV file records.
     *
     * @param exchange exchange Obj
     */
    @Override
    @SuppressWarnings("unchecked")

    public void process(Exchange exchange) {



        var lovServices = exchange.getIn().getBody() instanceof List
            ? (List<Categories>) exchange.getIn().getBody()
            : singletonList((Categories) exchange.getIn().getBody());

        log.info("{}:: Number of Records before validation {}::",
                 logComponentName, lovServices.size()
        );


        var validatedLovServices = validate(
            lovServiceJsrValidatorInitializer,
            lovServices
        );

        var jsrValidatedLovServices = validatedLovServices.size();
        log.info("{}:: Number of Records after applying the JSR validator are {}::",
                 logComponentName, jsrValidatedLovServices
        );
//TDOD its not required
       // filterFlagServiceForForeignKeyViolations(validatedFlagServices, exchange);

        audit(lovServiceJsrValidatorInitializer, exchange);

        if (validatedLovServices.isEmpty()) {
            log.error("{}:: No valid Flag Service Record is found in the input file::", logComponentName);
            throw new RouteFailedException("No valid Flag Service Record found in the input file. "
                                               + "Please review and try again.");
        }

        if (validatedLovServices.size() != jsrValidatedLovServices) {
            setFileStatus(exchange, applicationContext);
        }

        exchange.getMessage().setBody(validatedLovServices);
    }

    /**
     * Filter if Primary Key is not present in the parent table.
     *
     * @param validatedFlagServices list of Validated records after jsr validation
     * @param exchange              exchange obj
     */
 /*   private void filterFlagServiceForForeignKeyViolations(List<Categories> validatedFlagServices,
                                                          Exchange exchange) {

        if (isNotEmpty(validatedFlagServices)) {
            var flagCodeList = getIdList(jdbcTemplate, flagCodeQuery);
            checkForeignKeyConstraint(
                validatedFlagServices,
                flagCodeRecord -> checkIfValueNotInListIfPresent(flagCodeRecord.getFlagCode().trim(), flagCodeList),
                FLAG_CODE, FLAG_CODE_NOT_EXISTS,
                exchange, flagServiceJsrValidatorInitializer
            );
        }
    }*/
}
