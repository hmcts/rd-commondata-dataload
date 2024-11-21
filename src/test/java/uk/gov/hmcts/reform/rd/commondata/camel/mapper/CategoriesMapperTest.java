package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoriesMapperTest {

    @Spy
    CategoriesMapper listOfValuesMapper = new CategoriesMapper();

    @Test
    void testMapper() {
        Categories categories = Categories.builder()
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
            .externalReference(null)
            .externalReferenceType(null)
            .build();
        var expected = new HashMap<String, Object>();
        expected.put("categoryKey", trim(categories.getCategoryKey()));
        expected.put("serviceId", trim(categories.getServiceId()));
        expected.put("key", trim(categories.getKey()));
        expected.put("value_en", trim(categories.getValueEN()));
        expected.put("value_cy", trim(categories.getValueCY()));
        expected.put("hinttext_en", trim(categories.getHintTextEN()));
        expected.put("hinttext_cy", trim(categories.getHintTextCY()));
        expected.put("lov_order", Long.valueOf(trim(categories.getLovOrder())));
        expected.put("parentcategory", trim(categories.getParentCategory()));
        expected.put("parentkey", trim(categories.getParentKey()));
        expected.put("active", trim(categories.getActive()));
        expected.put("external_reference", trim(categories.getExternalReference()));
        expected.put("external_reference_type", trim(categories.getExternalReferenceType()));
        Map<String, Object> actual = listOfValuesMapper.getMap(categories);
        verify(listOfValuesMapper, times(1)).getMap(categories);
        Assertions.assertThat(actual).hasSize(13).isEqualTo(expected);
    }

    @Test
    void testMapperWithNullLovOrder() {
        Categories categories = Categories.builder()
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
        expected.put("categoryKey", trim(categories.getCategoryKey()));
        expected.put("serviceId", trim(categories.getServiceId()));
        expected.put("key", trim(categories.getKey()));
        expected.put("value_en", trim(categories.getValueEN()));
        expected.put("value_cy", trim(categories.getValueCY()));
        expected.put("hinttext_en", trim(categories.getHintTextEN()));
        expected.put("hinttext_cy", trim(categories.getHintTextCY()));
        expected.put("lov_order", null);
        expected.put("parentcategory", trim(categories.getParentCategory()));
        expected.put("parentkey", trim(categories.getParentKey()));
        expected.put("active", trim(categories.getActive()));
        expected.put("external_reference", trim(categories.getExternalReference()));
        expected.put("external_reference_type", trim(categories.getExternalReferenceType()));
        Map<String, Object> actual = listOfValuesMapper.getMap(categories);
        verify(listOfValuesMapper, times(1)).getMap(categories);
        Assertions.assertThat(actual).hasSize(13).isEqualTo(expected);
    }
}
