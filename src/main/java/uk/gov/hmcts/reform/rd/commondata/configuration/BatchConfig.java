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
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
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
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask;

    @Autowired
    JobResultListener jobResultListener;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Value("${commondata-flag-service-route-task}")
    String commonDataTask;

    @Value("${commondata-categories-route-task}")
    String commonDataCategoriesTask;

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

    @Bean
    public Step stepCommonDataCategoriesRoute() {
        return steps.get(commonDataCategoriesTask)
            .tasklet(commonDataCategoriesRouteTask)
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
            .on("*").to(stepCommonDataCategoriesRoute())
            .end()
            .build();
    }
}
