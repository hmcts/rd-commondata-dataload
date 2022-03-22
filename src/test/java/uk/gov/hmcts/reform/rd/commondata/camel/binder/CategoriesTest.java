package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class CategoriesTest {

    @Test
    void testListOfValues() {
        Categories categories = Categories.builder().categoryKey("TEST001")
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
            .build();
        Assertions.assertEquals("TEST001", categories.getCategoryKey());
        Assertions.assertEquals("XXXX", categories.getServiceId());
        Assertions.assertEquals("TEST001", categories.getKey());
        Assertions.assertEquals("TEST001", categories.getValueEN());
        Assertions.assertEquals("TEST001", categories.getValueCY());
        Assertions.assertEquals("TEST001", categories.getHintTextEN());
        Assertions.assertEquals("TEST001", categories.getHintTextCY());
        Assertions.assertEquals("TEST001", categories.getLovOrder());
        Assertions.assertEquals("TEST001", categories.getParentCategory());
        Assertions.assertEquals("TEST001", categories.getParentKey());
        Assertions.assertEquals("Y", categories.getActive());

        String listOfValuesBuilderString = Categories.builder()
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
        Assertions.assertEquals(listOfValuesBuilderString, "ListOfValues.ListOfValuesBuilder("
            + "categoryKey=TEST002, serviceId=XXXX, key=TEST002, valueEN=TEST002, valueCY=TEST002, hintTextEN=TEST002"
            + ", hintTextCY=TEST002, lovOrder=TEST002, parentCategory=TEST002, parentKey=TEST002, active=Y)");
    }

}
