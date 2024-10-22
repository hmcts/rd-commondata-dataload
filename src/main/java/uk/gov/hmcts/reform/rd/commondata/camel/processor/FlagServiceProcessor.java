package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FLAG_CODE;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FLAG_CODE_NOT_EXISTS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.checkIfValueNotInListIfPresent;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

@Component
@Slf4j
public class FlagServiceProcessor extends JsrValidationBaseProcessor<FlagService>
    implements IFlagCodeProcessor<FlagService> {

    @Autowired
    JsrValidatorInitializer<FlagService> flagServiceJsrValidatorInitializer;

    @Value("${logging-component-name}")
    String logComponentName;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${flag-code-query}")
    String flagCodeQuery;

    @Autowired
    DataQualityCheckConfiguration dataQualityCheckConfiguration;

    /**
     * validate CSV file records.
     *
     * @param exchange exchange Obj
     */
    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {

        var  flagServices = exchange.getIn().getBody() instanceof List
            ? (List<FlagService>) exchange.getIn().getBody()
            : singletonList((FlagService) exchange.getIn().getBody());

        log.info("{}:: Number of Records before validation {}::",
                 logComponentName, flagServices.size()
        );

        var validatedFlagServices = validate(
            flagServiceJsrValidatorInitializer,
            flagServices
        );

        var jsrValidatedFlagServices = validatedFlagServices.size();
        log.info("{}:: Number of Records after applying the JSR validator are {}::",
                 logComponentName, jsrValidatedFlagServices
        );

        filterFlagServiceForForeignKeyViolations(validatedFlagServices, exchange);

        audit(flagServiceJsrValidatorInitializer, exchange);

        if (validatedFlagServices.isEmpty()) {
            log.error("{}:: No valid Flag Service Record is found in the input file::", logComponentName);
            throw new RouteFailedException("No valid Flag Service Record found in the input file. "
                                               + "Please review and try again.");
        }

        if (validatedFlagServices.size() != jsrValidatedFlagServices) {
            setFileStatus(exchange, applicationContext, PARTIAL_SUCCESS);
        }

        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);

        if (!flagServices.isEmpty()) {
            dataQualityCheckConfiguration.processExceptionRecords(exchange, singletonList(flagServices),
                applicationContext, flagServiceJsrValidatorInitializer);
        }
        exchange.getContext().getGlobalOptions().put(FILE_NAME, routeProperties.getFileName());
        exchange.getMessage().setBody(validatedFlagServices);

    }


    /**
     * Filter if Primary Key is not present in the parent table.
     *
     * @param validatedFlagServices list of Validated records after jsr validation
     * @param exchange              exchange obj
     */
    private void filterFlagServiceForForeignKeyViolations(List<FlagService> validatedFlagServices,
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
    }
}
