package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class FlagServiceTest {

    private Validator validator;

    @Test
    void testFlagService() {
        FlagService flagService = FlagService.builder()
            .ID("1")
            .serviceId("XXXX")
            .hearingRelevant("TRUE")
            .requestReason("FALSE")
            .flagCode("TEST001")
            .build();
        Assertions.assertEquals("1", flagService.getID());
        Assertions.assertEquals("XXXX", flagService.getServiceId());
        Assertions.assertEquals("TRUE", flagService.getHearingRelevant());
        Assertions.assertEquals("FALSE", flagService.getRequestReason());
        Assertions.assertEquals("TEST001", flagService.getFlagCode());

        String flagServiceBuilderString = FlagService.builder()
            .ID("2")
            .serviceId("YYYY")
            .hearingRelevant("FALSE")
            .requestReason("TRUE")
            .flagCode("TEST002")
            .defaultStatus("Requested")
            .availableExternally("true")
            .toString();
        Assertions.assertEquals(flagServiceBuilderString, "FlagService.FlagServiceBuilder("
            + "ID=2, serviceId=YYYY, hearingRelevant=FALSE, requestReason=TRUE"
            + ", flagCode=TEST002, defaultStatus=Requested, availableExternally=true)");
    }

    @Test
    void testFlagServiceJsrAnnotation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        FlagService flagService = new FlagService();
        flagService.setID("1");
        flagService.setServiceId("XXXX");
        flagService.setRequestReason("FALSE");
        flagService.setFlagCode("TEST001");
        flagService.setHearingRelevant("WRONG");
        flagService.setDefaultStatus("Requested");
        flagService.setAvailableExternally("true");
        Set<ConstraintViolation<FlagService>> violations = validator.validate(flagService);
        Assertions.assertEquals(1, violations.size());

        FlagService flagService1 = new FlagService();
        flagService1.setID("");
        flagService1.setServiceId("XXXX");
        flagService1.setRequestReason("WRONG");
        flagService1.setFlagCode("TEST001");
        flagService1.setHearingRelevant("WRONG");
        flagService.setDefaultStatus("Requested");
        flagService.setAvailableExternally("true");
        Set<ConstraintViolation<FlagService>> violations1 = validator.validate(flagService1);
        Assertions.assertEquals(4, violations1.size());

        FlagService flagService2 = new FlagService();
        Set<ConstraintViolation<FlagService>> violations2 = validator.validate(flagService2);
        Assertions.assertEquals(5, violations2.size());
    }
}
