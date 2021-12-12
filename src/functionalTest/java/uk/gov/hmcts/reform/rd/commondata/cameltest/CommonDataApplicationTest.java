package uk.gov.hmcts.reform.rd.commondata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataIntegrationBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {CommonDataCamelConfig.class, CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@SuppressWarnings("unchecked")
class CommonDataApplicationTest extends CommonDataIntegrationBaseTest {

    @Value("${commondata-flag-service-start-route}")
    String startRoute;

    @Value("${archival-route}")
    String archivalRoute;

    @Autowired
    @Qualifier("springJdbcTransactionManager")
    protected PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    /**
     * Day 1 Success Scenerio.
     *
     * @throws Exception Exception
     */
    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletSuccessDay1() throws Exception {
        testInsertion();
    }

    /**
     * Day2 Success Scenerio.
     *
     * @throws Exception Exception
     */
    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletSuccessWithInsertAndTruncateInsertDay2() throws Exception {
        testInsertion();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        jdbcTemplate.update("delete from DATALOAD_SCHEDULAR_AUDIT");
        TransactionStatus status = platformTransactionManager.getTransaction(def);
        platformTransactionManager.commit(status);
        SpringStarter.getInstance().restart();

        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success_day2.csv"))
        );
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));

        jobLauncherTestUtils.launchJob();

        validateFlagServiceFile(jdbcTemplate, flagServiceSelectData, List.of(
            FlagService.builder().ID("4").serviceId("day2").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build()
        ), 1);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    private void testInsertion() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagServiceFile(jdbcTemplate, flagServiceSelectData, List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build(),
            FlagService.builder().ID("2").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0008").build(),
            FlagService.builder().ID("3").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0009").build()
        ), 3);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletIdempotent() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success.csv"))
        );
        JobParameters params = new JobParametersBuilder()
            .addString(jobLauncherTestUtils.getJob().getName(), String.valueOf(System.currentTimeMillis()))
            .toJobParameters();
        dataIngestionLibraryRunner.run(jobLauncherTestUtils.getJob(), params);
        SpringStarter.getInstance().restart();

        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success_day2.csv"))
        );
        List<Map<String, Object>> auditDetails = jdbcTemplate.queryForList(auditSchedulerQuery);
        final Timestamp timestamp = (Timestamp) auditDetails.get(0).get("scheduler_end_time");
        dataIngestionLibraryRunner.run(jobLauncherTestUtils.getJob(), params);
        //Validate Success Result
        validateFlagServiceFile(jdbcTemplate, flagServiceSelectData, List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build(),
            FlagService.builder().ID("2").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0008").build(),
            FlagService.builder().ID("3").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0009").build()
        ), 3);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FLAG_SERVICE_FILE_NAME);
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME);
        dataIngestionLibraryRunner.run(jobLauncherTestUtils.getJob(), params);
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success_day2.csv"))
        );

        List<Map<String, Object>> auditDetailsNextRun = jdbcTemplate.queryForList(auditSchedulerQuery);
        final Timestamp timestampNextRun = (Timestamp) auditDetailsNextRun.get(0).get("scheduler_end_time");
        assertEquals(timestamp, timestampNextRun);
    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME);
    }
}
