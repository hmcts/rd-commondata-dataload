package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class FlagServiceTest {

    private Validator validator;

    @Test
    public void testFlagService() {
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
            .toString();
        Assertions.assertEquals(flagServiceBuilderString, "FlagService.FlagServiceBuilder("
            + "ID=2, serviceId=YYYY, hearingRelevant=FALSE, requestReason=TRUE"
            + ", flagCode=TEST002)");
    }

    @Test
    public void testFlagServiceJsrAnnotation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        FlagService flagService = new FlagService();
        flagService.setID("1");
        flagService.setServiceId("XXXX");
        flagService.setRequestReason("FALSE");
        flagService.setFlagCode("TEST001");
        flagService.setHearingRelevant("WRONG");
        Set<ConstraintViolation<FlagService>> violations = validator.validate(flagService);
        Assertions.assertEquals(1, violations.size());

        FlagService flagService1 = new FlagService();
        flagService1.setID("");
        flagService1.setServiceId("XXXX");
        flagService1.setRequestReason("WRONG");
        flagService1.setFlagCode("TEST001");
        flagService1.setHearingRelevant("WRONG");
        Set<ConstraintViolation<FlagService>> violations1 = validator.validate(flagService1);
        Assertions.assertEquals(4, violations1.size());

        FlagService flagService2 = new FlagService();
        Set<ConstraintViolation<FlagService>> violations2 = validator.validate(flagService2);
        Assertions.assertEquals(5, violations2.size());
    }
}
