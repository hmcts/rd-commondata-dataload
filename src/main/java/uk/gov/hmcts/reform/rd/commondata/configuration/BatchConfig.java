package uk.gov.hmcts.reform.rd.commondata.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.rd.commondata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagDetailsRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataOtherCategoriesRouteTask;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

    @Autowired
    StepBuilderFactory steps;

    @Autowired
    CommonDataFlagServiceRouteTask commonDataFlagServiceRouteTask;

    @Autowired
    CommonDataOtherCategoriesRouteTask commonDataOtherCategoriesRouteTask;

    @Autowired
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask;

    @Autowired
    CommonDataCaseLinkingRouteTask commonDataCaseLinkingRouteTask;

    @Autowired
    CommonDataFlagDetailsRouteTask commonDataFlagDetailsRouteTask;

    @Autowired
    JobResultListener jobResultListener;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Value("${commondata-flag-service-route-task}")
    String commonDataTask;

    @Value("${commondata-categories-route-task}")
    String commonDataCategoriesTask;

    @Value("${commondata-flag-details-route-task}")
    String commonDataFlagDetailsTask;

    @Value("${commondata-caselinking-route-task}")
    String commonDataCaseLinkingTask;

    @Value("${commondata-caselinking-route-disable:false}")
    boolean isDisabledCaseLinkingRoute;

    @Value("${commondata-othercategories-route-task}")
    String commonDataOtherCategoriesTask;

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


    @Bean
    public Step stepCommonDataCaseLinkingRoute() {
        return steps.get(commonDataCaseLinkingTask)
            .tasklet(commonDataCaseLinkingRouteTask)
            .build();
    }

    @Bean
    public Step stepCommonDataFlagDetailsRoute() {
        return steps.get(commonDataFlagDetailsTask)
            .tasklet(commonDataFlagDetailsRouteTask)
            .build();
    }

    @Bean
    public Step stepOtherCategoriesRoute() {
        return steps.get(commonDataOtherCategoriesTask)
            .tasklet(commonDataOtherCategoriesRouteTask)
            .build();
    }

    /**
     * Returns Job bean.
     * @return Job
     */
    @Bean
    public Job runRoutesJob() {
        return jobBuilderFactory.get(jobName)
            .start(stepCommonDataFlagDetailsRoute())
            .listener(jobResultListener)
            .on("*").to(stepCommonDataRoute())
            .on("*").to(stepCommonDataCategoriesRoute())
            .on("*").to(checkCaseLinkingRouteStatus())
            .from(checkCaseLinkingRouteStatus()).on("STOPPED").to(stepOtherCategoriesRoute())
            .from(checkCaseLinkingRouteStatus()).on("ENABLED").to(stepCommonDataCaseLinkingRoute())
                                                .on("*").to(stepOtherCategoriesRoute())
            .end()
            .build();
    }

    @Bean
    public JobExecutionDecider checkCaseLinkingRouteStatus() {
        return (job, step) -> new FlowExecutionStatus(isDisabledCaseLinkingRoute ? "STOPPED" : "ENABLED");
    }

}
