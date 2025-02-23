package uk.gov.hmcts.reform.rd.commondata.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.validator.HibernateValidationProviderResolver;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
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
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataOtherCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataDRecords;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataBlobSupport;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import javax.sql.DataSource;

@Configuration
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
    public DataQualityCheckConfiguration dataQualityCheckConfiguration() {
        return new DataQualityCheckConfiguration();
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

    @Bean
    public CommonDataDRecords headerCommonDataDRecords() {
        return new CommonDataDRecords();
    }

    // db configuration starts
    private static final PostgreSQLContainer testPostgres = new PostgreSQLContainer("postgres")
        .withDatabaseName("dbcommondata_test");

    static {
        testPostgres.start();
    }

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = getDataSourceBuilder();
        return dataSourceBuilder.build();
    }

    private DataSourceBuilder getDataSourceBuilder() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
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
    // db configuration ends

    // transaction configuration starts
    @Bean(name = "txManager")
    public PlatformTransactionManager txManager() {
        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager();
        platformTransactionManager.setDataSource(dataSource());
        return platformTransactionManager;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager();
        platformTransactionManager.setDataSource(dataSource());
        return platformTransactionManager;
    }

    @Bean(name = "springJdbcTransactionManager")
    public PlatformTransactionManager springJdbcTransactionManager() {
        DataSourceTransactionManager platformTransactionManager = new DataSourceTransactionManager();
        platformTransactionManager.setDataSource(springJdbcDataSource());
        return platformTransactionManager;
    }

    @Bean(name = "PROPAGATION_REQUIRED")
    public SpringTransactionPolicy getSpringTransaction() {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(txManager());
        springTransactionPolicy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return springTransactionPolicy;
    }

    @Bean(name = "PROPAGATION_REQUIRES_NEW")
    public SpringTransactionPolicy propagationRequiresNew() {
        SpringTransactionPolicy springTransactionPolicy = new SpringTransactionPolicy();
        springTransactionPolicy.setTransactionManager(txManager());
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
    CommonDataOtherCategoriesRouteTask commonDataOtherCategoriesRouteTask() {
        return new CommonDataOtherCategoriesRouteTask();
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
        return new SpringCamelContext(applicationContext);
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
