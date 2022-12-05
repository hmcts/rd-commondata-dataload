package uk.gov.hmcts.reform.rd.commondata.cameltest;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.CommonDataFunctionalBaseTest;
import uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport.SpringStarter;
import uk.gov.hmcts.reform.rd.commondata.config.CommonDataCamelConfig;
import uk.gov.hmcts.reform.rd.commondata.configuration.BatchConfig;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;
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
public class CommonDataFlagDetailsLoadTest extends CommonDataFunctionalBaseTest {

    private static final String FLAG_DETAILS_TABLE_NAME = "flag_details";
    private static final String HEADER_MISMATCH_MESSAGE
        = "There is a mismatch in the headers of the csv file :: FlagService-test.csv";
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

    private void testFlagDetailsInsertion(String fileName, String status) throws Exception {
        commonDataBlobSupport.uploadFile(
            UPLOAD_LIST_OF_VALUES_FILE_NAME,
            new FileInputStream(getFile(String.format("classpath:sourceFiles/flagDetails/%s", fileName)))
        );

        camelContext.getGlobalOptions()
            .put(SCHEDULER_START_TIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        jobLauncherTestUtils.launchJob();
        //Validate Success Result
        validateFlagDetailsFileLoad(List.of(
            FlagDetails.builder().id("1").flagCode("CF0001").valueEn("Case").valueCy("").categoryId("0")
                .mrdCreatedTime("07-04-2022 12:43:00").mrdUpdatedTime("17-06-2022 13:33:00").mrdDeletedTime("").build(),
            FlagDetails.builder().id("2").flagCode("PF0001").valueEn("Party").valueCy("").categoryId("0")
                .mrdCreatedTime("07-04-2022 12:43:00").mrdUpdatedTime("17-06-2022 13:33:00").mrdDeletedTime("").build()
          ));
        //Validates Success Audit
        validateFlagDetailsFileAudit(jdbcTemplate, auditSchedulerQuery, status, UPLOAD_FLAG_DETAILS_FILE_NAME);
    }

    private void validateFlagDetailsFileLoad(List<FlagDetails> expected) {
        var rowMapper = newInstance(FlagDetails.class);
        var actual =
            jdbcTemplate.query(flagDetailsSelectData, rowMapper);
        assertThat(actual)
            .hasSize(2)
            .hasSameElementsAs(expected);
    }
}
