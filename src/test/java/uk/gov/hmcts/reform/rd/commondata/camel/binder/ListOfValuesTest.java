package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ListOfValuesTest {

    @Test
    void testListOfValues() {
        ListOfValues listOfValues = ListOfValues.builder().categoryKey("TEST001")
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
        Assertions.assertEquals("TEST001", listOfValues.getCategoryKey());
        Assertions.assertEquals("XXXX", listOfValues.getServiceId());
        Assertions.assertEquals("TEST001", listOfValues.getKey());
        Assertions.assertEquals("TEST001", listOfValues.getValueEN());
        Assertions.assertEquals("TEST001", listOfValues.getValueCY());
        Assertions.assertEquals("TEST001", listOfValues.getHintTextEN());
        Assertions.assertEquals("TEST001", listOfValues.getHintTextCY());
        Assertions.assertEquals("TEST001", listOfValues.getLovOrder());
        Assertions.assertEquals("TEST001", listOfValues.getParentCategory());
        Assertions.assertEquals("TEST001", listOfValues.getParentKey());
        Assertions.assertEquals("Y", listOfValues.getActive());

        String listOfValuesBuilderString = ListOfValues.builder()
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
