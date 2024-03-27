package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class FlagDetailsTest {

    @Test
    void testFlagDetails() {
        FlagDetails flagDetails = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("2020-01-01 00:00:00")
            .mrdUpdatedTime("2020-01-01 00:00:00")
            .mrdDeletedTime("2020-01-01 00:00:00")
            .build();
        Assertions.assertEquals("1", flagDetails.getId());
        Assertions.assertEquals("ABC001", flagDetails.getFlagCode());
        Assertions.assertEquals("ABC001", flagDetails.getValueEn());
        Assertions.assertEquals("ABC001", flagDetails.getValueCy());
        Assertions.assertEquals("01", flagDetails.getCategoryId());
        Assertions.assertEquals("2020-01-01 00:00:00", flagDetails.getMrdCreatedTime());
        Assertions.assertEquals("2020-01-01 00:00:00", flagDetails.getMrdUpdatedTime());
        Assertions.assertEquals("2020-01-01 00:00:00", flagDetails.getMrdDeletedTime());

        String flagDetailsBuilderString = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("2020-01-01 00:00:00")
            .mrdUpdatedTime("2020-01-01 00:00:00")
            .mrdDeletedTime("2020-01-01 00:00:00")
            .toString();
        Assertions.assertEquals(flagDetailsBuilderString, "FlagDetails.FlagDetailsBuilder("
            + "id=1, flagCode=ABC001, valueEn=ABC001, valueCy=ABC001, categoryId=01, mrdCreatedTime=2020-01-01 00:00:00"
            + ", mrdUpdatedTime=2020-01-01 00:00:00, mrdDeletedTime=2020-01-01 00:00:00)");
    }

    @Test
    void testFlagDetailsJsrAnnotation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        FlagDetails flagDetails = FlagDetails.builder()
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("07-04-2022 12:43:00")
            .mrdUpdatedTime("07-04-2022 12:43:00")
            .mrdDeletedTime("07-04-2022 12:43:00")
            .build();
        Set<ConstraintViolation<FlagDetails>> violations = validator.validate(flagDetails);
        Assertions.assertEquals(1, violations.size());

        FlagDetails flagDetails1 = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueCy("ABC001")
            .mrdCreatedTime("07-04-2022 12:43:00")
            .mrdUpdatedTime("07-04-2022 12:43:00")
            .mrdDeletedTime("07-04-2022 12:43:00")
            .build();
        Set<ConstraintViolation<FlagDetails>> violations1 = validator.validate(flagDetails1);
        Assertions.assertEquals(2, violations1.size());

        FlagDetails flagDetails2 = new FlagDetails();
        Set<ConstraintViolation<FlagDetails>> violations2 = validator.validate(flagDetails2);
        Assertions.assertEquals(4, violations2.size());

        FlagDetails flagDetails3 = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("01/01/2020 00:00:00")
            .mrdUpdatedTime("07-04-2022 12:43:00")
            .mrdDeletedTime("07-04-2022 12:43:00")
            .build();

        Set<ConstraintViolation<FlagDetails>> violations3 = validator.validate(flagDetails3);
        Assertions.assertEquals(1, violations3.size());
    }
}
