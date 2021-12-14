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
public class CommonDataFlagServiceRouteTaskTest {
    @Spy
    CommonDataFlagServiceRouteTask commonDataFlagServiceRouteTask = new CommonDataFlagServiceRouteTask();

    DataLoadRoute dataLoadRoute = mock(DataLoadRoute.class);

    CommonDataExecutor commonDataExecutor = mock(CommonDataExecutor.class);

    CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    public void init() {
        setField(commonDataFlagServiceRouteTask, "logComponentName", "testlogger");
        setField(commonDataFlagServiceRouteTask, "dataLoadRoute", dataLoadRoute);
        setField(commonDataFlagServiceRouteTask, "commonDataExecutor", commonDataExecutor);
        setField(commonDataFlagServiceRouteTask, "camelContext", camelContext);
    }

    @Test
    void testExecute() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataFlagServiceRouteTask
            .execute(anyString(), anyList(), anyBoolean()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataFlagServiceRouteTask, times(1))
            .execute(anyString(), anyList(), anyBoolean());
    }

    @Test
    void testExecute_NoAuditPreference() {
        doNothing().when(dataLoadRoute).startRoute(anyString(), anyList());
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        Assertions.assertEquals(RepeatStatus.FINISHED, commonDataFlagServiceRouteTask
            .execute(anyString(), anyList(), isNull()));
        verify(dataLoadRoute,times(1)).startRoute(anyString(),anyList());
        verify(commonDataFlagServiceRouteTask, times(1))
            .execute(anyString(), anyList(), isNull());
    }
}
