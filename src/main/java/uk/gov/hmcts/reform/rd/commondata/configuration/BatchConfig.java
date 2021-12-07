package uk.gov.hmcts.reform.rd.commondata.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.rd.commondata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

    @Autowired
    StepBuilderFactory steps;

    @Autowired
    CommonDataFlagServiceRouteTask commonDataFlagServiceRouteTask;

    @Autowired
    JobResultListener jobResultListener;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Value("${commondata-flag-service-route-task}")
    String commonDataTask;

    @Value("${batchjob-name}")
    String jobName;

    /**
     * Create Step to run common Data Flag Route.
     * @return Step
     */
    @Bean
    public Step stepCommonDataRoute() {
        return steps.get(commonDataTask)
            .tasklet(commonDataFlagServiceRouteTask)
            .build();
    }

    /**
     * Returns Job bean.
     * @return Job
     */
    @Bean
    public Job runRoutesJob() {
        return jobBuilderFactory.get(jobName)
            .start(stepCommonDataRoute())
            .listener(jobResultListener)
            .build();
    }
}
