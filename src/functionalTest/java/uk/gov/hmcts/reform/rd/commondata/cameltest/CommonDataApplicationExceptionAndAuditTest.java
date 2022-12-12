package uk.gov.hmcts.reform.rd.commondata.cameltest;

import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

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
@ExtendWith(SpringExtension.class)
@WithTags({@WithTag("testType:Functional")})
@SuppressWarnings("unchecked")
class CommonDataApplicationExceptionAndAuditTest extends CommonDataFunctionalBaseTest {

    private static final String FLAG_SERVICE_TABLE_NAME = "flag_service";

    @BeforeEach
    @Sql(scripts = "/testData/commondata_insert_flag_details.sql")
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testTaskletPartialSuccessAndJsr() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_partial_success.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagServiceFile(jdbcTemplate, flagServiceSelectData, List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").defaultStatus("Requested").availableExternally("t").build(),
            FlagService.builder().ID("2").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0008").defaultStatus("Active").availableExternally("t").build(),
            FlagService.builder().ID("3").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0009").defaultStatus("Requested").availableExternally("f").build()
        ), 3);
        //Validates Success Audit
        validateFlagServiceFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            "PartialSuccess",
            UPLOAD_FLAG_SERVICE_FILE_NAME
        );

        Quartet<String, String, String, Long> quartet = Quartet.with("serviceId", "must not be empty", "4", 5L);
        validateFlagServiceFileJsrException(jdbcTemplate, exceptionQuery, 3, FLAG_SERVICE_TABLE_NAME, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletFailure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServices = jdbcTemplate.queryForList(flagServiceSelectData);
        assertEquals(0, flagServices.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "No valid Flag Service Record found in the input file. Please review and try again."
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 1);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletFailureForInvalidFlagCode() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_failure_foreignkey_violation.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServices = jdbcTemplate.queryForList(flagServiceSelectData);
        assertEquals(0, flagServices.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "flag_code does not exist in parent table"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 0);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FLAG_SERVICE_FILE_NAME);

    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME);
    }
}
