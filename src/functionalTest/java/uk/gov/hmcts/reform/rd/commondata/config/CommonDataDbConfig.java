//package uk.gov.hmcts.reform.rd.commondata.config;
//
//import org.apache.camel.spring.spi.SpringTransactionPolicy;
//import org.junit.jupiter.api.extension.RegisterExtension;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
//import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.core.task.SyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class CommonDataDbConfig {
//
////    @Autowired
//////    @Qualifier("springJdbcDataSource")
////    private DataSource dataSource;
//
//    // db configuration starts
//
//    @Container
//    @RegisterExtension
//    @ServiceConnection
//    static final PostgreSQLContainer testPostgres = new PostgreSQLContainer("postgres")
//        .withDatabaseName("dbcommondata");
//
////    @Bean
//    public DataSource dataSource() {
//        return springJdbcDataSource();
//    }
//
//    private DataSourceBuilder getDataSourceBuilder() {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName("org.postgresql.Driver");
//        dataSourceBuilder.url(testPostgres.getJdbcUrl());
//        dataSourceBuilder.username(testPostgres.getUsername());
//        dataSourceBuilder.password(testPostgres.getPassword());
//        return dataSourceBuilder;
//    }
//
//    @Bean //("springJdbcDataSource")
//    public DataSource springJdbcDataSource() {
//        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
//        return dataSourceBuilder.build();
//    }
//
//    @Bean("springJdbcTemplate")
//    public JdbcTemplate springJdbcTemplate() {
//        JdbcTemplate jdbcTemplate = new JdbcTemplate();
//        jdbcTemplate.setDataSource(springJdbcDataSource());
//        return jdbcTemplate;
//    }
//    // db configuration ends
//
//    // transaction configuration starts
//    @Bean(name = "txManager")
//    public PlatformTransactionManager txManager() {
//        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource());
//        platformTransactionManager.setDataSource(dataSource());
//        return platformTransactionManager;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        return txManager();
//    }
//
//    @Bean(name = "springJdbcTransactionManager")
//    public PlatformTransactionManager springJdbcTransactionManager() {
//        DataSourceTransactionManager platformTransactionManager
//            = new DataSourceTransactionManager(springJdbcDataSource());
//        platformTransactionManager.setDataSource(springJdbcDataSource());
//        return platformTransactionManager;
//    }
//
//    @Bean(name = "PROPAGATION_REQUIRED")
//    public SpringTransactionPolicy getSpringTransaction() {
//        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
//        springTransactionPolicy.setTransactionManager(transactionManager());
//        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
//        return springTransactionPolicy;
//    }
//
//    @Bean(name = "PROPAGATION_REQUIRES_NEW")
//    public SpringTransactionPolicy propagationRequiresNew() {
//        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
//        springTransactionPolicy.setTransactionManager(transactionManager());
//        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
//        return springTransactionPolicy;
//    }
//
//    // transaction configuration ends
// }
