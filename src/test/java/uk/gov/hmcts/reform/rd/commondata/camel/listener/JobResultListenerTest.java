package uk.gov.hmcts.reform.rd.commondata.camel.listener;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import uk.gov.hmcts.reform.data.ingestion.camel.service.ArchivalBlobServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IArchivalBlobService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class JobResultListenerTest {

    JobResultListener jobResultListener = spy(new JobResultListener());

    JobExecution jobExecution = mock(JobExecution.class);

    IArchivalBlobService archivalBlobService = mock(ArchivalBlobServiceImpl.class);

    @BeforeEach
    public void init() {
        setField(jobResultListener, "archivalBlobService", archivalBlobService);
    }

    @Test
    void beforeJobTest() {
        jobResultListener.beforeJob(jobExecution);
        verify(jobResultListener, times(1)).beforeJob(jobExecution);
    }

    @Test
    void afterJobTest() {
        doNothing().when(archivalBlobService).executeArchiving();
        jobResultListener.afterJob(jobExecution);
        verify(jobResultListener, times(1)).afterJob(jobExecution);
        verify(archivalBlobService, times(1)).executeArchiving();
    }
}
