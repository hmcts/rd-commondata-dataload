package uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport;

import org.apache.camel.CamelContext;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.data.ingestion.DataIngestionLibraryRunner;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ArchiveFileProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.data.ingestion.camel.service.AuditServiceImpl;
import uk.gov.hmcts.reform.data.ingestion.camel.service.IEmailService;
import uk.gov.hmcts.reform.data.ingestion.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCaseLinkingRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataFlagServiceRouteTask;
import uk.gov.hmcts.reform.rd.commondata.camel.task.CommonDataCategoriesRouteTask;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.jdbc.core.BeanPropertyRowMapper.newInstance;

@ExtendWith(SpringExtension.class)
public abstract class CommonDataFunctionalBaseTest {

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    @Qualifier("springJdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DataLoadRoute parentRoute;

    @Value("${archival-cred}")
    protected String archivalCred;

    @Value("${flag-service-select-sql}")
    protected String flagServiceSelectData;

    @Value("${list-of-values-select-sql}")
    protected String listOfValuesSelectData;

    @Value("${audit-enable}")
    protected Boolean auditEnable;

    @Autowired
    protected DataLoadUtil dataLoadUtil;

    @Autowired
    protected ExceptionProcessor exceptionProcessor;

    @Autowired
    protected IEmailService emailService;

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Value("${exception-select-query}")
    protected String exceptionQuery;

    @Value("${ordered-exception-select-query}")
    protected String orderedExceptionQuery;

    @Value("${select-dataload-scheduler}")
    protected String auditSchedulerQuery;

    @Autowired
    protected CommonDataBlobSupport commonDataBlobSupport;

    @Autowired
    protected DataIngestionLibraryRunner dataIngestionLibraryRunner;

    @Autowired
    protected AuditServiceImpl auditService;

    @Autowired
    protected ArchiveFileProcessor archiveFileProcessor;

    @Autowired
    protected CommonDataFlagServiceRouteTask commonDataFlagServiceRouteTask;

    @Autowired
    protected CommonDataCategoriesRouteTask commonDataCategoriesRouteTask;

    @Autowired
    protected CommonDataCaseLinkingRouteTask commonDataCaseLinkingRouteTask;


    public static final String UPLOAD_FLAG_SERVICE_FILE_NAME = "FlagService-test.csv";

    public static final String UPLOAD_LIST_OF_VALUES_FILE_NAME = "ListOfValues-test.csv";

    public static final String UPLOAD_CASE_LINKING_FILE_NAME = "CaseLinking-test.csv";


    @BeforeEach
    public void setUpSpringContext() throws Exception {
        new TestContextManager(getClass()).prepareTestInstance(this);
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        SpringStarter.getInstance().init(testContextManager);
    }

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("azure.storage.account-key",
                           "0+VBTzfcEU7KaatWUgruR8lxJHCtoXuXZQcbPPcxp8WSTa2c3K4nWFdrNr2b45UpMJePHkZ0I2SatzDlC4e93w==");
        System.setProperty("azure.storage.account-name", "rdpreview");
        System.setProperty("RD_LD_SDK_KEY", "sdk-b1310c4a-e522-4512-9e68-d75d23b04c5d");
        System.setProperty("OAUTH2_CLIENT_AUTH",
                           "cmQtcHJvZmVzc2lvbmFsLWFwaTpjYzVmMmE2LTk2OTAtMTFlOS1iYzQyLTUyNmFmNzc2NGY2NA==");
        System.setProperty("OAUTH2_AUTH", "YWRtaW4ucmVmZGF0YUBobWN0cy5uZXQ6Q2hhbjllLW1lCgo=");
        System.setProperty("azure.storage.container-name", "rd-common-data");
    }

    protected void validateFlagServiceFile(JdbcTemplate jdbcTemplate, String serviceSql,
                                           List<FlagService> exceptedResult, int size) {
        var rowMapper = newInstance(FlagService.class);
        var flagServices = jdbcTemplate.query(serviceSql, rowMapper);
        assertEquals(size, flagServices.size());
        assertEquals(exceptedResult, flagServices);
    }

    protected void validateListOfValuesFile(JdbcTemplate jdbcTemplate, String serviceSql,
                                            List<Categories> exceptedResult, int size) {
        RowMapper<Categories> rowMapper = (rs, rowNum) -> {
            Categories categories = new Categories();
            categories.setActive(rs.getString("active"));
            categories.setCategoryKey(rs.getString("categorykey"));
            categories.setHintTextEN(rs.getString("hinttext_en"));
            categories.setHintTextCY(rs.getString("hinttext_cy"));
            categories.setKey(rs.getString("key"));
            categories.setValueCY(rs.getString("value_cy"));
            categories.setValueEN(rs.getString("value_en"));
            categories.setLovOrder(rs.getString("lov_order"));
            categories.setServiceId(rs.getString("serviceid"));
            categories.setParentCategory(rs.getString("parentcategory"));
            categories.setParentKey(rs.getString("parentkey"));
            return categories;
        };
        var listOfValues = jdbcTemplate.query(serviceSql, rowMapper);
        assertEquals(size, listOfValues.size());
        assertEquals(exceptedResult, listOfValues);
    }

    protected void validateFlagServiceFileAudit(JdbcTemplate jdbcTemplate,
                                                String auditSchedulerQuery, String status, String fileName) {
        var result = jdbcTemplate.queryForList(auditSchedulerQuery);
        assertEquals(3, result.size());
        Optional<Map<String, Object>> auditEntry =
            result.stream().filter(audit -> audit.containsValue(fileName)).findFirst();
        assertTrue(auditEntry.isPresent());
        auditEntry.ifPresent(audit -> assertEquals(status, audit.get("status")));
    }

    @SuppressWarnings("unchecked")
    protected void validateFlagServiceFileJsrException(JdbcTemplate jdbcTemplate,
                                                       String exceptionQuery, int size, String tableName,
                                                       Quartet<String, String, String, Long>... quartets) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertEquals(result.size(), size);

        List<Map<String, Object>> actualResult = result.stream()
            .filter(exception -> exception.containsValue(tableName))
            .collect(Collectors.toUnmodifiableList());
        int numberOfMatchingErrors = 0;
        for (Map<String, Object> currResult : actualResult) {
            for (Quartet<String, String, String, Long> quartet : quartets) {
                if (quartet.getValue1().equals(currResult.get("error_description"))) {
                    numberOfMatchingErrors++;
                    assertEquals(quartet.getValue0(), currResult.get("field_in_error"));
                    assertEquals(quartet.getValue2(), currResult.get("key"));
                    assertEquals(quartet.getValue3(), currResult.get("row_id"));
                }
            }
        }
        assertEquals(numberOfMatchingErrors, quartets.length);
    }

    @SuppressWarnings("unchecked")
    protected void validateFlagServiceFileException(JdbcTemplate jdbcTemplate,
                                                    String exceptionQuery,
                                                    Pair<String, String> pair,
                                                    int index) {
        var result = jdbcTemplate.queryForList(exceptionQuery);
        assertThat(
            (String) result.get(index).get("error_description"),
            containsString(pair.getValue1())
        );
    }
}
