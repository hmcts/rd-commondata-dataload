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
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.DATE_TIME_FORMAT;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

@Component
@Slf4j
public class FlagDetailsProcessor extends JsrValidationBaseProcessor<FlagDetails>
    implements IFlagCodeProcessor<FlagDetails> {

    public static final String EXPIRED_DATE = "MRD_Deleted_Time";
    public static final String EXPIRED_DATE_ERROR_MSG = "Expired MRD delete date";

    @Autowired
    JsrValidatorInitializer<FlagDetails> flagDetailsJsrValidatorInitializer;

    @Value("${logging-component-name}")
    String logComponentName;


    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {

        var flagDetails = exchange.getIn().getBody() instanceof List
            ? (List<FlagDetails>) exchange.getIn().getBody()
            : singletonList((FlagDetails) exchange.getIn().getBody());

        log.info("{}:: Number of Records before validation {}::",
                 logComponentName, flagDetails.size()
        );

        List<FlagDetails> filteredFlagDetails = removeExpiredRecords(flagDetails, exchange);

        var validatedFlagDetails = validate(
            flagDetailsJsrValidatorInitializer,
            filteredFlagDetails
        );

        var jsrValidatedFlagDetails = validatedFlagDetails.size();
        log.info("{}:: Number of Records after applying the JSR validator are {}::",
                 logComponentName, jsrValidatedFlagDetails
        );

        audit(flagDetailsJsrValidatorInitializer, exchange);

        if (validatedFlagDetails.isEmpty()) {
            log.error("{}:: No valid Flag Details Record is found in the input file::", logComponentName);
            throw new RouteFailedException("No valid Flag Details Record found in the input file. "
                                               + "Please review and try again.");
        }

        if (filteredFlagDetails.size() != jsrValidatedFlagDetails) {
            setFileStatus(exchange, applicationContext, PARTIAL_SUCCESS);
        }

        exchange.getMessage().setBody(validatedFlagDetails);
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
            .filter(flagDetail -> {
                try {
                    return StringUtils.isNotBlank(flagDetail.getMrdDeletedTime())
                        && isDateExpired(flagDetail.getMrdDeletedTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());

        return List.copyOf(flagDetailsWithExpiredDates);
    }

    private boolean isDateExpired(String dateString) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date dt = dateFormatter.parse(dateString);

        return dt.compareTo(new Date()) <= 0;
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

            flagDetailsJsrValidatorInitializer.auditJsrExceptions(expiredDetailsList, EXPIRED_DATE,
                                                                  EXPIRED_DATE_ERROR_MSG, exchange
            );

            setFileStatus(exchange, applicationContext, PARTIAL_SUCCESS);
        }
    }

}
