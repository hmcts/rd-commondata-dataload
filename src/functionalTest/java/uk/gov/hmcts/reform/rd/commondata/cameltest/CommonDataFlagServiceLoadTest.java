package uk.gov.hmcts.reform.rd.commondata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;
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
public class CommonDataFlagServiceLoadTest extends CommonDataFunctionalBaseTest {

    @Autowired
    @Qualifier("springJdbcTransactionManager")
    protected PlatformTransactionManager platformTransactionManager;

    private static final String FLAG_SERVICE_TABLE_NAME = "flag_service";

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file in to a clean flag_service table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServiceCsv_Success() throws Exception {
        testFlagServiceInsertion(
            "flag_service_success.csv",
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file with different headers in different cases "
        + "in to a clean flag_service table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServiceCsv_TestCaseInsensitiveHeaders_Success() throws Exception {
        String fileName = "flag_service_success_insensitive_headers.csv";
        testFlagServiceInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has headers and data enclosed within quotes"
        + " in to a clean flag_service table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServiceCsv_WithQuotes_Success() throws Exception {
        String fileName = "flag_service_success_with_quotes.csv";
        testFlagServiceInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Partial Success - Test for loading a valid Csv file which has entries "
        + "missing a few non-mandatory fields")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServiceCsv_WithEmptyServiceId_PartialSuccess() throws Exception {
        String fileName = "flag_service_partial_success.csv";
        testFlagServiceInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        //Validates Success Audit
        validateFlagServiceFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_SERVICE_FILE_NAME
        );
    }

    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has a combination of "
        + "valid entries and entries missing a mandatory field")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServicesCsv_WithMissingMandatoryValue_PartialSuccess() throws Exception {
        String fileName = "flag_service_partial_success.csv";
        testFlagServiceInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has a string "
        + "instead of boolean")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServicesCsv_WithStringValueInsteadOfBoolean_PartialSuccess() throws Exception {
        String fileName = "flag_service_partial_hearingrelevant_string.csv";
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                String.format("classpath:sourceFiles/flagService/%s", fileName)))
        );
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagServiceFileLoad(List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build()
        ), 1);
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a leading "
        + "trailing white space")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagServiceCsv_WithLeadingTrailingSpace_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_success_leadingspace.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagServiceFile(jdbcTemplate, flagServiceSelectData, List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build()
        ), 1);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has a non numeric"
        + "value for ID field")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagServicesCsv_WithNonNumericValueID_PartialSuccess() throws Exception {
        String fileName = "flag_service_partial_success_id_notnumeric.csv";
        testFlagServiceInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        Quartet<String, String, String, Long> quartet = Quartet.with("ID", "allowed numeric value only", "hello", 5L);
        validateFlagServiceFileJsrException(jdbcTemplate, exceptionQuery, 3, FLAG_SERVICE_TABLE_NAME, quartet);

    }


    @Test
    @DisplayName("Status: Failure - Test for loading a file with an additional unknown header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagServiceCsv_UnknownHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_failure_additional_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServices = jdbcTemplate.queryForList(flagServiceSelectData);
        assertEquals(0, flagServices.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: FlagService-test.csv"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 0);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with a missing header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagServiceCsv_MissingHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_failure_unknown_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServices = jdbcTemplate.queryForList(flagServiceSelectData);
        assertEquals(0, flagServices.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: FlagService-test.csv"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 0);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with the headers in jumbled order.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagServiceCsv_HeaderInJumbledOrder_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagService/flag_service_failure_jumbled_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagServices = jdbcTemplate.queryForList(flagServiceSelectData);
        assertEquals(0, flagServices.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: FlagService-test.csv"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 0);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    private void testFlagServiceInsertion(String fileName, String status) throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_SERVICE_FILE_NAME,
            new FileInputStream(getFile(String.format("classpath:sourceFiles/flagService/%s", fileName)))
        );

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagServiceFileLoad(List.of(
            FlagService.builder().ID("1").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0004").build(),
            FlagService.builder().ID("2").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0008").build(),
            FlagService.builder().ID("3").serviceId("xxxxx").hearingRelevant("f").requestReason("t").flagCode(
                "RA0009").build()
        ), 3);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, status, UPLOAD_FLAG_SERVICE_FILE_NAME);
    }

    private void validateFlagServiceFileLoad(List<FlagService> expected, int size) {
        var rowMapper = newInstance(FlagService.class);
        var actual =
            jdbcTemplate.query(flagServiceSelectData, rowMapper);
        assertThat(actual)
            .hasSize(size)
            .hasSameElementsAs(expected);
    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_SERVICE_FILE_NAME);
    }
}
