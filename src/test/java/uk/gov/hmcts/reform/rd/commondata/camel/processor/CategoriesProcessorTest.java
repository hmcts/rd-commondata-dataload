package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ImmutableList;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
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

import java.util.ArrayList;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
public class CategoriesProcessorTest {
    @Spy
    private CategoriesProcessor processor = new CategoriesProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @Spy
    JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Mock
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

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
        //setField(processor, "flagCodeQuery", "test");
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case1")
    void testListOfValuesCsv_DupRecord_Case1() throws Exception {
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
    void testListOfValuesCsv_DupRecord_Case2() throws Exception {
        var lovServiceList = new ArrayList<Categories>();
        lovServiceList.addAll(getLovServicesCase2());

        exchange.getIn().setBody(lovServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(1, actualLovServiceList.size());

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
                .valueEN("ADVANCE PAYMENT new")
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
                .build()
        );
    }
}
