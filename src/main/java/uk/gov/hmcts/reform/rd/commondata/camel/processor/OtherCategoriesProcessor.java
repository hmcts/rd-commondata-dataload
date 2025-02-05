package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_Y;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

@Component
@Slf4j
public class OtherCategoriesProcessor extends JsrValidationBaseProcessor<OtherCategories> {

    @Value("${logging-component-name}")
    private String logComponentName;

    @Autowired
    JsrValidatorInitializer<OtherCategories> lovServiceJsrValidatorInitializer;
    public static final String LOV_COMPOSITE_KEY = "categorykey,key,serviceid";
    public static final String LOV_COMPOSITE_KEY_ERROR_MSG = "Composite Key violation";

    private DataQualityCheckConfiguration dataQualityCheckConfiguration;

    public static final String ZERO_BYTE_CHARACTER_ERROR_MESSAGE =
        "Zero byte characters identified - check source file";

    public OtherCategoriesProcessor(DataQualityCheckConfiguration dataQualityCheckConfiguration) {
        this.dataQualityCheckConfiguration = dataQualityCheckConfiguration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) {

        var otherCategoriesListategoriesList = (exchange.getIn().getBody() instanceof List)
            ? (List<OtherCategories>) exchange.getIn().getBody()
            : singletonList((OtherCategories) exchange.getIn().getBody());

        var finalCategoriesList = validate(otherCategoriesListategoriesList, exchange);

        List<Pair<String, Long>> zeroByteCharacterRecords = new ArrayList<>();
        if (otherCategoriesListategoriesList != null && !otherCategoriesListategoriesList.isEmpty()) {
            //validation to check if there are any zerobyte characters
            zeroByteCharacterRecords = dataQualityCheckConfiguration.processExceptionRecords(
                singletonList(otherCategoriesListategoriesList));
        }

        if (!zeroByteCharacterRecords.isEmpty()) {
            List<Pair<String, Long>> distinctZeroByteCharacterRecords = zeroByteCharacterRecords.stream()
                .distinct().toList();
            audit(distinctZeroByteCharacterRecords,null, exchange, ZERO_BYTE_CHARACTER_ERROR_MESSAGE);
        }

        List<OtherCategories> invalidCategories = getInvalidCategories(otherCategoriesListategoriesList,
            finalCategoriesList);
        List<Pair<String, Long>> invalidCategoryIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(invalidCategories)) {
            invalidCategories.forEach(categories -> invalidCategoryIds.add(Pair.of(categories.getKey(),
                categories.getRowId())));
            audit(invalidCategoryIds, LOV_COMPOSITE_KEY,exchange, LOV_COMPOSITE_KEY_ERROR_MSG);
        }
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        exchange.getContext().getGlobalOptions().put(FILE_NAME, routeProperties.getFileName());
        exchange.getMessage().setBody(finalCategoriesList);

    }

    public void audit(List<Pair<String, Long>> invalidCategoryIds,String fieldInError,
                          Exchange exchange,String message) {
        if (!invalidCategoryIds.isEmpty()) {
            setFileStatus(exchange, applicationContext, FAILURE);
            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                invalidCategoryIds, fieldInError,
                message, exchange);
        }
    }

    private  List<OtherCategories> validate(List<OtherCategories> otherCategoriesListategoriesList,Exchange exchange) {

        log.info(" {} Categories Records count before Validation {}::", logComponentName,
            otherCategoriesListategoriesList.size()
        );

        Multimap<String, OtherCategories> filteredCategories = convertToMultiMap(otherCategoriesListategoriesList);
        List<OtherCategories> finalCategoriesList = getValidCategories(filteredCategories);

        log.info(" {} Categories Records count after Validation {}::", logComponentName,
            finalCategoriesList.size()
        );

        if (otherCategoriesListategoriesList.size() != finalCategoriesList.size()) {
            String auditStatus = PARTIAL_SUCCESS;
            if (finalCategoriesList.isEmpty()) {
                auditStatus = FAILURE;
            }
            setFileStatus(exchange, applicationContext, auditStatus);
        }

        return finalCategoriesList;
    }


    private List<OtherCategories> getInvalidCategories(List<OtherCategories> orgCategoryList,
                                                  List<OtherCategories> finalCategoriesList) {
        List<OtherCategories> invalidCategories = new ArrayList<>(orgCategoryList);

        invalidCategories.removeAll(finalCategoriesList);

        return invalidCategories;
    }

    private List<OtherCategories> getValidCategories(Multimap<String, OtherCategories> multimap) {

        List<OtherCategories> finalCategories = new ArrayList<>();
        multimap.asMap().forEach((key, collection) -> {
            List<OtherCategories> categoriesList = collection.stream().toList();
            if (categoriesList.size() > 1) {
                finalCategories.addAll(filterInvalidCategories(categoriesList));
            } else {
                finalCategories.addAll(categoriesList);
            }
        });
        return finalCategories;
    }

    private Multimap<String, OtherCategories> convertToMultiMap(List<OtherCategories> categoriesList) {
        Multimap<String, OtherCategories> multimap = ArrayListMultimap.create();
        categoriesList.forEach(categories -> multimap.put(categories.getCategoryKey() + categories.getServiceId()
                                                              + categories.getKey(), categories));
        return multimap;
    }

    private List<OtherCategories> filterInvalidCategories(List<OtherCategories> categoriesList) {
        List<OtherCategories> validCategories = new ArrayList<>();

        boolean activeProcessed = false;

        for (OtherCategories category : categoriesList) {
            if ((ACTIVE_Y.equalsIgnoreCase(category.getActive())
                && !activeProcessed)) {
                validCategories.add(category);
                activeProcessed = true;
            }
        }
        return validCategories;
    }
}

