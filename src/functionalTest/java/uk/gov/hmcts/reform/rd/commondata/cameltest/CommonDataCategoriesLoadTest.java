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
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;
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
@SuppressWarnings("unchecked")

public class CommonDataCategoriesLoadTest extends CommonDataFunctionalBaseTest {

    @Autowired
    CommonDataCategoriesRouteTask commonDataCategoriesRouteTask;

    @Autowired
    @Qualifier("springJdbcTransactionManager")
    protected PlatformTransactionManager platformTransactionManager;

    private static final String CATEGORIES_TABLE_NAME = "List_Of_Values";

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
        validateFlagServiceFileException(jdbcTemplate, exceptionQuery, pair, 1);
        validateFlagServiceFileAudit(jdbcTemplate, auditSchedulerQuery, "Failure", UPLOAD_LIST_OF_VALUES_FILE_NAME);
    }

    @Test
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
