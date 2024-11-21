package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OtherCategoriesMapperTest {

    @Spy
    OtherCategoriesMapper listOfValuesMapper = new OtherCategoriesMapper();

    @Test
    void testMapper() {
        OtherCategories otherCategories = OtherCategories.builder()
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
        expected.put("categoryKey", trim(otherCategories.getCategoryKey()));
        expected.put("serviceId", trim(otherCategories.getServiceId()));
        expected.put("key", trim(otherCategories.getKey()));
        expected.put("value_en", trim(otherCategories.getValueEN()));
        expected.put("value_cy", trim(otherCategories.getValueCY()));
        expected.put("hinttext_en", trim(otherCategories.getHintTextEN()));
        expected.put("hinttext_cy", trim(otherCategories.getHintTextCY()));
        expected.put("lov_order", Long.valueOf(trim(otherCategories.getLovOrder())));
        expected.put("parentcategory", trim(otherCategories.getParentCategory()));
        expected.put("parentkey", trim(otherCategories.getParentKey()));
        expected.put("active", trim(otherCategories.getActive()));
        expected.put("external_reference", trim(otherCategories.getExternalReference()));
        expected.put("external_reference_type", trim(otherCategories.getExternalReferenceType()));
        Map<String, Object> actual = listOfValuesMapper.getMap(otherCategories);
        verify(listOfValuesMapper, times(1)).getMap(otherCategories);
        Assertions.assertThat(actual).hasSize(13).isEqualTo(expected);
    }

    @Test
    void testMapperWithNullLovOrder() {
        OtherCategories otherCategories = OtherCategories.builder()
            .categoryKey("TEST002")
            .serviceId("XXXX")
            .key("TEST002")
            .valueEN("TEST002")
            .valueCY("TEST002")
            .hintTextEN("TEST002")
            .hintTextCY("TEST002")
            .lovOrder("")
            .parentCategory("TEST002")
            .parentKey("TEST002")
            .active("Y")
            .build();
        var expected = new HashMap<String, Object>();
        expected.put("categoryKey", trim(otherCategories.getCategoryKey()));
        expected.put("serviceId", trim(otherCategories.getServiceId()));
        expected.put("key", trim(otherCategories.getKey()));
        expected.put("value_en", trim(otherCategories.getValueEN()));
        expected.put("value_cy", trim(otherCategories.getValueCY()));
        expected.put("hinttext_en", trim(otherCategories.getHintTextEN()));
        expected.put("hinttext_cy", trim(otherCategories.getHintTextCY()));
        expected.put("lov_order", null);
        expected.put("parentcategory", trim(otherCategories.getParentCategory()));
        expected.put("parentkey", trim(otherCategories.getParentKey()));
        expected.put("active", trim(otherCategories.getActive()));
        expected.put("external_reference", trim(otherCategories.getExternalReference()));
        expected.put("external_reference_type", trim(otherCategories.getExternalReferenceType()));
        Map<String, Object> actual = listOfValuesMapper.getMap(otherCategories);
        verify(listOfValuesMapper, times(1)).getMap(otherCategories);
        Assertions.assertThat(actual).hasSize(13).isEqualTo(expected);
    }

}


