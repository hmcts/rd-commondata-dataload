package uk.gov.hmcts.reform.rd.commondata.camel.task;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CommonDataCategoriesRouteTaskTest {
    @Spy
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask = new CommonDataCategoriesRouteTask();

    DataLoadRoute dataLoadRoute = mock(DataLoadRoute.class);

    CommonDataExecutor commonDataExecutor = mock(CommonDataExecutor.class);

    CamelContext camelContext = new DefaultCamelContext();

    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @Mock
    JsrValidatorInitializer<Categories> categoriesJsrValidatorInitializer;

    @BeforeEach
    void init() {
        setField(commonDataCategoriesRouteTask, "logComponentName", "testlogger");
        setField(commonDataCategoriesRouteTask, "dataLoadRoute", dataLoadRoute);
        setField(commonDataCategoriesRouteTask, "commonDataExecutor", commonDataExecutor);
        setField(commonDataCategoriesRouteTask, "camelContext", camelContext);
        setField(commonDataCategoriesRouteTask, "jdbcTemplate", jdbcTemplate);
        setField(commonDataCategoriesRouteTask, "categoriesJsrValidatorInitializer",
                 categoriesJsrValidatorInitializer);
    }

    @Test
    void testExecute() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenReturn(List.of(
                Categories.builder().categoryKey("categoryKey").serviceId("serviceId").key("key").build()
            ));
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);

        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCategoriesRouteTask.execute(any(), any()));
        verify(dataLoadRoute,times(1)).startRoute(any(),any());
        verify(commonDataCategoriesRouteTask, times(1)).execute(any(), any());
        verify(categoriesJsrValidatorInitializer,times(1))
            .auditJsrExceptions(ArgumentMatchers.any(), any(), anyString(), any(Exchange.class));
        verify(jdbcTemplate, times(1))
            .query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString());
        verify(jdbcTemplate, times(1)).update(anyString(), anyString());
    }

    @Test
    void testExecute_NoAuditPreference() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCategoriesRouteTask
            .execute(anyString(), anyList(), isNull()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataCategoriesRouteTask, times(1))
            .execute(anyString(), anyList(), isNull());
    }

}

