package uk.gov.hmcts.reform.rd.commondata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.javatuples.Pair;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
@SuppressWarnings("unchecked")

public class CommonDataCaseLinkingLoadTest extends CommonDataFunctionalBaseTest {

    @Autowired
    CommonDataCaseLinkingRouteTask commonDataCaseLinkingRouteTask;

    @Autowired
    @Qualifier("txManager")
    protected PlatformTransactionManager platformTransactionManager;

    private static final String CATEGORIES_TABLE_NAME = "List_Of_Values";

    @BeforeEach
    public void init() throws Exception {
        SpringStarter.getInstance().restart();
        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        commonDataBlobSupport.uploadFile(
            UPLOAD_FLAG_DETAILS_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/flagDetails/flag_details.csv"))
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file in to a clean list_of_values table")
    public void testCaseLinkingCsv_Success() throws Exception {
        testCaseLinkingInsertion(
            "case_linking_success.csv",
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file with different headers in different cases "
        + "in to a clean list_of_values table")
    public void testCaseLinkingCsv_TestCaseInsensitiveHeaders_Success() throws Exception {
        String fileName = "case_linking_success_insensitive_headers.csv";
        testCaseLinkingInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has headers and data enclosed within quotes"
        + " in to a clean list_of_values table")
    public void testCaseLinkingCsv_WithQuotes_Success() throws Exception {
        String fileName = "case_linking_success_with_quotes.csv";
        testCaseLinkingInsertion(
            fileName,
            MappingConstants.SUCCESS
        );
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a leading "
        + "trailing white space")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testCaseLinkingCsv_WithLeadingTrailingSpace_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_CASE_LINKING_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/categories/list_of_values_success_leadingspace.csv"))
        );

        jobLauncherTestUtils.launchJob();
        //Validate Success Result
    }

    @Test
    @DisplayName("Status: Success - Test for loading a valid Csv file which has a  numeric"
        + "value for CategoryKey field")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    public void testCaseLinkingCsv_WithNonNumericValueCategoryKey_Success() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_CASE_LINKING_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/caseLinking/case_linking_partial_success_categoryKey_numeric.csv"))
        );

        jobLauncherTestUtils.launchJob();

    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with an additional unknown header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testCaseLinkingCsv_UnknownHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_CASE_LINKING_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/caseLinking/case_linking_failure_additional_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(0, listOfValues.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_CASE_LINKING_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: CaseLinking-test.csv"
        );

    }

    @Test
    @DisplayName("Status: Failure - Test for loading a file with a missing header.")
    @Sql(scripts = {"/testData/commondata_truncate.sql"})
    void testCaseLinkingCsv_MissingHeader_Failure() throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_CASE_LINKING_FILE_NAME,
            new FileInputStream(getFile(
                "classpath:sourceFiles/caseLinking/case_linking_failure_unknown_header.csv"))
        );

        jobLauncherTestUtils.launchJob();
        var listOfValues = jdbcTemplate.queryForList(listOfValuesSelectData);
        assertEquals(0, listOfValues.size());

        Pair<String, String> pair = new Pair<>(
            UPLOAD_CASE_LINKING_FILE_NAME,
            "There is a mismatch in the headers of the csv file :: CaseLinking-test.csv"
        );

    }

    private void testCaseLinkingInsertion(String fileName, String status) throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_CASE_LINKING_FILE_NAME,
            new FileInputStream(getFile(String.format("classpath:sourceFiles/caseLinking/%s", fileName)))
        );

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();

    }



    @AfterEach
    void tearDown() throws Exception {
        //Delete Uploaded test file with Snapshot delete
        commonDataBlobSupport.deleteBlob(UPLOAD_CASE_LINKING_FILE_NAME);
    }



}
