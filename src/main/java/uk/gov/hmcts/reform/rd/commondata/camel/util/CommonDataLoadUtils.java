package uk.gov.hmcts.reform.rd.commondata.camel.util;

import org.apache.camel.Exchange;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

public class CommonDataLoadUtils {

    private CommonDataLoadUtils() {

    }

    /**
     * Check id is present in Parent table or not.
     *
     * @param id          id
     * @param knownIdList knownIdList
     * @return true if not present in the list
     */
    public static boolean checkIfValueNotInListIfPresent(String id, List<String> knownIdList) {
        return (isNotEmpty(id)) ? isFalse(knownIdList.contains(id)) : Boolean.FALSE;
    }

    /**
     * Check for Foregin Key.
     *
     * @param domainObjects List of Records after jsr validation
     * @param predicate     Function to compare Parent table record
     * @return Return List of unmatched Record.
     */
    public static <T> List<T> filterDomainObjects(List<T> domainObjects, Predicate<T> predicate) {
        return domainObjects.stream()
            .filter(predicate).collect(Collectors.toList());
    }

    public static void setFileStatus(Exchange exchange, ApplicationContext applicationContext, String auditStatus) {
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        var fileStatus = getFileDetails(exchange.getContext(), routeProperties.getFileName());
        fileStatus.setAuditStatus(auditStatus);
        registerFileStatusBean(applicationContext, routeProperties.getFileName(), fileStatus,
                               exchange.getContext()
        );
    }
}
