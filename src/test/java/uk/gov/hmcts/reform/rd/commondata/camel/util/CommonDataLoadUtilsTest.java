package uk.gov.hmcts.reform.rd.commondata.camel.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;

import java.util.ArrayList;

import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.checkIfValueNotInListIfPresent;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.filterDomainObjects;

public class CommonDataLoadUtilsTest {

    @Test
    void testCheckIfValueNotInListIfPresent() {
        Assertions.assertFalse(checkIfValueNotInListIfPresent("123", ImmutableList.of("123")));
    }

    @Test
    void testCheckIfValueNotInListIfPresent_NullIdPassed() {
        Assertions.assertFalse(checkIfValueNotInListIfPresent(null, ImmutableList.of("123")));
    }

    @Test
    void testCheckIfValueNotInListIfPresent_IdNotInList() {
        Assertions.assertTrue(checkIfValueNotInListIfPresent("abc", ImmutableList.of("123")));
    }

    @Test
    void testfilterDomainObjects() {
        FlagService flagService = new FlagService();
        flagService.setID("1");
        flagService.setServiceId("XXXX");
        flagService.setRequestReason("FALSE");
        flagService.setFlagCode("TEST001");
        flagService.setHearingRelevant("WRONG");

        FlagService flagService1 = new FlagService();
        flagService1.setID("");
        flagService1.setServiceId("XXXX");
        flagService1.setRequestReason("WRONG");
        flagService1.setFlagCode("TEST002");
        flagService1.setHearingRelevant("WRONG");

        var domainObjects = new ArrayList<FlagService>();
        domainObjects.add(flagService);
        domainObjects.add(flagService1);

        var flagCodeList = new ArrayList<String>();
        flagCodeList.add("TEST003");
        flagCodeList.add("TEST004");

        var result = filterDomainObjects(
            domainObjects,
            d -> checkIfValueNotInListIfPresent(d.getFlagCode(), flagCodeList)
        );
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        result.removeAll(domainObjects);
        Assertions.assertEquals(0, result.size());
    }
}
