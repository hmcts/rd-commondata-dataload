package uk.gov.hmcts.reform.rd.commondata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
//@EnableBatchProcessing
@Import(CommonDataCamelConfig.class)
public class CommonDataSpringBatchConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

//    @Bean
//    public DefaultDataFieldMaxValueIncrementerFactory incrementerFactory() {
//        return new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
//    }
//
//    @Bean
//    public JobRepositoryFactoryBean jobRepositoryFactoryBean() {
//        final var jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
//        jobRepositoryFactoryBean.setDataSource(dataSource);
//        jobRepositoryFactoryBean.setIncrementerFactory(incrementerFactory());
//        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
//
//        return jobRepositoryFactoryBean;
//    }
//
//    @Bean
//    public TaskExecutor taskExecutor() {
//        //return new SimpleAsyncTaskExecutor();
//        return new SyncTaskExecutor();
//    }
//
//    @Bean
//    public JobRepository jobRepository() throws Exception {
//        return jobRepositoryFactoryBean().getObject();
//    }
//
//    @Bean
//    public JobLauncher jobLauncher() throws Exception {
//        final var jobLauncher = new TaskExecutorJobLauncher();
//
//        jobLauncher.setTaskExecutor(taskExecutor());
//
//        jobLauncher.setJobRepository(jobRepository());
//
//        return jobLauncher;
//    }

}