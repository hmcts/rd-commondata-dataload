package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ImmutableList;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
public class OtherCategoriesProcessorTest {
    @Spy
    private OtherCategoriesProcessor processor = new OtherCategoriesProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @Spy
    JsrValidatorInitializer<OtherCategories> lovServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Spy
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;

    private static final List<String> ZERO_BYTE_CHARACTERS = List.of("\u200B", " ");

    private static final List<Pair<String, Long>> ZERO_BYTE_CHARACTER_RECORDS = List.of(
        Pair.of("BBA3-002AD", null),Pair.of("BBA3-001AD", null));

    DataQualityCheckConfiguration dataQualityCheckConfiguration = new DataQualityCheckConfiguration();

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        setField(dataQualityCheckConfiguration, "zeroByteCharacters", ZERO_BYTE_CHARACTERS);
        setField(lovServiceJsrValidatorInitializer, "validator", validator);
        setField(lovServiceJsrValidatorInitializer, "camelContext", camelContext);
        // setField(processor, "jdbcTemplate", jdbcTemplate);
        setField(lovServiceJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(lovServiceJsrValidatorInitializer, "platformTransactionManager",
                 platformTransactionManager
        );
        setField(processor, "lovServiceJsrValidatorInitializer",
                 lovServiceJsrValidatorInitializer
        );
        setField(processor, "logComponentName",
                 "testlogger"
        );

        setField(processor, "dataQualityCheckConfiguration", dataQualityCheckConfiguration);
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }

    @Test
    void testFlagDetailsCsv_0byte_characters() throws Exception {

        var zeroBytesFlagDetails = getOtherCAtegoriesWithZeroBytes();
        exchange.getIn().setBody(zeroBytesFlagDetails);

        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());
        verify(lovServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(eq(ZERO_BYTE_CHARACTER_RECORDS),
                                eq(null),
                                eq("Zero byte characters identified - check source file"),
                                eq(exchange));
    }



    @Test
    @DisplayName("Test for LOV Duplicate records Case2")
    void testListOfValuesCsv_DupRecord_Case2() {
        var lovServiceList = new ArrayList<OtherCategories>();
        lovServiceList.addAll(getLovServicesCase2());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(1, actualLovServiceList.size());

        verify(lovServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(any(),anyString(),anyString(),any());

        assertFalse(actualLovServiceList.isEmpty());
    }

    @Test
    @DisplayName("Test for LOV 'D' records Case3")
    void testListOfValuesCsv_DupRecord_Case3() throws Exception {
        var lovServiceList = new ArrayList<OtherCategories>();
        lovServiceList.addAll(getLovServicesCase3());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(1, actualLovServiceList.size());
    }

    @Test
    @DisplayName("Test for LOV 'D' records Case3")
    void testListOfValuesCsv_DupRecord_Case4() throws Exception {
        var lovServiceList = new ArrayList<OtherCategories>();
        lovServiceList.addAll(getLovServicesCase4());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(0, actualLovServiceList.size());
        assertTrue(actualLovServiceList.isEmpty());

    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case2")
    void testListOfValuesCsv_DupRecord_Case_Null() {
        var lovServiceList = new ArrayList<OtherCategories>();
        lovServiceList.addAll(Collections.emptyList());

        exchange.getIn().setBody(lovServiceList);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(0, actualLovServiceList.size());
        assertTrue(actualLovServiceList.isEmpty());
    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case1")
    void testListOfValuesCsv_DupRecord_Case1() {
        var lovServiceList = new ArrayList<OtherCategories>();
        lovServiceList.addAll(getLovServicesCase1());

        exchange.getIn().setBody(lovServiceList);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());

    }

    private List<OtherCategories> getLovServicesCase1() {
        return ImmutableList.of(
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA4")
                .key("BBA4-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA4-001")
                .active("Y")
                .build()
        );
    }

    private List<OtherCategories> getLovServicesCase2() {
        return ImmutableList.of(
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("N")
                .build()
        );
    }

    private List<OtherCategories> getLovServicesCase3() {
        return ImmutableList.of(
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build()
        );
    }

    private List<OtherCategories> getLovServicesCase4() {
        return ImmutableList.of(
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-002")
                .active("D")
                .build()
        );
    }

    private List<OtherCategories> getOtherCAtegoriesWithZeroBytes() {
        return List.of(
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-002AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType ")
                .parentKey("\\u200BBBA3-001")
                .active("D")
                .build(),
            OtherCategories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3\u200B")
                .key("BBA3-001AD")
                .valueEN("\u200BADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-002")
                .active("D")
                .build()
        );
    }

}
