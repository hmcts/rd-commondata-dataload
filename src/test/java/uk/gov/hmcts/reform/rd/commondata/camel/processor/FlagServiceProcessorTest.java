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
import uk.gov.hmcts.reform.data.ingestion.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.data.ingestion.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
class FlagServiceProcessorTest {
    @Spy
    private FlagServiceProcessor processor = new FlagServiceProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @Spy
    JsrValidatorInitializer<FlagService> flagServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Mock
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;

    private static final List<String> ZERO_BYTE_CHARACTERS = List.of("\u200B", " ");

    private static final List<Pair<String, Long>> ZERO_BYTE_CHARACTER_RECORDS = List.of(
        Pair.of("TEST001", null),Pair.of("TEST002", null));

    DataQualityCheckConfiguration dataQualityCheckConfiguration = new DataQualityCheckConfiguration();

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        setField(dataQualityCheckConfiguration, "zeroByteCharacters", ZERO_BYTE_CHARACTERS);
        setField(flagServiceJsrValidatorInitializer, "validator", validator);
        setField(flagServiceJsrValidatorInitializer, "camelContext", camelContext);
        setField(processor, "jdbcTemplate", jdbcTemplate);
        setField(flagServiceJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(flagServiceJsrValidatorInitializer, "platformTransactionManager",
                 platformTransactionManager
        );
        setField(processor, "flagServiceJsrValidatorInitializer",
                 flagServiceJsrValidatorInitializer
        );
        setField(processor, "logComponentName",
                 "testlogger"
        );

        setField(processor, "dataQualityCheckConfiguration", dataQualityCheckConfiguration);
        setField(processor, "flagCodeQuery", "test");
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }

    @Test
    void testFlagDetailsCsv_0byte_characters() throws Exception {
        var zeroBytesFlagDetails = new ArrayList<FlagService>();
        zeroBytesFlagDetails.addAll(getFlagServiceWithZeroBytes());

        exchange.getIn().setBody(zeroBytesFlagDetails);

        doNothing().when(processor).audit(flagServiceJsrValidatorInitializer, exchange);
        when((processor).validate(flagServiceJsrValidatorInitializer,zeroBytesFlagDetails))
            .thenReturn(zeroBytesFlagDetails);
        when(jdbcTemplate.queryForList("test", String.class)).thenReturn(ImmutableList.of("TEST001", "TEST002"));
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());
        verify(flagServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(eq(ZERO_BYTE_CHARACTER_RECORDS),
                                eq(null),
                                eq("Zero byte characters identified - check source file"),
                                eq(exchange));
    }

    @Test
    @DisplayName("Test to check the behaviour when multiple valid Flag Service Records are passed."
        + " All the Flag Service have data in all the fields.")
    void testProcessValidFile() throws Exception {
        var expectedValidFlagServices = getValidFlagServices();
        exchange.getIn().setBody(expectedValidFlagServices);
        doNothing().when(processor).audit(flagServiceJsrValidatorInitializer, exchange);
        when(jdbcTemplate.queryForList("test", String.class)).thenReturn(ImmutableList.of("TEST001", "TEST002"));
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        List actualFlagServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(expectedValidFlagServices.size(), actualFlagServiceList.size());
    }

    @Test
    @DisplayName("Test to check the behaviour when multiple Flag Service Records are passed"
        + " along with an invalid Flag Service Record.")
    void testProcessValidFile_CombinationOfValidAndInvalidFlagServices() throws Exception {
        var flagServiceList = new ArrayList<FlagService>();
        flagServiceList.addAll(getInvalidFlagServices());

        var expectedValidFlagServices = getValidFlagServices();
        flagServiceList.addAll(expectedValidFlagServices);

        exchange.getIn().setBody(flagServiceList);
        doNothing().when(processor).audit(flagServiceJsrValidatorInitializer, exchange);
        when(jdbcTemplate.queryForList("test", String.class)).thenReturn(ImmutableList.of("TEST001", "TEST002"));

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualFlagServiceList = (List) exchange.getMessage().getBody();

        Assertions.assertEquals(expectedValidFlagServices.size(), actualFlagServiceList.size());

    }

    @Test
    @DisplayName("Test to check the behaviour when Record not present in parent table")
    void testProcessForForeignKeyViolation() throws Exception {
        var flagServiceList = new ArrayList<FlagService>();
        flagServiceList.addAll(getFlagServices());

        exchange.getIn().setBody(flagServiceList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        doNothing().when(processor).audit(flagServiceJsrValidatorInitializer, exchange);
        when(jdbcTemplate.queryForList("test", String.class)).thenReturn(ImmutableList.of("TEST001", "TEST002"));

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualFlagServiceList = (List) exchange.getMessage().getBody();
        verify(flagServiceJsrValidatorInitializer, times(1))
            .auditJsrExceptions(anyList(), anyString(), anyString(), any());
        Assertions.assertEquals(1, actualFlagServiceList.size());

    }

    @Test
    @DisplayName("Test to check the behaviour when No valid record Found")
    void testProcessWhenNoValidFlagServiceRecord() throws Exception {
        var flagServiceList = new ArrayList<FlagService>();
        List<Pair<String, Long>> invalidFlagCode = new ArrayList<>();
        flagServiceList.addAll(getInvalidFlagServices());
        exchange.getIn().setBody(flagServiceList);
        doNothing().when(processor).audit(flagServiceJsrValidatorInitializer, exchange);
        Assertions.assertThrows(RouteFailedException.class, () -> processor.process(exchange));
    }

    private List<FlagService> getInvalidFlagServices() {
        return ImmutableList.of(
            FlagService.builder()
                .ID("1")
                .serviceId("XXXX")
                .hearingRelevant("")
                .requestReason("FALSE")
                .flagCode("TEST000")
                .build());
    }

    private List<FlagService> getFlagServices() {
        return ImmutableList.of(
            FlagService.builder()
                .ID("1")
                .serviceId("XXXX")
                .hearingRelevant("FALSE")
                .requestReason("FALSE")
                .flagCode("TEST001")
                .build(),
            FlagService.builder()
                .ID("2")
                .serviceId("XXXX")
                .hearingRelevant("TRUE")
                .requestReason("FALSE")
                .flagCode("TEST000")
                .build()
        );
    }


    private List<FlagService> getValidFlagServices() {
        return ImmutableList.of(
            FlagService.builder()
                .ID("1")
                .serviceId("XXXX")
                .hearingRelevant("TRUE")
                .requestReason("FALSE")
                .flagCode("TEST001")
                .build(),
            FlagService.builder()
                .ID("2")
                .serviceId("XXXX")
                .hearingRelevant("TRUE")
                .requestReason("FALSE")
                .flagCode("TEST002")
                .build()
        );
    }

    private List<FlagService> getFlagServiceWithZeroBytes() {
        return ImmutableList.of(
            FlagService.builder()
                .ID("1")
                .serviceId("XXXX")
                .hearingRelevant("TRUE")
                .requestReason("FALSE ")
                .flagCode("TEST001")
                .build(),
            FlagService.builder()
                .ID("2")
                .serviceId("XXXX")
                .hearingRelevant("TRUE")
                .requestReason("F\u200BALSE")
                .flagCode("TEST002")
                .build()
        );

    }
}
