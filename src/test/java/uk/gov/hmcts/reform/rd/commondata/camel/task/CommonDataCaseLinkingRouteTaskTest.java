package uk.gov.hmcts.reform.rd.commondata.camel.task;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
class CommonDataCaseLinkingRouteTaskTest {
    @Spy
    CommonDataCaseLinkingRouteTask commonDataCaseLinkingRouteTask = new CommonDataCaseLinkingRouteTask();

    DataLoadRoute dataLoadRoute = mock(DataLoadRoute.class);

    CommonDataExecutor commonDataExecutor = mock(CommonDataExecutor.class);

    CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    void init() {
        setField(commonDataCaseLinkingRouteTask, "logComponentName", "testlogger");
        setField(commonDataCaseLinkingRouteTask, "dataLoadRoute", dataLoadRoute);
        setField(commonDataCaseLinkingRouteTask, "commonDataExecutor", commonDataExecutor);
        setField(commonDataCaseLinkingRouteTask, "camelContext", camelContext);
    }

    @Test
    void testExecute() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCaseLinkingRouteTask
            .execute(anyString(), anyList(), anyBoolean()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataCaseLinkingRouteTask, times(1))
            .execute(anyString(), anyList(), anyBoolean());
    }

    @Test
    void testExecute_NoAuditPreference() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCaseLinkingRouteTask
            .execute(anyString(), anyList(), isNull()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataCaseLinkingRouteTask, times(1))
            .execute(anyString(), anyList(), isNull());
    }

    @Test
    void execute() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCaseLinkingRouteTask
            .execute(anyString(), anyList(), anyBoolean()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataCaseLinkingRouteTask, times(1))
            .execute(anyString(), anyList(), anyBoolean());
    }

    @Test
    void testExecute1() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataCaseLinkingRouteTask.execute(any(), any()));
        verify(dataLoadRoute,times(1)).startRoute(any(),any());
        verify(commonDataCaseLinkingRouteTask, times(1)).execute(any(), any());

    }


}
