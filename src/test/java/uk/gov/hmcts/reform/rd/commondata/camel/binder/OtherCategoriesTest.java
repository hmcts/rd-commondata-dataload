package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OtherCategoriesTest {

    @Test
    void testOtherCategories() {
        OtherCategories otherCategories = OtherCategories.builder().categoryKey("TEST001")
            .serviceId("XXXX")
            .key("TEST001")
            .valueEN("TEST001")
            .valueCY("TEST001")
            .hintTextEN("TEST001")
            .hintTextCY("TEST001")
            .lovOrder("TEST001")
            .parentCategory("TEST001")
            .parentKey("TEST001")
            .active("Y")
            .externalReference(null)
            .externalReferenceType(null)
            .build();
        Assertions.assertEquals("TEST001", otherCategories.getCategoryKey());
        Assertions.assertEquals("XXXX", otherCategories.getServiceId());
        Assertions.assertEquals("TEST001", otherCategories.getKey());
        Assertions.assertEquals("TEST001", otherCategories.getValueEN());
        Assertions.assertEquals("TEST001", otherCategories.getValueCY());
        Assertions.assertEquals("TEST001", otherCategories.getHintTextEN());
        Assertions.assertEquals("TEST001", otherCategories.getHintTextCY());
        Assertions.assertEquals("TEST001", otherCategories.getLovOrder());
        Assertions.assertEquals("TEST001", otherCategories.getParentCategory());
        Assertions.assertEquals("TEST001", otherCategories.getParentKey());
        Assertions.assertEquals("Y", otherCategories.getActive());

        String categoriesBuilderString = OtherCategories.builder()
            .categoryKey("TEST002")
            .serviceId("XXXX")
            .key("TEST002")
            .valueEN("TEST002")
            .valueCY("TEST002")
            .hintTextEN("TEST002")
            .hintTextCY("TEST002")
            .lovOrder("TEST002")
            .parentCategory("TEST002")
            .parentKey("TEST002")
            .active("Y")
            .toString();
        Assertions.assertEquals(categoriesBuilderString, "OtherCategories.OtherCategoriesBuilder("
            + "categoryKey=TEST002, serviceId=XXXX, key=TEST002, valueEN=TEST002, valueCY=TEST002, hintTextEN=TEST002"
            + ", hintTextCY=TEST002, lovOrder=TEST002, parentCategory=TEST002, parentKey=TEST002, active=Y,"
            + " externalReference=null, externalReferenceType=null)");
    }

}
