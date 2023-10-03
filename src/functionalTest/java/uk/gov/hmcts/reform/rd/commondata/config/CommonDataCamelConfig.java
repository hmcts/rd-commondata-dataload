package uk.gov.hmcts.reform.rd.commondata.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.validator.HibernateValidationProviderResolver;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.data.ingestion.DataIngestionLibraryRunner;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ArchiveFileProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.CommonCsvFieldProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.FileReadProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.FileResponseProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ParentStateCheckProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.ArchivalRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.service.ArchivalBlobServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.EmailServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IEmailService;
import uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.camel.listener.JobResultListener;
import uk.gov.hmcts.reform.rd.commondata.camel.mapper.CategoriesMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.mapper.FlagDetailsMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.mapper.FlagServiceMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.processor.CategoriesProcessor;
import uk.gov.hmcts.reform.rd.commondata.camel.processor.FlagDetailsProcessor;
import uk.gov.hmcts.reform.rd.commondata.camel.processor.FlagServiceProcessor;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagDetailsRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataBlobSupport;

import javax.sql.DataSource;

@Configuration
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootConfiguration
//@TestConfiguration (proxyBeanMethods = false)
@Testcontainers
public class CommonDataCamelConfig {

    @Bean
    CommonDataBlobSupport integrationTestSupport() {
        return new CommonDataBlobSupport();
    }

    @Bean
    public JsrValidatorInitializer<FlagService> flagServiceJsrValidatorInitializer() {
        return new JsrValidatorInitializer<>();
    }

    @Bean
    public JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer() {
        return new JsrValidatorInitializer<>();
    }

    @Bean
    public JsrValidatorInitializer<FlagDetails> flagDetailsJsrValidatorInitializer() {
        return new JsrValidatorInitializer<>();
    }


    @Bean
    public FlagServiceProcessor flagServiceProcessor() {
        return new FlagServiceProcessor();
    }

    @Bean
    public CategoriesProcessor listOfValuesProcessor() {
        return new CategoriesProcessor();
    }

    @Bean
    public FlagDetailsProcessor flagDetailsProcessor() {
        return new FlagDetailsProcessor();
    }

    @Bean
    public CategoriesMapper listOfValuesMapper() {
        return new CategoriesMapper();
    }

    @Bean
    public FlagServiceMapper flagServiceMapper() {
        return new FlagServiceMapper();
    }

    @Bean
    public FlagDetailsMapper flagDetailsMapper() {
        return new FlagDetailsMapper();
    }

    @Bean
    public Categories listOfValues() {
        return new Categories();
    }


    @Bean
    public FlagService flagService() {
        return new FlagService();
    }

    @Bean
    public FlagDetails flagDetails() {
        return new FlagDetails();
    }

    // Route configuration ends

    // processor configuration starts
    @Bean
    FileReadProcessor fileReadProcessor() {
        return new FileReadProcessor();
    }

    @Bean
    ArchiveFileProcessor azureFileProcessor() {
        return new ArchiveFileProcessor();
    }

    @Bean
    public ExceptionProcessor exceptionProcessor() {
        return new ExceptionProcessor();
    }

    @Bean
    public AuditServiceImpl schedulerAuditProcessor() {
        return new AuditServiceImpl();
    }

    @Bean
    public CommonCsvFieldProcessor commonCsvFieldProcessor() {
        return new CommonCsvFieldProcessor();
    }

    @Bean
    ParentStateCheckProcessor parentStateCheckProcessor() {
        return new ParentStateCheckProcessor();
    }

    @Bean
    public HeaderValidationProcessor headerValidationProcessor() {
        return new HeaderValidationProcessor();
    }
    // processor configuration starts

//    @RegisterExtension
//    protected static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    // db configuration starts
//    @Bean
//    @ServiceConnection
//    public PostgreSQLContainer postgres() {
//        return testPostgres;
//    }
//        return new PostgreSQLContainer("postgres")
//                .withDatabaseName("dbcommondata_test");

    @Container
    @RegisterExtension
    @ServiceConnection
    static final PostgreSQLContainer testPostgres = new PostgreSQLContainer("postgres")
            .withDatabaseName("dbcommondata")
            ;
//        .withUsername("dbcommondata")
//        .withPassword("dbcommondata")

//    static class TestPostgresContainer extends PostgreSQLContainer<TestPostgresContainer> {
//
//        TestPostgresContainer(String containerName) {
//            super(containerName);
//        }
//
//        @Override
//        protected void configure() {
//            withUrlParam("loggerLevel", "ALL");
//            addEnv("POSTGRES_DB", getDatabaseName());
//            addEnv("POSTGRES_USER", getUsername());
//            addEnv("POSTGRES_PASSWORD", getPassword());
//        }
//    }

//    @DynamicPropertySource
//    static void registerPgProperties(DynamicPropertyRegistry registry) {
////        registry.add("spring.datasource.url", testPostgres::getJdbcUrl);
//        registry.add("spring.datasource.url", () -> "jdbc:tc:postgres:11:///dbcommondata");
////        registry.add("spring.datasource.username", testPostgres::getUsername);
////        registry.add("spring.datasource.password", testPostgres::getPassword);
//    }

    static {
//        testPostgres.start();
//        try {
//            Thread.sleep(60_000l);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        testPostgres.stop();
    }

//    public static void restartPostgres() {
//        testPostgres.stop();
//        testPostgres.start();
//    }

//    @PreDestroy
//    public void stop() {
//        testPostgres.stop();
//    }

    @Bean
    public DataSource dataSource() {
//        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
//        return dataSourceBuilder.build();
        return springJdbcDataSource();
    }

    private DataSourceBuilder getDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
//        if (testPostgres.isRunning())
//            testPostgres.stop();
//        if (!testPostgres.isRunning())
//            testPostgres.start();
        dataSourceBuilder.url(testPostgres.getJdbcUrl());
        dataSourceBuilder.username(testPostgres.getUsername());
        dataSourceBuilder.password(testPostgres.getPassword());
        return dataSourceBuilder;
    }

    @Bean("springJdbcDataSource")
    public DataSource springJdbcDataSource() {
        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
        return dataSourceBuilder.build();
    }

    @Bean("springJdbcTemplate")
    public JdbcTemplate springJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(springJdbcDataSource());
        return jdbcTemplate;
    }

//    @Bean
//    public DataSource defaultDataSource() {
//        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
//        return dataSourceBuilder.build();
//    }
    // db configuration ends

    // transaction configuration starts
    @Bean(name = "txManager")
    public PlatformTransactionManager txManager() {
        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource());
        platformTransactionManager.setDataSource(dataSource());
        return platformTransactionManager;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return txManager();
//        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(defaultDataSource());
//        transactionManager.setDataSource(defaultDataSource());
//        return transactionManager;
    }

    @Bean(name = "springJdbcTransactionManager")
    public PlatformTransactionManager springJdbcTransactionManager() {
        DataSourceTransactionManager platformTransactionManager
            = new DataSourceTransactionManager(springJdbcDataSource());
        platformTransactionManager.setDataSource(springJdbcDataSource());
        return platformTransactionManager;
    }

    @Bean(name = "PROPAGATION_REQUIRED")
    public SpringTransactionPolicy getSpringTransaction() {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(transactionManager());
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return springTransactionPolicy;
    }

    @Bean(name = "PROPAGATION_REQUIRES_NEW")
    public SpringTransactionPolicy propagationRequiresNew() {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(transactionManager());
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        return springTransactionPolicy;
    }

    // transaction configuration ends

    // tasks configuration starts
    @Bean
    CommonDataFlagServiceRouteTask commonDataFlagServiceRouteTask() {
        return new CommonDataFlagServiceRouteTask();
    }

    @Bean
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask() {
        return new CommonDataCategoriesRouteTask();
    }

    @Bean
    CommonDataCaseLinkingRouteTask commonDataCaseLinkingRouteTask() {
        return new CommonDataCaseLinkingRouteTask();
    }

    @Bean
    CommonDataFlagDetailsRouteTask commonDataFlagDetailsRouteTask() {
        return new CommonDataFlagDetailsRouteTask();
    }

    @Bean
    CommonDataExecutor commonDataExecutor() {
        return new CommonDataExecutor();
    }
    // tasks configuration ends

    // camel related configuration starts
    @Bean
    DataLoadRoute dataLoadRoute() {
        return new DataLoadRoute();
    }

    @Bean
    ArchivalRoute archivalRoute() {
        return new ArchivalRoute();
    }

    @Bean
    public CamelContext camelContext(ApplicationContext applicationContext) {
//        return contextExtension.getContext();
//        return new SpringCamelContext(applicationContext);
        return new SpringBootCamelContext(applicationContext, true);
    }
    // camel related configuration ends

    // miscellaneous configuration starts
    @Bean("myValidationProviderResolver")
    HibernateValidationProviderResolver hibernateValidationProviderResolver() {
        return new HibernateValidationProviderResolver();
    }

    @Bean("myConstraintValidatorFactory")
    public ConstraintValidatorFactoryImpl constraintValidatorFactory() {
        return new ConstraintValidatorFactoryImpl();
    }

    @Bean
    public DataLoadUtil dataLoadUtil() {
        return new DataLoadUtil();
    }

    @Bean
    IEmailService emailService() {
        return Mockito.mock(EmailServiceImpl.class);
    }

    @Bean
    JobResultListener jobResultListener() {
        return new JobResultListener();
    }

    @Bean
    FileResponseProcessor fileResponseProcessor() {
        return new FileResponseProcessor();
    }

    @Bean
    ArchivalBlobServiceImpl archivalBlobService() {
        return new ArchivalBlobServiceImpl();
    }

    @Bean
    DataIngestionLibraryRunner dataIngestionLibraryRunner() {
        return new DataIngestionLibraryRunner();
    }

    // miscellaneous configuration ends
}
