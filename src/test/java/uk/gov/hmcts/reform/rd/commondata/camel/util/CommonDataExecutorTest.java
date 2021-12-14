package uk.gov.hmcts.reform.rd.commondata.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.IS_READY_TO_AUDIT;

@ExtendWith(MockitoExtension.class)
public class CommonDataExecutorTest {
    CommonDataExecutor commonDataExecutor = new CommonDataExecutor();

    CommonDataExecutor commonDataExecutorSpy = spy(commonDataExecutor);

    CamelContext camelContext = new DefaultCamelContext();

    AuditServiceImpl auditService = mock(AuditServiceImpl.class);

    ProducerTemplate producerTemplate = mock(ProducerTemplate.class);

    @BeforeEach
    public void init() {
        setField(commonDataExecutorSpy, "auditService", auditService);
    }

    @Test
    void testExecute() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        commonDataExecutorSpy.execute(camelContext, "test", "test");
        verify(commonDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }

    @Test
    void testExecute_AuditDisabled() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.FALSE.toString());
        commonDataExecutorSpy.execute(camelContext, "test", "test");
        verify(commonDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_AuditEnabled() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, Boolean.TRUE.toString());
        commonDataExecutorSpy.execute(camelContext, "test", "test");
        verify(commonDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(1)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecute_NoAuditPreference() {
        doNothing().when(producerTemplate).sendBody(Mockito.any());
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, null);
        commonDataExecutorSpy.execute(camelContext, "test", "test");
        verify(commonDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
        verify(auditService, times(0)).auditSchedulerStatus(camelContext);
    }

    @Test
    void testExecuteException() {
        doNothing().when(auditService).auditSchedulerStatus(camelContext);
        commonDataExecutorSpy.execute(camelContext, "test", "test");
        verify(commonDataExecutorSpy, times(1)).execute(camelContext, "test", "test");
    }
}
