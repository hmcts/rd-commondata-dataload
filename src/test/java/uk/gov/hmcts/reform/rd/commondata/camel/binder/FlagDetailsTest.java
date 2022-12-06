package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlagDetailsTest {

    @Test
    void testFlagDetails() {
        FlagDetails flagDetails = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("17-06-2022 13:33:00")
            .mrdUpdatedTime("17-06-2022 13:33:00")
            .mrdDeletedTime("17-06-2022 13:33:00")
            .build();
        Assertions.assertEquals("1", flagDetails.getId());
        Assertions.assertEquals("ABC001", flagDetails.getFlagCode());
        Assertions.assertEquals("ABC001", flagDetails.getValueEn());
        Assertions.assertEquals("ABC001", flagDetails.getValueCy());
        Assertions.assertEquals("01", flagDetails.getCategoryId());
        Assertions.assertEquals("17-06-2022 13:33:00", flagDetails.getMrdCreatedTime());
        Assertions.assertEquals("17-06-2022 13:33:00", flagDetails.getMrdUpdatedTime());
        Assertions.assertEquals("17-06-2022 13:33:00", flagDetails.getMrdDeletedTime());

        String flagDetailsBuilderString = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("01")
            .mrdCreatedTime("17-06-2022 13:33:00")
            .mrdUpdatedTime("17-06-2022 13:33:00")
            .mrdDeletedTime("17-06-2022 13:33:00")
            .toString();
        Assertions.assertEquals(flagDetailsBuilderString, "FlagDetails.FlagDetailsBuilder("
            + "id=1, flagCode=ABC001, valueEn=ABC001, valueCy=ABC001, categoryId=01, mrdCreatedTime=17-06-2022 13:33:00"
            + ", mrdUpdatedTime=17-06-2022 13:33:00, mrdDeletedTime=17-06-2022 13:33:00)");
    }

}
