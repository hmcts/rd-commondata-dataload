package uk.gov.hmcts.reform.rd.commondata.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.spi.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.FileStatus;
import uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.FILE_NAME;

@SuppressWarnings("unchecked")
class CommonDataDRecordsTest {


    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    CamelContext camelContext = mock(CamelContext.class);

    ExtendedCamelContext extendedCamelContext = mock(ExtendedCamelContext.class);

    JsrValidatorInitializer<OtherCategories> lovServiceJsrValidatorInitializer = mock(JsrValidatorInitializer.class);

    @InjectMocks
    CommonDataDRecords commonDataDRecords = new CommonDataDRecords();


    CommonDataExecutor commonDataExecutor = mock(CommonDataExecutor.class);

    @BeforeEach
    void init() {

        setField(commonDataDRecords, "lovServiceJsrValidatorInitializer", lovServiceJsrValidatorInitializer);
        setField(commonDataDRecords, "camelContext", camelContext);
        setField(commonDataDRecords, "jdbcTemplate", jdbcTemplate);

        when(camelContext.getCamelContextExtension()).thenReturn(extendedCamelContext);
    }


    @Test
    void testExecute_NoDCategoriestoDelete() {

        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenReturn(Collections.emptyList());
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(0);

        commonDataDRecords.auditAndDeleteCategories();

        verify(lovServiceJsrValidatorInitializer,times(1))
            .auditJsrExceptions(anyList(),any(),anyString(),any());

    }

    @Test
    void testExecute_DCategoriestoDelete_withFailureStatus() {

        Categories expected = Categories.builder().categoryKey("value returned by query")
            .serviceId("value returned by query")
            .key("value returned by query").build();
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenAnswer((Answer<List<Categories>>) invocation -> {
                // Fetch the method arguments
                Object[] args = invocation.getArguments();

                // Create a mock result set and setup an expectation on it
                ResultSet rs = mock(ResultSet.class);
                when(rs.getString(anyString())).thenReturn("value returned by query");

                // Fetch the row mapper instance from the arguments
                RowMapper<Categories> rm = (RowMapper<Categories>) args[1];
                // Invoke the row mapper
                Categories actual = rm.mapRow(rs, 0);

                // Assert the result of the row mapper execution
                Assertions.assertEquals(expected, actual);

                // Return your created list for the template#query call
                return List.of(expected);
            });
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);

        FileStatus fileStatus = mockFileStatus(MappingConstants.FAILURE);
        Assertions.assertEquals(MappingConstants.FAILURE, fileStatus.getAuditStatus());

        commonDataDRecords.auditAndDeleteCategories();

        Assertions.assertEquals(MappingConstants.FAILURE, fileStatus.getAuditStatus());

        verify(lovServiceJsrValidatorInitializer,times(1))
            .auditJsrExceptions(anyList(),any(),anyString(),any());

    }

    @Test
    void testExecute_DCategoriestoDelete_withOutStatus() {

        Categories expected = Categories.builder().categoryKey("value returned by query")
            .serviceId("value returned by query")
            .key("value returned by query").build();
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenAnswer((Answer<List<Categories>>) invocation -> {
                // Fetch the method arguments
                Object[] args = invocation.getArguments();

                // Create a mock result set and setup an expectation on it
                ResultSet rs = mock(ResultSet.class);
                when(rs.getString(anyString())).thenReturn("value returned by query");

                // Fetch the row mapper instance from the arguments
                RowMapper<Categories> rm = (RowMapper<Categories>) args[1];
                // Invoke the row mapper
                Categories actual = rm.mapRow(rs, 0);

                // Assert the result of the row mapper execution
                Assertions.assertEquals(expected, actual);

                // Return your created list for the template#query call
                return List.of(expected);
            });
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);

        FileStatus fileStatus = mockFileStatus(null);
        Assertions.assertNull(fileStatus.getAuditStatus());

        commonDataDRecords.auditAndDeleteCategories();

        Assertions.assertEquals(MappingConstants.PARTIAL_SUCCESS, fileStatus.getAuditStatus());

        verify(lovServiceJsrValidatorInitializer,times(1))
            .auditJsrExceptions(anyList(),any(),anyString(),any());

    }


    @Test
    void testExecute_DCategoriestoDelete_ParitialSuccess() {

        Categories expected = Categories.builder().categoryKey("value returned by query")
            .serviceId("value returned by query")
            .key("value returned by query").build();
        when(commonDataExecutor.execute(any(), anyString(), anyString())).thenReturn("success");
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Categories>>any(), anyString()))
            .thenAnswer((Answer<List<Categories>>) invocation -> {
                // Fetch the method arguments
                Object[] args = invocation.getArguments();

                // Create a mock result set and setup an expectation on it
                ResultSet rs = mock(ResultSet.class);
                when(rs.getString(anyString())).thenReturn("value returned by query");

                // Fetch the row mapper instance from the arguments
                RowMapper<Categories> rm = (RowMapper<Categories>) args[1];
                // Invoke the row mapper
                Categories actual = rm.mapRow(rs, 0);

                // Assert the result of the row mapper execution
                Assertions.assertEquals(expected, actual);

                // Return your created list for the template#query call
                return List.of(expected);
            });
        when(jdbcTemplate.update(anyString(), anyString())).thenReturn(1);
        FileStatus fileStatus = mockFileStatus(MappingConstants.SUCCESS);
        Assertions.assertEquals(MappingConstants.SUCCESS, fileStatus.getAuditStatus());

        commonDataDRecords.auditAndDeleteCategories();

        Assertions.assertEquals(MappingConstants.PARTIAL_SUCCESS, fileStatus.getAuditStatus());
        verify(lovServiceJsrValidatorInitializer,times(1))
            .auditJsrExceptions(anyList(),any(),anyString(),any());

    }

    private FileStatus mockFileStatus(String status) {
        Registry registry = mock(Registry.class);
        when(camelContext.getRegistry()).thenReturn(registry);
        when(camelContext.getGlobalOptions()).thenReturn(Map.of(FILE_NAME,"Categories.csv"));
        FileStatus fileStatus = FileStatus.builder().auditStatus(status).build();
        when(registry.lookupByName(anyString())).thenReturn(fileStatus);
        return fileStatus;
    }

}
