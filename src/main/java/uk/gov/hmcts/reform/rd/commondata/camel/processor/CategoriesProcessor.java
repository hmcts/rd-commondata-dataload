package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_FLAG_D;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_Y;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;


@Component
@Slf4j
public class CategoriesProcessor extends JsrValidationBaseProcessor<Categories> {

    @Value("${logging-component-name}")
    private String logComponentName;
    @Autowired
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer;
    @Autowired
    DataQualityCheckConfiguration dataQualityCheckConfiguration;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String LOV_COMPOSITE_KEY = "categorykey,key,serviceid";
    public static final String LOV_COMPOSITE_KEY_ERROR_MSG = "Composite Key violation";

    public static final String EXTERNAL_REFERENCE_ERROR_MSG = "Both external_reference_type and "
        + "external_reference_type value must be null or both must be not-null";

    public static final String ZERO_BYTE_CHARACTER_ERROR_MESSAGE =
        "Zero byte characters identified - check source file";

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) {

        var categoriesList = (exchange.getIn().getBody() instanceof List)
            ? (List<Categories>) exchange.getIn().getBody()
            : singletonList((Categories) exchange.getIn().getBody());

        log.info(" {} Categories Records count before Validation {}::", logComponentName,
                 categoriesList.size()
        );

        Multimap<String, Categories> filteredCategories = convertToMultiMap(categoriesList);
        List<Categories> finalCategoriesList = getValidCategories(filteredCategories);

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

        List<Pair<String, Long>> zeroByteCharacterRecords = new ArrayList<>();
        List<Categories> categoriesWithException = new ArrayList<>();
        if (finalCategoriesList != null && !finalCategoriesList.isEmpty()) {
            //validation to check if there are any zerobyte characters
            zeroByteCharacterRecords = dataQualityCheckConfiguration.processExceptionRecords(
                exchange, singletonList(finalCategoriesList),
                    applicationContext, lovServiceJsrValidatorInitializer);
            //validation for external reference fields
            categoriesWithException = validateExternalReference(finalCategoriesList);

        }
        //find invalid / inactive records and audit
        var routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        exchange.getContext().getGlobalOptions().put(FILE_NAME, routeProperties.getFileName());
        processException(exchange, categoriesList, finalCategoriesList);

        if (!zeroByteCharacterRecords.isEmpty()) {
            List<Pair<String, Long>> distinctZeroByteCharacterRecords = zeroByteCharacterRecords.stream()
                .distinct().collect(Collectors.toList());
            audit(distinctZeroByteCharacterRecords, null, exchange, ZERO_BYTE_CHARACTER_ERROR_MESSAGE);
        } else if (!categoriesWithException.isEmpty()) {
            List<Categories> existingDataFromTablelist = dataQualityCheckConfiguration
                .getExistingListFromTable(jdbcTemplate);
            List<Pair<String, Long>> invalidCategoryIds = categoriesWithException.stream()
                .map(categories -> createExceptionRecordPair(categories)).toList();
            audit(invalidCategoryIds, LOV_COMPOSITE_KEY, exchange,
                EXTERNAL_REFERENCE_ERROR_MSG);
            exchange.getMessage().setBody(existingDataFromTablelist);
        } else {
            exchange.getMessage().setBody(finalCategoriesList);
        }
    }

    public <T> void audit(List<Pair<String, Long>> invalidCategoryIds,
                          String fieldError,Exchange exchange,String message) {
        if (!invalidCategoryIds.isEmpty()) {
            setFileStatus(exchange, applicationContext, FAILURE);
            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                invalidCategoryIds, fieldError,
                message, exchange);
        }
    }


    private void processException(Exchange exchange,
                                         List<Categories> categoriesList,
                                         List<Categories> finalCategoriesList) {


        List<Categories> invalidCategories = getInvalidCategories(categoriesList, finalCategoriesList);
        List<Pair<String, Long>> invalidCategoryIds = invalidCategories.stream()
            .map(categories -> createExceptionRecordPair(categories)).toList();
        if (!invalidCategoryIds.isEmpty()) {
            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                invalidCategoryIds,
                LOV_COMPOSITE_KEY,
                LOV_COMPOSITE_KEY_ERROR_MSG,
                exchange
            );
        }
    }

    private List<Categories> validateExternalReference(List<Categories> finalCategoriesList) {
        List<Categories> invalidCategories = new LinkedList<>();
        for (Categories category : finalCategoriesList) {
            if ((category.getExternalReference() != null & category.getExternalReferenceType() != null)
                && (
                ((!category.getExternalReference().isEmpty()
                && category.getExternalReferenceType().isEmpty()))
                || (category.getExternalReference().isEmpty()
                    && !category.getExternalReferenceType().isEmpty())
                )) {
                invalidCategories.add(category);
            }
        }
        return invalidCategories;
    }


    private Pair<String,Long> createExceptionRecordPair(Categories category) {
        return Pair.of(
            category.getKey(),
            category.getRowId()
        );
    }


    private List<Categories> getInvalidCategories(List<Categories> orgCategoryList,
                                                  List<Categories> finalCategoriesList) {
        List<Categories> invalidCategories = new ArrayList<>(orgCategoryList);

        invalidCategories.removeAll(finalCategoriesList);

        return invalidCategories;
    }

    private List<Categories> getValidCategories(Multimap<String, Categories> multimap) {

        List<Categories> finalCategories = new ArrayList<>();
        multimap.asMap().forEach((key, collection) -> {
            List<Categories> categoriesList = collection.stream().toList();
            finalCategories.addAll(filterCategories(categoriesList));
        });
        return finalCategories;
    }

    private Multimap<String, Categories> convertToMultiMap(List<Categories> categoriesList) {
        Multimap<String, Categories> multimap = ArrayListMultimap.create();
        categoriesList.forEach(categories -> multimap.put(categories.getCategoryKey() + categories.getServiceId()
                                                              + categories.getKey(), categories));
        return multimap;
    }

    private List<Categories> filterCategories(List<Categories> categoriesList) {
        List<Categories> validCategories = new LinkedList<>();
        List<Categories> deletedCategories = new LinkedList<>();

        for (Categories category : categoriesList) {
            if ((ACTIVE_Y.equalsIgnoreCase(category.getActive()))) {
                validCategories.add(category);
            }
        }

        if (validCategories.size() == 0) {
            for (Categories category : categoriesList) {
                if ((ACTIVE_FLAG_D.equalsIgnoreCase(category.getActive()))) {
                    deletedCategories.add(category);
                    break;
                }
            }
        }
        validCategories.addAll(deletedCategories);
        return validCategories;
    }
}
