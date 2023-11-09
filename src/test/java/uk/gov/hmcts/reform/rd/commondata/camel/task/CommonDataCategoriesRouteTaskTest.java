package uk.gov.hmcts.reform.rd.commondata.camel.task;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;

import java.sql.ResultSet;
import java.util.Collections;
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
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer;

    @BeforeEach
    void init() {
        setField(commonDataCategoriesRouteTask, "logComponentName", "testlogger");
        setField(commonDataCategoriesRouteTask, "dataLoadRoute", dataLoadRoute);
        setField(commonDataCategoriesRouteTask, "commonDataExecutor", commonDataExecutor);
        setField(commonDataCategoriesRouteTask, "camelContext", camelContext);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void testExecute() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        Categories expected = Categories.builder().categoryKey("value returned by query")
            .serviceId("value returned by query")
            .key("value returned by query").build();
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenAnswer((Answer<List<Categories>>) invocation -> {
                // Fetch the method arguments
                Object[] args = invocation.getArguments();

                // Create a mock result set and setup an expectation on it
                ResultSet rs = mock(ResultSet.class);
                when(rs.getString(anyString())).thenReturn("value returned by query");

                // Fetch the row mapper instance from the arguments
                RowMapper<Categories> rm = (RowMapper<Categories>) args[1];
                // Invoke the row mapper
                Categories actual = rm.mapRow(rs, 0);

                // Assert the result of the row mapper execution
                Assertions.assertEquals(expected, actual);

                // Return your created list for the template#query call
                return List.of(expected);
            });
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);

        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCategoriesRouteTask.execute(any(), any()));
        verify(dataLoadRoute,times(1)).startRoute(any(),any());
        verify(commonDataCategoriesRouteTask, times(1)).execute(any(), any());

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


    @Test
    void testExecute_NoDCategoriestoDelete() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenReturn(Collections.emptyList()
            );
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(0);

        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCategoriesRouteTask.execute(any(), any()));
        verify(dataLoadRoute,times(1)).startRoute(any(),any());
        verify(commonDataCategoriesRouteTask, times(1)).execute(any(), any());

    }

}

