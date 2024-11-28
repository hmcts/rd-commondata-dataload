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
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
 class CategoriesProcessorTest {
    @Spy
    private CategoriesProcessor processor = new CategoriesProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);
    private static final List<String> ZERO_BYTE_CHARACTERS = List.of("\u200B", " ");

    private static final List<Pair<String, Long>> ZERO_BYTE_CHARACTER_RECORDS = List.of(Pair.of("BFA1-001AD", null),
                                                                   Pair.of("BFA1-DC\u200BX", null),
                                                                    Pair.of("BFA1-PAD", null));

    public static final String LOV_EXTERNAL_REFERENCE = "external_reference,external_reference_type";

    public static final String EXTERNAL_REFERENCE_ERROR_MSG = "Both external_reference and "
        + "external_reference_type value must be null or both must be not-null";

    @Spy
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();


    DataQualityCheckConfiguration dataQualityCheckConfiguration = new DataQualityCheckConfiguration();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Spy
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;


    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        setField(dataQualityCheckConfiguration, "zeroByteCharacters", ZERO_BYTE_CHARACTERS);
        setField(lovServiceJsrValidatorInitializer, "validator", validator);
        setField(lovServiceJsrValidatorInitializer, "camelContext", camelContext);
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
        setField(processor, "dataQualityCheckConfiguration",
                 dataQualityCheckConfiguration
        );
        //setField(processor, "flagCodeQuery", "test");
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case1")
    void testListOfValuesCsv_DupRecord_Case1() {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase1());

        exchange.getIn().setBody(lovServiceList);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());
    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case2")
    void testListOfValuesCsv_DupRecord_Case2() {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase2());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(1, actualLovServiceList.size());
        assertFalse(actualLovServiceList.isEmpty());
        verify(lovServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(any(),anyString(),anyString(),any());


    }

    @Test
    @DisplayName("Test for LOV 'D' records Case3")
    void testListOfValuesCsv_DupRecord_Case3() throws Exception {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase3());

        exchange.getIn().setBody(lovServiceList);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());
    }

    //when two records with same composite key are passed to processor
    //The first record is selected and duplicate ones are eliminated
    @Test
    @DisplayName("Test for LOV 'D' records Case3")
    void testListOfValuesCsv_DupRecord_Case4() throws Exception {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase4());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(1, actualLovServiceList.size());
        assertFalse(actualLovServiceList.isEmpty());

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
    @DisplayName("Test for 0 byte characters in record")
    void testListOfValuesCsv_0byte_characters() {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase5());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(5, actualLovServiceList.size());
        verify(lovServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(ZERO_BYTE_CHARACTER_RECORDS,
                                null,
                                "Zero byte characters identified - check source file",
                               exchange);
    }

    @Test
    @DisplayName("Test for missing external reference values in record")
    void testListOfValuesExternal_Reference_Validation() {
        var lovServiceList = new ArrayList<Categories>();
        List<Categories> categories = getLovServicesWithExternalReferenceInvalid();
        lovServiceList.addAll(categories);
        String query = "Select * from list_of_values";
        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertNotNull(actualLovServiceList);
        Assertions.assertEquals(1, actualLovServiceList.size());
        List<Pair<String, Long>> externalRefRecWithError = List.of(Pair.of(categories.get(0).getKey(), null));

        verify(lovServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(externalRefRecWithError,
                LOV_EXTERNAL_REFERENCE,
                EXTERNAL_REFERENCE_ERROR_MSG,
                exchange);
    }

    @Test
    @DisplayName("Test success external reference values in record")
    void testListOfValuesExternal_Reference_Success() {
        var lovServiceList = new ArrayList<Categories>();
        List<Categories> categories = getLovServicesWithExternalReference();
        lovServiceList.addAll(categories);

        exchange.getIn().setBody(lovServiceList);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertNotNull(actualLovServiceList);
        Assertions.assertEquals(categories.size(), actualLovServiceList.size());
        Assertions.assertEquals(categories.get(0).getExternalReference(),
            ((Categories) actualLovServiceList.get(0)).getExternalReference());
        Assertions.assertEquals(categories.get(0).getExternalReferenceType(),
            ((Categories) actualLovServiceList.get(0)).getExternalReferenceType());
        Assertions.assertEquals(categories.get(1).getExternalReference(),
            ((Categories) actualLovServiceList.get(1)).getExternalReference());
        Assertions.assertEquals(categories.get(1).getExternalReferenceType(),
            ((Categories) actualLovServiceList.get(1)).getExternalReferenceType());
    }


    private List<Categories> getLovServicesWithExternalReference() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("panelCategoryMember")
                .serviceId("BBA3")
                .key("PC1-01-74")
                .valueEN("Medical office holder")
                .parentCategory("panelCategory")
                .parentKey("PC2")
                .active("Y")
                .externalReferenceType("MedicalRole")
                .externalReference("74")
                .build(),
            Categories.builder()
                .categoryKey("panelCategoryMember")
                .serviceId("BBA3")
                .key("PC1-01-84")
                .valueEN("Judicial office holder")
                .parentCategory("panelCategory")
                .parentKey("PC1")
                .active("Y")
                .externalReferenceType("JudicialRole")
                .externalReference("84")
                .build()
        );
    }

    private List<Categories> getLovServicesWithExternalReferenceInvalid() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("panelCategoryMember")
                .serviceId("BBA3")
                .key("PC1-01-74")
                .valueEN("Medical office holder")
                .parentCategory("panelCategory")
                .parentKey("PC2")
                .active("Y")
                .externalReferenceType("MedicalRole")
                .externalReference("")
                .build(),
            Categories.builder()
                .categoryKey("panelCategoryMember")
                .serviceId("BBA3")
                .key("PC1-01-84")
                .valueEN("Judicial office holder")
                .parentCategory("panelCategory")
                .parentKey("PC1")
                .active("Y")
                .externalReferenceType("JudicialRole")
                .externalReference("84")
                .build()
        );
    }

    private List<Categories> getLovServicesCase1() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            Categories.builder()
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

    private List<Categories> getLovServicesCase2() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT new")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            Categories.builder()
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

    private List<Categories> getLovServicesCase3() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("Y")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA2")
                .key("BBA3-008AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build()
        );
    }

    private List<Categories> getLovServicesCase4() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BBA3")
                .key("BBA3-001AD")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BBA3-001")
                .active("D")
                .build(),
            Categories.builder()
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

    private List<Categories> getLovServicesCase5() {
        return ImmutableList.of(
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BFA1")
                .key("BFA1-001AD")
                .valueEN("Refusal of application under the EEA regulations\u200B")
                .parentCategory("caseType")
                .parentKey("BFA1-001")
                .active("Y")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BFA1")
                .key("BFA1-PAD")
                .valueEN("ADVANCE PAYMENT ")
                .parentCategory("caseType")
                .parentKey("BFA1-002")
                .active("Y")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BFA1")
                .key("BFA1-EAD")
                .valueEN("Refusal of application under the EEA regulations")
                .parentCategory("caseType")
                .parentKey("BFA1-002")
                .active("Y")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BFA1")
                .key("BFA1-PAX")
                .valueEN("ADVANCE PAYMENT")
                .parentCategory("caseType")
                .parentKey("BFA1-002")
                .active("Y")
                .build(),
            Categories.builder()
                .categoryKey("caseSubType")
                .serviceId("BFA1")
                .key("BFA1-DC\u200BX")
                .valueEN("Revocation of a protection status")
                .parentCategory("caseType")
                .parentKey("BFA1-002")
                .active("Y")
                .build()
        );
    }
}
