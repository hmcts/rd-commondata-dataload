package uk.gov.hmcts.reform.rd.commondata.camel.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ACTIVE_FLAG_D;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.DELETE_CATEGORY_BY_STATUS;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.INVALID_JSR_PARENT_ROW;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.SELECT_CATEGORY_BY_STATUS;

@Component
@Slf4j
public class CommonDataCategoriesRouteTask extends BaseTasklet implements Tasklet {

    @Value("${commondata-categories-start-route}")
    String startRoute;

    @Value("${commondata-categories-routes-to-execute}")
    List<String> routesToExecute;

    @Autowired
    JsrValidatorInitializer<Categories> categoriesJsrValidatorInitializer;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        RepeatStatus repeatStatus = super.execute(startRoute, routesToExecute, Boolean.FALSE);

        auditAndDeleteCategories();
        return repeatStatus;
    }

    private void auditAndDeleteCategories() {
        // Select Status based Categories
        List<Categories> categories = getCategoriesByStatus(ACTIVE_FLAG_D);
        log.info("Status with {} Categories are {}", ACTIVE_FLAG_D, categories.size());
        // Audit status based Categories
        audit(categories, INVALID_JSR_PARENT_ROW);
        // Delete given Categories
        boolean deleted = deleteCategories(ACTIVE_FLAG_D);
        log.info("Deleted {} status Categories are {}", ACTIVE_FLAG_D, deleted);
    }

    private boolean deleteCategories(String status) {
        return jdbcTemplate.update(DELETE_CATEGORY_BY_STATUS, status) == 1;
    }

    private void audit(List<Categories> categories, String message) {
        List<Pair<String, Long>> categoryKeys = new ArrayList<>();
        Exchange exchange = new DefaultExchange(camelContext);
        RouteProperties properties = new RouteProperties();
        properties.setTableName("list_of_values");
        exchange.getIn().setHeader("routedetails", properties);
        categories.forEach(category -> categoryKeys.add(Pair.of(
            String.join("-",category.getCategoryKey(), category.getKey(), category.getServiceId()),
            category.getRowId())));
        categoriesJsrValidatorInitializer.auditJsrExceptions(categoryKeys, null, message, exchange);
    }

    private List<Categories> getCategoriesByStatus(String activeFlag) {
        return jdbcTemplate.query(SELECT_CATEGORY_BY_STATUS,
                                  (rs, rowNum) -> {
                                      Categories categories =  new Categories();
                                      categories.setCategoryKey(rs.getString("categorykey"));
                                      categories.setKey(rs.getString("key"));
                                      categories.setServiceId(rs.getString("serviceid"));
                                      categories.setRowId((long) rs.getRow());
                                      return categories;
                                  },
                                  activeFlag
        );
    }


}
