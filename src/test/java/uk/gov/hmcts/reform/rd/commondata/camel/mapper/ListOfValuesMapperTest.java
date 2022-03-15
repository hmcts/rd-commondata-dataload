package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.ListOfValues;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ListOfValuesMapperTest {

    @Spy
    ListOfValuesMapper listOfValuesMapper = new ListOfValuesMapper();

    @Test
    void testMapper() {
        ListOfValues listOfValues = ListOfValues.builder()
            .categoryKey("TEST002")
            .serviceId("XXXX")
            .key("TEST002")
            .valueEN("TEST002")
            .valueCY("TEST002")
            .hintTextEN("TEST002")
            .hintTextCY("TEST002")
            .lovOrder("002")
            .parentCategory("TEST002")
            .parentKey("TEST002")
            .active("Y")
            .build();
        var expected = new HashMap<String, Object>();
        expected.put("categoryKey", trim(listOfValues.getCategoryKey()));
        expected.put("serviceId", trim(listOfValues.getServiceId()));
        expected.put("key", trim(listOfValues.getKey()));
        expected.put("value_en", trim(listOfValues.getValueEN()));
        expected.put("value_cy", trim(listOfValues.getValueCY()));
        expected.put("hinttext_en", trim(listOfValues.getHintTextEN()));
        expected.put("hinttext_cy", trim(listOfValues.getHintTextCY()));
        expected.put("lov_order", Long.valueOf(trim(listOfValues.getLovOrder())));
        expected.put("parentcategory", trim(listOfValues.getParentCategory()));
        expected.put("parentkey", trim(listOfValues.getParentKey()));
        expected.put("active", trim(listOfValues.getActive()));

        Map<String, Object> actual = listOfValuesMapper.getMap(listOfValues);
        verify(listOfValuesMapper, times(1)).getMap(listOfValues);
        Assertions.assertThat(actual).hasSize(11).isEqualTo(expected);
    }
}
