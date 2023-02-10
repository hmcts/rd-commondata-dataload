package uk.gov.hmcts.reform.rd.commondata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.data.ingestion.DataIngestionLibraryRunner;

@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
@SuppressWarnings("PMD.DoNotCallSystemExit")
@Slf4j
public class CommonDataLoadApplication implements ApplicationRunner {

    @Autowired
    Job job;

    @Value("${batchjob-name}")
    String jobName;

    @Autowired
    DataIngestionLibraryRunner dataIngestionLibraryRunner;

    private static String logComponentName;

    public static void main(final String[] args) throws InterruptedException {

        ApplicationContext context = SpringApplication.run(CommonDataLoadApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString(jobName, String.valueOf(System.currentTimeMillis()))
            .toJobParameters();
        dataIngestionLibraryRunner.run(job, params);
    }

    @Value("${logging-component-name}")
    public void setLogComponentName(String logComponentName) {
        CommonDataLoadApplication.logComponentName = logComponentName;
    }
}
