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
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagDetailsRouteTask;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {CommonDataCamelConfig.class, CamelTestContextBootstrapper.class,
    BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@SpringBootTest
@SpringBatchTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
public class CommonDataFlagDetailsLoadTest extends CommonDataFunctionalBaseTest {

    @Autowired
    CommonDataFlagDetailsRouteTask commonDataFlagDetailsRouteTask;

    @Autowired
    @Qualifier("txManager")
    protected PlatformTransactionManager platformTransactionManager;

    private static final String FLAG_DETAILS_TABLE_NAME = "flag_details";

    private static final String HEADER_MISMATCH_MESSAGE
        = "There is a mismatch in the headers of the csv file :: FlagDetails-test.csv";
    private static final String FAILURE_MESSAGE = "Failure";

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file in to a clean flag_details table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_success() throws Exception {
        testFlagDetailsInsertion(
            "flag_details_success.csv",
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file with different headers in different cases "
        + "in to a clean flag_details table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_TestCaseInsensitiveHeaders_Success() throws Exception {
        String fileName = "flag_details_success_insensitive_headers.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has headers and data enclosed within quotes"
        + " in to a clean flag_details table")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithQuotes_Success() throws Exception {
        String fileName = "flag_details_success_with_quotes.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a leading "
        + "trailing white space")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagDetailsCsv_WithLeadingTrailingSpace_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagDetails/flag_details_success_leadingspace.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagDetailsFile(jdbcTemplate, flagDetailsSelectData, List.of(
            buildFlagDetailsLoadObject()
        ), 1);
        //Validates Success Audit
        validateFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with a missing header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testFlagDetailsCsv_MissingHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagDetails/flag_details_failure_unknown_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagDetails = jdbcTemplate.queryForList(flagDetailsSelectData);
        assertEquals(0, flagDetails.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            HEADER_MISMATCH_MESSAGE
        );
        validateFlagDetailsFileFailureException(jdbcTemplate, orderedExceptionQuery, pair, 3);
        validateFileAudit(jdbcTemplate, auditSchedulerQuery, FAILURE_MESSAGE, UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    protected void validateFlagDetailsFile(JdbcTemplate jdbcTemplate, String flagDetailsSql,
                                           List<FlagDetails> exceptedResult, int size) {
        var rowMapper = newInstance(FlagDetails.class);
        var flagDetails = jdbcTemplate.query(flagDetailsSql, rowMapper);
        assertEquals(size, flagDetails.size());
        assertEquals(exceptedResult, flagDetails);
    }

    private FlagDetails buildFlagDetailsLoadObject() {
        return FlagDetails.builder().id("1").flagCode("CF0001").valueEn("Case").valueCy("").categoryId("0")
            .mrdCreatedTime("2022-04-07 12:43:00").mrdUpdatedTime("2022-06-17 13:33:00").build();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has an entry with missing flag code")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithMissingFlagCode_PartialSuccess() throws Exception {
        String fileName = "flag_details_partial_success_missing_flag_code.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(1, result.size());

        validateFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_DETAILS_FILE_NAME
        );

        Quartet<String, String, String, Long> quartet = Quartet.with("flagCode","Flag Code is missing","3",4L);
        validateFlagDetailsFileJsrException(jdbcTemplate, orderedExceptionQuery, quartet);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has an entry with missing value_en")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithMissingValueEn_PartialSuccess() throws Exception {
        String fileName = "flag_details_partial_success_missing_value_en.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(1, result.size());

        validateFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_DETAILS_FILE_NAME
        );

        Quartet<String, String, String, Long> quartet = Quartet.with("valueEn","Value_EN is missing","3",4L);
        validateFlagDetailsFileJsrException(jdbcTemplate, orderedExceptionQuery, quartet);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has an entry with missing category "
        + "Id")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithMissingCategoryId_PartialSuccess() throws Exception {
        String fileName = "flag_details_partial_success_missing_categoryId.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(1, result.size());

        validateFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_DETAILS_FILE_NAME
        );

        Quartet<String, String, String, Long> quartet = Quartet.with("categoryId","Category_ID is missing","3",4L);
        validateFlagDetailsFileJsrException(jdbcTemplate, orderedExceptionQuery, quartet);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has an entry with missing id")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithMissingId_PartialSuccess() throws Exception {
        String fileName = "flag_details_partial_success_missing_id.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(1, result.size());

        validateFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_DETAILS_FILE_NAME
        );

        Quartet<String, String, String, Long> quartet = Quartet.with("id","ID is missing","",4L);
        validateFlagDetailsFileJsrException(jdbcTemplate, orderedExceptionQuery, quartet);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Status: PartialSuccess - Test for loading a valid Csv file which has expired records")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testFlagDetailsCsv_WithExpiredRecords_PartialSuccess() throws Exception {
        String fileName = "flag_details_expired_record_partial_success.csv";
        testFlagDetailsInsertion(
            fileName,
            MappingConstants.PARTIAL_SUCCESS
        );
        var result = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(1, result.size());

        validateFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            MappingConstants.PARTIAL_SUCCESS,
            UPLOAD_FLAG_DETAILS_FILE_NAME
        );

        validateFlagDetailsFileLoad(List.of(
            FlagDetails.builder().id("1").flagCode("CF0001").valueEn("Case").valueCy("").categoryId("0")
                .mrdCreatedTime("2022-04-07 12:43:00").mrdUpdatedTime("2022-06-17 13:33:00").build(),
            FlagDetails.builder().id("2").flagCode("PF0001").valueEn("Party").valueCy("").categoryId("0")
                .mrdCreatedTime("2022-04-07 12:43:00").mrdUpdatedTime("2022-06-17 13:33:00").build()
        ));

        Quartet<String, String, String, Long> quartet = Quartet.with("", "Record is expired", "PF0001", 4L);
        validateFlagDetailsFileJsrException(jdbcTemplate, exceptionQuery, quartet);
    }

    @Test
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testTaskletFailure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagDetails/flag_details_failure.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var flagDetails = jdbcTemplate.queryForList(flagDetailsSelectData);
        assertEquals(0, flagDetails.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            "No records have been defined in the CSV"
        );
        validateFlagDetailsFileFailureException(jdbcTemplate, orderedExceptionQuery, pair, 3);
        validateFileAudit(jdbcTemplate, auditSchedulerQuery, FAILURE_MESSAGE, UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    void validateFileAudit(JdbcTemplate jdbcTemplate, String auditSchedulerQuery, String status, String fileName) {
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(5, result.size());
        Optional<Map<String, Object>> auditEntry =
            result.stream().filter(audit -> audit.containsValue(fileName)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> assertEquals(status, audit.get("status")));
    }

    protected void validateFlagDetailsFileFailureException(JdbcTemplate jdbcTemplate, String exceptionQuery,
                                                           Pair<String, String> pair, int index) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertTrue(result.stream().map(a -> a.get("error_description").toString()).toList().contains(pair.getValue1()));
    }

    @SuppressWarnings("unchecked")
    protected void validateFlagDetailsFileJsrException(JdbcTemplate jdbcTemplate, String exceptionQuery,
                                                       Quartet<String, String, String, Long>... quartets) {
        var result = jdbcTemplate.queryForList(exceptionQuery);

        List<Map<String, Object>> actualResult = result.stream()
            .filter(exception -> exception.containsValue(CommonDataFlagDetailsLoadTest.FLAG_DETAILS_TABLE_NAME))
            .collect(Collectors.toUnmodifiableList());
        for (Map<String, Object> currResult : actualResult) {
            for (Quartet<String, String, String, Long> quartet : quartets) {
                if (quartet.getValue1().equals(currResult.get("error_description"))) {
                    assertEquals(quartet.getValue0(), currResult.get("field_in_error"));
                    assertEquals(quartet.getValue2(), currResult.get("key"));
                    assertEquals(quartet.getValue3(), currResult.get("row_id"));
                }
            }
        }
    }

    private void testFlagDetailsInsertion(String fileName, String status) throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(String.format("classpath:sourceFiles/flagDetails/%s", fileName)))
        );

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagDetailsFileLoad(List.of(
            FlagDetails.builder().id("1").flagCode("CF0001").valueEn("Case").valueCy("").categoryId("0")
                .mrdCreatedTime("2022-04-07 12:43:00").mrdUpdatedTime("2022-06-17 13:33:00").build(),
            FlagDetails.builder().id("2").flagCode("PF0001").valueEn("Party").valueCy("").categoryId("0")
                .mrdCreatedTime("2022-04-07 12:43:00").mrdUpdatedTime("2022-06-17 13:33:00").build()
        ));
        //Validates Success Audit
        validateFileAudit(jdbcTemplate, auditSchedulerQuery, status, UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    private void validateFlagDetailsFileLoad(List<FlagDetails> expected) {
        var rowMapper = newInstance(FlagDetails.class);
        var actual =
            jdbcTemplate.query(flagDetailsSelectData, rowMapper);
        assertThat(actual)
            .hasSize(2)
            .hasSameElementsAs(expected);
    }

    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

}
