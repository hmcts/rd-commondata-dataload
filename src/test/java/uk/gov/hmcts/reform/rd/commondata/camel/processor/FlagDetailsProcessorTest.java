package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import com.google.common.collect.ImmutableList;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.configuration.DataQualityCheckConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
class FlagDetailsProcessorTest {

    @Spy
    private FlagDetailsProcessor processor = new FlagDetailsProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @Spy
    JsrValidatorInitializer<FlagDetails> flagDetailsJsrValidatorInitializer
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
        Pair.of("ABC001", null), Pair.of("ABC002", null));

    DataQualityCheckConfiguration dataQualityCheckConfiguration = new DataQualityCheckConfiguration();


    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        setField(dataQualityCheckConfiguration, "zeroByteCharacters", ZERO_BYTE_CHARACTERS);
        setField(flagDetailsJsrValidatorInitializer, "validator", validator);
        setField(flagDetailsJsrValidatorInitializer, "camelContext", camelContext);
        setField(flagDetailsJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(flagDetailsJsrValidatorInitializer, "platformTransactionManager",
                 platformTransactionManager
        );
        setField(processor, "flagDetailsJsrValidatorInitializer",
                 flagDetailsJsrValidatorInitializer
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

        var zeroBytesFlagDetails = getFlagDetailsWithZeroBytes();
        exchange.getIn().setBody(zeroBytesFlagDetails);

        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());
        verify(flagDetailsJsrValidatorInitializer, times(1))
            .auditJsrExceptions(ZERO_BYTE_CHARACTER_RECORDS,
                                null,
                                "Zero byte characters identified - check source file",
                                exchange);
    }

    @Test
    @DisplayName("Test all valid flag details are processed")
    void testProcessValidFile() throws Exception {
        var expectedValidFlagDetails = getValidFlagDetails();
        exchange.getIn().setBody(expectedValidFlagDetails);
        doNothing().when(processor).audit(flagDetailsJsrValidatorInitializer, exchange);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        List actualFlagDetailsList = (List) exchange.getMessage().getBody();

        Assertions.assertEquals(expectedValidFlagDetails.size(), actualFlagDetailsList.size());

    }

    @Test
    @DisplayName("Test records with expired flag details record")
    void testProcessValidFile_CombinationOfValidAndExpiredFlagDetails() throws Exception {
        var flagDetailsList = new ArrayList<FlagDetails>();
        flagDetailsList.addAll(getExpiredFlagDetails());
        flagDetailsList.addAll(getValidFlagDetails());

        exchange.getIn().setBody(flagDetailsList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        doNothing().when(processor).audit(flagDetailsJsrValidatorInitializer, exchange);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        List actualFlagDetailsList = (List) exchange.getMessage().getBody();

        var expectedValidFlagDetails = getValidFlagDetails();
        verify(processor, times(1)).auditRecord(getExpiredFlagDetails(), exchange);

        Assertions.assertEquals(expectedValidFlagDetails.size(), actualFlagDetailsList.size());

        verify(flagDetailsJsrValidatorInitializer, times(1))
            .auditJsrExceptions(any(),anyString(),anyString(),any());

    }

    @Test
    @DisplayName("Test records with valid mrd date flag details record")
    void testProcessValidFile_CombinationOfValidFlagDetails() throws Exception {
        var flagDetailsList = new ArrayList<FlagDetails>();
        flagDetailsList.addAll(getValidMrdDeletedDate());
        flagDetailsList.addAll(getValidFlagDetails());


        exchange.getIn().setBody(flagDetailsList);
        doNothing().when(processor).audit(flagDetailsJsrValidatorInitializer, exchange);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        List actualFlagDetailsList = (List) exchange.getMessage().getBody();

        Assertions.assertEquals(flagDetailsList.size(), actualFlagDetailsList.size());


    }

    @Test
    @DisplayName("Test records with missing mandatory details")
    void testProcessValidFile_InvalidFlagDetails() throws Exception {
        var flagDetailsList = new ArrayList<FlagDetails>();
        flagDetailsList.addAll(getInvalidFlagDetails());
        flagDetailsList.addAll(getValidFlagDetails());

        exchange.getIn().setBody(flagDetailsList);
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);
        doNothing().when(processor).audit(flagDetailsJsrValidatorInitializer, exchange);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);
        List actualFlagDetailsList = (List) exchange.getMessage().getBody();

        var expectedValidFlagDetails = getValidFlagDetails();

        Assertions.assertEquals(expectedValidFlagDetails.size(), actualFlagDetailsList.size());

    }

    @Test
    @DisplayName("Test when no valid record Found")
    void testProcessWhenNoValidFlagDetailsRecord() throws Exception {
        var flagDetailsList = new ArrayList<>(getExpiredFlagDetails());
        exchange.getIn().setBody(flagDetailsList);
        doNothing().when(processor).audit(flagDetailsJsrValidatorInitializer, exchange);
        doNothing().when(processor).auditRecord(getExpiredFlagDetails(), exchange);
        Assertions.assertThrows(RouteFailedException.class, () -> processor.process(exchange));

    }

    @Test
    @DisplayName("Test record with Invalid mrd deleted date")
    void testProcessWhenFlagDetailsWithInvalidExpiredDate() {
        var flagDetailsList = new ArrayList<FlagDetails>();
        flagDetailsList.addAll(getInvalidMrdDeletedDate());
        flagDetailsList.addAll(getValidFlagDetails());
        exchange.getIn().setBody(flagDetailsList);

        Assertions.assertThrows(RuntimeException.class, () -> processor.process(exchange));
    }

    private List<FlagDetails> getExpiredFlagDetails() {
        return ImmutableList.of(
            FlagDetails.builder()
                .id("1")
                .flagCode("ABC001")
                .valueEn("ABC001")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .mrdDeletedTime("17-06-2022 13:33:00")
                .build());
    }

    private List<FlagDetails> getInvalidFlagDetails() {
        return ImmutableList.of(
            FlagDetails.builder()
                .id("1")
                .valueEn("ABC001")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .build());
    }

    private List<FlagDetails> getInvalidMrdDeletedDate() {
        return ImmutableList.of(
            FlagDetails.builder()
                .id("1")
                .flagCode("ABC001")
                .valueEn("ABC001")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .mrdDeletedTime("invalidDate")
                .build());
    }

    private List<FlagDetails> getValidMrdDeletedDate() {
        return ImmutableList.of(
            FlagDetails.builder()
                .id("1")
                .flagCode("ABC001")
                .valueEn("ABC001")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .mrdDeletedTime("17-06-2028 13:33:00")
                .build());
    }

    private List<FlagDetails> getValidFlagDetails() {
        return ImmutableList.of(
            FlagDetails.builder()
                .id("1")
                .flagCode("ABC001")
                .valueEn("ABC001")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .build(),
            FlagDetails.builder()
                .id("2")
                .flagCode("ABC002")
                .valueEn("ABC002")
                .valueCy("ABC002")
                .categoryId("02")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .build());
    }

    private List<FlagDetails> getFlagDetailsWithZeroBytes() {
        return List.of(
            FlagDetails.builder()
                .id("1")
                .flagCode("ABC001")
                .valueEn("ABC001 ")
                .valueCy("ABC001")
                .categoryId("01")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .build(),
            FlagDetails.builder()
                .id("2")
                .flagCode("ABC002")
                .valueEn("ABC002")
                .valueCy("A\u200BBC002")
                .categoryId("02")
                .mrdCreatedTime("17-06-2022 13:33:00")
                .mrdUpdatedTime("17-06-2022 13:33:00")
                .build());

    }
}
