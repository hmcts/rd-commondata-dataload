package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.getDateTimeStamp;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

@Component
@Slf4j
public class FlagDetailsProcessor extends JsrValidationBaseProcessor<FlagDetails>
    implements IFlagCodeProcessor<FlagDetails> {

    public static final String EXPIRED_DATE_ERROR_MSG = "Record is expired";


    @Autowired
    JsrValidatorInitializer<FlagDetails> flagDetailsJsrValidatorInitializer;

    @Autowired
    DataQualityCheckConfiguration dataQualityCheckConfiguration;

    @Value("${logging-component-name}")
    String logComponentName;


    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {

        var flagDetailsList = exchange.getIn().getBody() instanceof List
            ? (List<FlagDetails>) exchange.getIn().getBody()
            : singletonList((FlagDetails) exchange.getIn().getBody());

        log.info("{}:: Number of Records before validation {}::",
                 logComponentName, flagDetailsList.size()
        );

        var validatedFlagDetails = validate(
            flagDetailsJsrValidatorInitializer,
            flagDetailsList
        );

        var jsrValidatedFlagDetails = validatedFlagDetails.size();
        log.info("{}:: Number of Records after applying the JSR validator are {}::",
                 logComponentName, jsrValidatedFlagDetails
        );

        var filteredFlagDetails = removeExpiredRecords(validatedFlagDetails, exchange);

        audit(flagDetailsJsrValidatorInitializer, exchange);

        if (validatedFlagDetails.isEmpty() || filteredFlagDetails.isEmpty()) {
            log.error("{}:: No valid Flag Details Record is found in the input file::", logComponentName);
            throw new RouteFailedException("No valid Flag Details Record found in the input file. "
                                               + "Please review and try again.");
        }

        if (flagDetailsList.size() != jsrValidatedFlagDetails) {
            setFileStatus(exchange, applicationContext, PARTIAL_SUCCESS);
        }

        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);

        if (!flagDetailsList.isEmpty()) {
            dataQualityCheckConfiguration.processExceptionRecords(exchange, singletonList(flagDetailsList),
                applicationContext, flagDetailsJsrValidatorInitializer);
        }

        exchange.getContext().getGlobalOptions().put(FILE_NAME, routeProperties.getFileName());
        exchange.getMessage().setBody(filteredFlagDetails);


    }

    public List<FlagDetails> removeExpiredRecords(List<FlagDetails> flagDetailsList,
                                                  Exchange exchange) {

        var flagDetails = new ArrayList<>(flagDetailsList);

        var expiredRecords = getExpiredRecords(flagDetails);

        if (!CollectionUtils.isEmpty(expiredRecords)) {
            remove(expiredRecords, flagDetails);

            auditRecord(expiredRecords, exchange);
        }

        return List.copyOf(flagDetails);
    }

    private List<FlagDetails> getExpiredRecords(List<FlagDetails> flagDetails) {

        var flagDetailsWithExpiredDates = flagDetails.stream()
            .filter(flagDetail -> StringUtils.isNotBlank(flagDetail.getMrdDeletedTime())
            && isDateExpired(flagDetail.getMrdDeletedTime()))
            .toList();

        return List.copyOf(flagDetailsWithExpiredDates);
    }

    private boolean isDateExpired(String dateString) {

        Timestamp ts = getDateTimeStamp(dateString);
        if (nonNull(ts)) {
            return ts.compareTo(Timestamp.valueOf(LocalDateTime.now())) <= 0;
        }

        return false;
    }

    public void remove(List<FlagDetails> flagDetailsToBeDeleted, List<FlagDetails> flagDetails) {
        var flagDetailsDeleted = new HashSet<>(flagDetailsToBeDeleted);
        flagDetails.removeIf(flagDetailsDeleted::contains);
    }

    public void auditRecord(List<FlagDetails> expiredFlagDetailsList, Exchange exchange) {
        if (nonNull(expiredFlagDetailsList) && !CollectionUtils.isEmpty(expiredFlagDetailsList)) {

            List<Pair<String, Long>> expiredDetailsList = new ArrayList<>();
            expiredFlagDetailsList.forEach(expiredDetails -> expiredDetailsList.add(Pair.of(
                expiredDetails.getFlagCode(),
                expiredDetails.getRowId()
            )));

            flagDetailsJsrValidatorInitializer.auditJsrExceptions(expiredDetailsList, "",
                                                                  EXPIRED_DATE_ERROR_MSG, exchange
            );

            setFileStatus(exchange, applicationContext, PARTIAL_SUCCESS);
        }
    }

}
