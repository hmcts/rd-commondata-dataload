package uk.gov.hmcts.reform.rd.commondata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
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
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.SCHEDULER_START_TIME;

@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@CamelSpringBootTest
@MockEndpoints("log:*")
@ContextConfiguration(classes = {CommonDataCamelConfig.class, CamelTestContextBootstrapper.class,
    JobLauncherTestUtils.class, BatchConfig.class, AzureBlobConfig.class, BlobStorageCredentials.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@SqlConfig(dataSource = "dataSource", transactionManager = "txManager",
    transactionMode = SqlConfig.TransactionMode.ISOLATED)
@SuppressWarnings("unchecked")

public class CommonDataCategoriesLoadTest extends CommonDataFunctionalBaseTest {

    @Autowired
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask;

    @Autowired
    @Qualifier("txManager")
    protected PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    public void init() {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file in to a clean list_of_values table")
    public void testListOfValuesCsv_Success() throws Exception {
        testListOfValuesInsertion(
            "list_of_values_success.csv",
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file with different headers in different cases "
        + "in to a clean list_of_values table")
    public void testListOfValuesCsv_TestCaseInsensitiveHeaders_Success() throws Exception {
        String fileName = "list_of_values_success_insensitive_headers.csv";
        testListOfValuesInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has headers and data enclosed within quotes"
        + " in to a clean list_of_values table")
    public void testListOfValuesCsv_WithQuotes_Success() throws Exception {
        String fileName = "list_of_values_success_with_quotes.csv";
        testListOfValuesInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a leading "
        + "trailing white space")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_WithLeadingTrailingSpace_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_success_leadingspace.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateListOfValuesFile(jdbcTemplate, listOfValuesSelectData, List.of(
            Categories.builder().categoryKey("AdditionalFacilities").serviceId("").key("AF-VF")
                .valueEN("Video Facility").valueCY("").hintTextEN("").hintTextCY("").parentCategory("")
                .parentKey("").active("Y").build()), 1);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a  numeric"
        + "value for CategoryKey field")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testListOfValuesCsv_WithNonNumericValueCategoryKey_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_partial_success_categoryKey_numeric.csv"))
        );

        jobLauncherTestUtils.launchJob();
        validateListOfValuesFile(jdbcTemplate, listOfValuesSelectData, List.of(
            Categories.builder().categoryKey("1").serviceId("").key("AF-WR")
                .valueEN("Witness Room").valueCY("").hintTextEN("").hintTextCY("").parentCategory("")
                .parentKey("").active("Y").build()), 1);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with an additional unknown header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_UnknownHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_failure_additional_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(0, listOfValues.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: ListOfValues-test.csv"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 3);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    //@Test - Commenting out due header validation flag becoming false
    @DisplayName("Status: Failure - Test for loading a file with a missing header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_MissingHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_failure_unknown_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(0, listOfValues.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: ListOfValues-test.csv"
        );
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 1);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    @Test
    @DisplayName("Status: Sucess - Test for LOV Duplicate records Case1.Filters duplicate records")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_DupRecord_Case1() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_duplicate_rec_case1.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateListOfValuesFile(jdbcTemplate, listOfValuesSelectData, List.of(
            Categories.builder().categoryKey("caseSubType").serviceId("BBA3").key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT scenario1").valueCY("").hintTextEN("")
                .hintTextCY("").parentCategory("caseType")
                .parentKey("BBA3-001").active("Y").build()), 1);
        //Validates Success Audit
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Success", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    @Test
    @DisplayName("Status: PartialSucess - Test for LOV Duplicate records Case2.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_DupRecord_Case2() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_duplicate_rec_case2.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(1, listOfValues.size());

        String comKeyErrorMsg = "Composite Key violation";
        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            comKeyErrorMsg
        );
        validateCategoriesFileException(jdbcTemplate, exceptionQuery, pair);
        validateCategoriesFileAudit(jdbcTemplate, auditSchedulerQuery,
                                    "PartialSuccess", UPLOAD_LIST_OF_VALUES_FILE_NAME
        );
    }

    @Test
    @DisplayName("Status: PartialSucess - Test for LOV Duplicate records Case3.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_DupRecord_Case3() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_duplicate_rec_case3.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(1, listOfValues.size());

        String comKeyErrorMsg = "Composite Key violation";
        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            comKeyErrorMsg
        );
        validateCategoriesFileException(jdbcTemplate, exceptionQuery, pair);
        validateCategoriesFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            "PartialSuccess",
            UPLOAD_LIST_OF_VALUES_FILE_NAME
        );
    }

    @Test
    @DisplayName("Status: PartialSucess - Test for LOV Duplicate records Case4.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_DupRecord_Case4() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_duplicate_rec_case4.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(1, listOfValues.size());

        String comKeyErrorMsg = "Composite Key violation";
        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            comKeyErrorMsg
        );
        validateCategoriesFileException(jdbcTemplate, exceptionQuery, pair);
        validateCategoriesFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            "PartialSuccess",
            UPLOAD_LIST_OF_VALUES_FILE_NAME
        );
    }

    @Test
    @DisplayName("Status: PartialSucess - Test for 0 byte characters.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_0_byte_character() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_0_byte_character.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(3, listOfValues.size());

        String zer0ByteCharacterErrorMsg = "Zero byte characters identified - check source file";
        Pair<String, String> pair = new Pair<>(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            zer0ByteCharacterErrorMsg
        );
        validateCategoriesFileException(jdbcTemplate, exceptionQuery, pair);
        validateCategoriesFileAudit(
            jdbcTemplate,
            auditSchedulerQuery,
            "Failure",
            UPLOAD_LIST_OF_VALUES_FILE_NAME
        );
    }

    @Test
    @DisplayName("To validate UTF-8 LOV csv file with Unicode char in header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testListOfValuesCsv_With_Unicode_Header() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_utf8_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);

        assertEquals(3, listOfValues.size());
        assertEquals("AdditionalFacilities",listOfValues.get(0).get("categorykey"));
        assertEquals("AF-WR",listOfValues.get(0).get("key"));
        assertEquals("Witness Room",listOfValues.get(0).get("value_en"));
        assertEquals("Y",listOfValues.get(0).get("active"));



    }

    @Test
    @DisplayName("Status: Success - Test for loading a file with records of same composite key both active=D"
        + "value for CategoryKey field")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testListOfValuesCsv_WithDelete_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_delete_success.csv"))
        );

        jobLauncherTestUtils.launchJob();

        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        //all records marked as D hence all deleted
        assertEquals(0, listOfValues.size());

        //Validates audit
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(5, result.size());
        Optional<Map<String, Object>> auditEntry =
            result.stream().filter(audit -> audit.containsValue(UPLOAD_LIST_OF_VALUES_FILE_NAME)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> assertEquals("PartialSuccess", audit.get("status")));

        //validate exceptons
        var resultExceptions = jdbcTemplate.queryForList(exceptionRecordsQuery);
        assertEquals(3, resultExceptions.size());
        resultExceptions.forEach(p -> assertEquals(p.get("error_description"),
            "Record is deleted as Active flag was 'D'"));
    }


    protected void validateCategoriesFileAudit(JdbcTemplate jdbcTemplate,
                                                String auditSchedulerQuery, String status, String fileName) {
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(5, result.size());
        Optional<Map<String, Object>> auditEntry =
            result.stream().filter(audit -> audit.containsValue(fileName)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> assertEquals(status, audit.get("status")));
    }

    protected void validateCategoriesFileException(JdbcTemplate jdbcTemplate,
                                                    String exceptionQuery,
                                                    Pair<String, String> pair) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertTrue(result.stream().map(a -> a.get("error_description").toString()).toList().contains(pair.getValue1()));

    }


    private void testListOfValuesInsertion(String fileName, String status) throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(String.format("classpath:sourceFiles/categories/%s", fileName)))
        );

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();

    }



    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }



}
