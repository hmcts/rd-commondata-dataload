package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.JsrValidationBaseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.FileStatus;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil.getFileDetails;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil.registerFileStatusBean;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_Y;

@Component
@Slf4j
public class CategoriesProcessor extends JsrValidationBaseProcessor<Categories> {

    @Value("${logging-component-name}")
    private String logComponentName;

    @Autowired
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer;
    public static final String LOV_COMPOSITE_KEY = "categorykey,key,serviceid";
    public static final String LOV_COMPOSITE_KEY_ERROR_MSG = "Composite Key violation";


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
        exchange.getMessage().setBody(finalCategoriesList);

        List<Categories> invalidCategories = getInvalidCategories(categoriesList, finalCategoriesList);
        List<Pair<String, Long>> invalidCategoryIds = new ArrayList<>();

        if (!CollectionUtils.isEmpty(invalidCategories)) {
            invalidCategories.forEach(categories -> invalidCategoryIds.add(Pair.of(
                categories.getKey(),
                categories.getRowId()
            )));

            lovServiceJsrValidatorInitializer.auditJsrExceptions(
                invalidCategoryIds,
                LOV_COMPOSITE_KEY,
                LOV_COMPOSITE_KEY_ERROR_MSG,
                exchange
            );
        }

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
            if (categoriesList.size() > 1) {
                finalCategories.addAll(filterInvalidCategories(categoriesList));
            } else {
                finalCategories.addAll(categoriesList);
            }
        });
        return finalCategories;
    }

    private Multimap<String, Categories> convertToMultiMap(List<Categories> categoriesList) {
        Multimap<String, Categories> multimap = ArrayListMultimap.create();
        categoriesList.forEach(categories -> multimap.put(categories.getCategoryKey() + categories.getServiceId()
                                                              + categories.getKey(), categories));
        return multimap;
    }

    void setFileStatus(Exchange exchange, ApplicationContext applicationContext, String auditStatus) {
        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        FileStatus fileStatus = getFileDetails(exchange.getContext(), routeProperties.getFileName());
        fileStatus.setAuditStatus(auditStatus);
        registerFileStatusBean(applicationContext, routeProperties.getFileName(), fileStatus,
                               exchange.getContext()
        );
    }

    private List<Categories> filterInvalidCategories(List<Categories> categoriesList) {
        List<Categories> validCategories = new ArrayList<>();

        boolean activeProcessed = false;

        for (Categories category : categoriesList) {
            if ((ACTIVE_Y.equalsIgnoreCase(category.getActive())
                    && !activeProcessed)) {
                validCategories.add(category);
                activeProcessed = true;
            }
        }
        return validCategories;
    }
}
