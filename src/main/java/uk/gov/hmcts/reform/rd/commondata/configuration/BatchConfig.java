package uk.gov.hmcts.reform.rd.commondata.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.rd.commondata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagDetailsRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataOtherCategoriesRouteTask;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;

    private final JobLauncher jobLauncher;

    private final PlatformTransactionManager batchTransactionManager;

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

    public BatchConfig(JobLauncher jobLauncher,
                       JobRepository jobRepository,
                       PlatformTransactionManager batchTransactionManager) {
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.batchTransactionManager = batchTransactionManager;
    }

    /**
     * Create Step to run common Data Flag Route.
     * @return Step
     */
    @Bean
    public Step stepCommonDataRoute() {
        return new StepBuilder(commonDataTask, jobRepository)
            .tasklet(commonDataFlagServiceRouteTask, batchTransactionManager)
            .build();
    }

    @Bean
    public Step stepCommonDataCategoriesRoute() {
        return new StepBuilder(commonDataCategoriesTask, jobRepository)
            .tasklet(commonDataCategoriesRouteTask, batchTransactionManager)
            .build();
    }


    @Bean
    public Step stepCommonDataCaseLinkingRoute() {
        return new StepBuilder(commonDataCaseLinkingTask, jobRepository)
            .tasklet(commonDataCaseLinkingRouteTask, batchTransactionManager)
            .build();
    }

    @Bean
    public Step stepCommonDataFlagDetailsRoute() {
        return new StepBuilder(commonDataFlagDetailsTask, jobRepository)
            .tasklet(commonDataFlagDetailsRouteTask, batchTransactionManager)
            .build();
    }

    @Bean
    public Step stepOtherCategoriesRoute() {
        return new StepBuilder(commonDataOtherCategoriesTask, jobRepository)
            .tasklet(commonDataOtherCategoriesRouteTask, batchTransactionManager)
            .build();
    }

    /**
     * Returns Job bean.
     *
     * @return Job
     */
    @Bean
    public Job runRoutesJob() {
        return new JobBuilder(jobName, jobRepository)
            .start(stepCommonDataFlagDetailsRoute())
            .listener(jobResultListener)
            .on("*").to(stepCommonDataRoute())
            .on("*").to(stepOtherCategoriesRoute())
            .on("*").to(checkCaseLinkingRouteStatus())
            .from(checkCaseLinkingRouteStatus()).on("STOPPED")
            .to(stepCommonDataCategoriesRoute())
            .from(checkCaseLinkingRouteStatus()).on("ENABLED")
            .to(stepCommonDataCaseLinkingRoute())
            .on("*").to(stepCommonDataCategoriesRoute())
            .end()
            .build();
    }

    @Bean
    public JobExecutionDecider checkCaseLinkingRouteStatus() {
        return (job, step) -> new FlowExecutionStatus(isDisabledCaseLinkingRoute ? "STOPPED" : "ENABLED");
    }
}
