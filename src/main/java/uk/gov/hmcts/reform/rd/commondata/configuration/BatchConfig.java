package uk.gov.hmcts.reform.rd.commondata.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.rd.commondata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagDetailsRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataOtherCategoriesRouteTask;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class BatchConfig {

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

    /**
     * Create Step to run common Data Flag Route.
     * @return Step
     */
    @Bean
    public Step stepCommonDataRoute(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        return new StepBuilder(commonDataTask, jobRepository)
            .tasklet(commonDataFlagServiceRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Step stepCommonDataCategoriesRoute(JobRepository jobRepository,
                                              PlatformTransactionManager transactionManager) {
        return new StepBuilder(commonDataCategoriesTask, jobRepository)
            .tasklet(commonDataCategoriesRouteTask, transactionManager)
            .build();
    }


    @Bean
    public Step stepCommonDataCaseLinkingRoute(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager) {
        return new StepBuilder(commonDataCaseLinkingTask, jobRepository)
            .tasklet(commonDataCaseLinkingRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Step stepCommonDataFlagDetailsRoute(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager) {
        return new StepBuilder(commonDataFlagDetailsTask, jobRepository)
            .tasklet(commonDataFlagDetailsRouteTask, transactionManager)
            .build();
    }

    @Bean
    public Step stepOtherCategoriesRoute(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager) {
        return new StepBuilder(commonDataOtherCategoriesTask, jobRepository)
            .tasklet(commonDataOtherCategoriesRouteTask, transactionManager)
            .build();
    }

    /**
     * Returns Job bean.
     * @return Job
     */
    @Bean
    public Job runRoutesJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(jobName, jobRepository)
            .start(stepCommonDataFlagDetailsRoute(jobRepository, transactionManager))
            .listener(jobResultListener)
            .on("*").to(stepCommonDataRoute(jobRepository, transactionManager))
            .on("*").to(stepOtherCategoriesRoute(jobRepository, transactionManager))
            .on("*").to(checkCaseLinkingRouteStatus())
            .from(checkCaseLinkingRouteStatus()).on("STOPPED")
            .to(stepCommonDataCategoriesRoute(jobRepository, transactionManager))
            .from(checkCaseLinkingRouteStatus()).on("ENABLED")
            .to(stepCommonDataCaseLinkingRoute(jobRepository, transactionManager))
            .on("*").to(stepCommonDataCategoriesRoute(jobRepository, transactionManager))
            .end()
            .build();
    }

    @Bean
    public JobExecutionDecider checkCaseLinkingRouteStatus() {
        return (job, step) -> new FlowExecutionStatus(isDisabledCaseLinkingRoute ? "STOPPED" : "ENABLED");
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager platformTransactionManager
            = new DataSourceTransactionManager(dataSource);
        platformTransactionManager.setDataSource(dataSource);
        return platformTransactionManager;
    }

    @Bean(name = "jobRepository")
    public JobRepository jobRepository(DataSource dataSource,
                                       PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
