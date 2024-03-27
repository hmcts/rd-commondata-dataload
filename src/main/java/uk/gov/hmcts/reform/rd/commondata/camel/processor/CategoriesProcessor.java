package uk.gov.hmcts.reform.rd.commondata.camel.processor;


import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_FLAG_D;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_Y;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

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
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

@Component
@Slf4j
public class CategoriesProcessor extends JsrValidationBaseProcessor<Categories> {

    @Value("${logging-component-name}")
    private String logComponentName;
    @Autowired
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer;
    @Autowired
    DataQualityCheckConfiguration dataQualityCheckConfiguration;
    public static final String LOV_COMPOSITE_KEY = "categorykey,key,serviceid";
    public static final String LOV_COMPOSITE_KEY_ERROR_MSG = "Composite Key violation";
    public static final String ZERO_BYTE_CHARACTER_ERROR_MESSAGE =
        "Zero byte characters identified - check source file";

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) {

        List<Categories> categoriesList;

        categoriesList = (exchange.getIn().getBody() instanceof List)
            ? (List<Categories>) exchange.getIn().getBody()
            : singletonList((Categories) exchange.getIn().getBody());

        log.info(" {} Categories Records count before Validation {}::", logComponentName,
                 categoriesList.size()
        );

        Multimap<String, Categories> filteredCategories = convertToMultiMap(categoriesList,ACTIVE_Y);
        List<Categories> finalCategoriesList = getValidCategories(filteredCategories,ACTIVE_Y);

        Multimap<String, Categories> filteredInactiveCategories = convertToMultiMap(categoriesList,ACTIVE_FLAG_D);
        List<Categories> finalInvaliCategoriesList = getValidCategories(filteredInactiveCategories,ACTIVE_FLAG_D);

        List<Categories>  onlyDeletedNoActiveRecords = onlyDeletedNoActiveRecords(
            finalCategoriesList,finalInvaliCategoriesList);

        finalCategoriesList.addAll(onlyDeletedNoActiveRecords);

        log.info(" {} Categories Records count after Validation {}::", logComponentName,
                 finalCategoriesList.size()
        );

        if (categoriesList.size() != finalCategoriesList.size()) {
            String auditStatus = PARTIAL_SUCCESS;
            if (finalCategoriesList.isEmpty()) {
                auditStatus = FAILURE;
            }
            setFileStatus(exchange, applicationContext, auditStatus);
        }
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        exchange.getContext().getGlobalOptions().put(FILE_NAME, routeProperties.getFileName());
        exchange.getMessage().setBody(finalCategoriesList);

        processExceptionRecords(exchange, categoriesList, finalCategoriesList);
    }

    private void processExceptionRecords(Exchange exchange,
                                         List<Categories> categoriesList,
                                         List<Categories> finalCategoriesList) {

        List<Pair<String, Long>> zeroByteCharacterRecords = identifyRecordsWithZeroByteCharacters(categoriesList);
        if (!zeroByteCharacterRecords.isEmpty()) {
            String auditStatus = FAILURE;
            setFileStatus(exchange, applicationContext, auditStatus);

            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                zeroByteCharacterRecords,
                null,
                ZERO_BYTE_CHARACTER_ERROR_MESSAGE,
                exchange
            );
        }

        List<Categories> invalidCategories = getInvalidCategories(categoriesList, finalCategoriesList);
        List<Pair<String, Long>> invalidCategoryIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(invalidCategories)) {
            invalidCategories.forEach(categories -> invalidCategoryIds.add(
                createExceptionRecordPair(categories)
            ));

            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                invalidCategoryIds,
                LOV_COMPOSITE_KEY,
                LOV_COMPOSITE_KEY_ERROR_MSG,
                exchange
            );
        }
    }

    private Pair<String,Long> createExceptionRecordPair(Categories category) {
        return Pair.of(
            category.getKey(),
            category.getRowId()
        );
    }

    private List<Pair<String, Long>> identifyRecordsWithZeroByteCharacters(List<Categories> categoriesList) {

        return categoriesList.stream()
            .filter(category ->
                        checkStringForZeroByteCharacters(category.toString())
            )
            .map(
                this::createExceptionRecordPair
            )
            .toList();
    }

    private boolean checkStringForZeroByteCharacters(String string) {
        return dataQualityCheckConfiguration.zeroByteCharacters.stream()
            .anyMatch(
                string::contains
        );
    }

    private List<Categories> getInvalidCategories(List<Categories> orgCategoryList,
                                                  List<Categories> finalCategoriesList) {
        List<Categories> invalidCategories = new ArrayList<>(orgCategoryList);

        invalidCategories.removeAll(finalCategoriesList);

        return invalidCategories;
    }

    private List<Categories> getValidCategories(Multimap<String, Categories> multimap,String activeFlag) {

        List<Categories> finalCategories = new ArrayList<>();
        multimap.asMap().forEach((key, collection) -> {
            List<Categories> categoriesList = collection.stream().toList();
            if (categoriesList.size() > 1) {
                finalCategories.addAll(filterCategories(categoriesList,activeFlag));
            } else {
                finalCategories.addAll(categoriesList);
            }
        });
        return finalCategories;
    }

    private Multimap<String, Categories> convertToMultiMap(List<Categories> categoriesList, String activeFlag) {
        Multimap<String, Categories> multimap = ArrayListMultimap.create();
        categoriesList.forEach(categories -> {
            if (categories.getActive().equalsIgnoreCase(activeFlag)) {
                multimap.put(categories.getCategoryKey() + categories.getServiceId()
                    + categories.getKey(), categories);
            }

        });
        return multimap;
    }

    private List<Categories> filterCategories(List<Categories> categoriesList,String activeFlag) {
        List<Categories> validCategories = new ArrayList<>();

        boolean activeProcessed = false;

        for (Categories category : categoriesList) {
            if ((activeFlag.equalsIgnoreCase(category.getActive())
                    && !activeProcessed)) {
                validCategories.add(category);
                activeProcessed = true;
            }
        }
        return validCategories;
    }

    private List<Categories> onlyDeletedNoActiveRecords(
        List<Categories> activecategoriesList,List<Categories> inactivecategoriesList) {

        List<Categories> activecategoriesList1 = new ArrayList<>();

        for (Categories category1:activecategoriesList) {
            for (Categories category:inactivecategoriesList) {
                if ((category1.getCategoryKey().equalsIgnoreCase(category.getCategoryKey()))
                    && (category1.getKey().equalsIgnoreCase(category.getKey()))
                    && (category1.getServiceId().equalsIgnoreCase(category.getServiceId()))) {
                    activecategoriesList1.add(category);
                }
            }
        }
        inactivecategoriesList.removeAll(activecategoriesList1);
        return inactivecategoriesList;
    }
}
