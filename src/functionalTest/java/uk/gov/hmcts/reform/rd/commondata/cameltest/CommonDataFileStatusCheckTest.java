package uk.gov.hmcts.reform.rd.commondata.cameltest;

import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataSpringBatchConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {CommonDataCamelConfig.class, CommonDataSpringBatchConfig.class,
    CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@ExtendWith(SpringExtension.class)
@WithTags({@WithTag("testType:Functional")})
class CommonDataFileStatusCheckTest extends CommonDataFunctionalBaseTest {

    @Value("${truncate-audit}")
    protected String truncateAudit;

    @Value("${truncate-exception}")
    protected String truncateException;

    @Value("${select-dataload-scheduler-failure}")
    String commonDataAuditSqlFailure;

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletStaleFileErrorDay2WithKeepingDay1Data() throws Exception {
        //Day 1 happy path
        uploadFiles(String.valueOf(new Date(System.currentTimeMillis()).getTime()));

        JobParameters params = new JobParametersBuilder()
            .addString(jobLauncherTestUtils.getJob().getName(), UUID.randomUUID().toString())
            .toJobParameters();
        dataIngestionLibraryRunner.run(jobLauncherTestUtils.getJob(), params);
        deleteFile();
        deleteAuditAndExceptionDataOfDay1();

        //Day 2 stale files
        uploadFiles(String.valueOf(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).getTime()));

        //not ran with dataIngestionLibraryRunner to set stale file via camelContext.getGlobalOptions()
        //.remove(SCHEDULER_START_TIME);
        params = new JobParametersBuilder()
            .addString(jobLauncherTestUtils.getJob().getName(), UUID.randomUUID().toString())
            .toJobParameters();
        jobLauncherTestUtils.launchJob(params);
        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "(.+?) not loaded due to file stale error"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair);
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(5, result.size());
        assertEquals(5, jdbcTemplate.queryForList(commonDataAuditSqlFailure).size());
        List<Map<String, Object>> judicialUserRoleType = jdbcTemplate.queryForList(flagServiceSelectData);
        assertFalse(judicialUserRoleType.isEmpty());
    }

    private void deleteFile() throws Exception {
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME, false);
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletNoFileErrorDay2WithKeepingDay1Data() throws Exception {
        //Day 1 happy path
        uploadFiles(String.valueOf(new Date(System.currentTimeMillis()).getTime()));

        JobParameters params = new JobParametersBuilder()
            .addString(jobLauncherTestUtils.getJob().getName(), UUID.randomUUID().toString())
            .toJobParameters();
        dataIngestionLibraryRunner.run(jobLauncherTestUtils.getJob(), params);
        deleteFile();
        deleteAuditAndExceptionDataOfDay1();

        //Day 2 no upload file
        camelContext.getGlobalOptions().put(
            SCHEDULER_START_TIME,
            String.valueOf(new Date(System.currentTimeMillis()).getTime())
        );
        params = new JobParametersBuilder()
            .addString(jobLauncherTestUtils.getJob().getName(), UUID.randomUUID().toString())
            .toJobParameters();
        jobLauncherTestUtils.launchJob(params);
        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "FlagService-test.csv file does not exist in azure storage account"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 1);
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(5, result.size());
        assertEquals(5, jdbcTemplate.queryForList(commonDataAuditSqlFailure).size());
        List<Map<String, Object>> flagCodes = jdbcTemplate.queryForList(flagServiceSelectData);
        assertThat(flagCodes).isNotEmpty().hasSize(3);
    }

    private void deleteAuditAndExceptionDataOfDay1() {
        jdbcTemplate.execute(truncateAudit);
        jdbcTemplate.execute(truncateException);
        SpringStarter.getInstance().restart();
    }

    private void uploadFiles(String time) throws Exception {
        camelContext.getGlobalOptions().put(SCHEDULER_START_TIME, time);
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagDetails/flag_details.csv"))
        );
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success.csv"))
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_DETAILS_FILE_NAME);
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME);
    }
}
