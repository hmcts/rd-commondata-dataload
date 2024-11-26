package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trim;

public class CommonMapper {

    private CommonMapper() {
        // utility class
    }

    public static Map<String, Object> getMap(String categoryKey, String serviceId, String key, String valueEn,
                                      String valueCy, String hintTextEn, String hintTextCy, String parentCategory,
                                      String parentKey, String active, String externalReference,
                                      String externalReferenceType, String lovOrder) {
        Map<String, Object> categoriesRow = new HashMap<>();
        categoriesRow.put("categoryKey", trim(categoryKey));
        categoriesRow.put("serviceId", trim(serviceId));
        categoriesRow.put("key", trim(key));
        categoriesRow.put("value_en", trim(valueEn));
        categoriesRow.put("value_cy", trim(valueCy));
        categoriesRow.put("hinttext_en", trim(hintTextEn));
        categoriesRow.put("hinttext_cy", trim(hintTextCy));
        categoriesRow.put("parentcategory", trim(parentCategory));
        categoriesRow.put("parentkey", trim(parentKey));
        categoriesRow.put("active", trim(active));
        categoriesRow.put("external_reference", trim(externalReference));
        categoriesRow.put("external_reference_type", trim(externalReferenceType));
        if (isBlank(lovOrder)) {
            categoriesRow.put("lov_order", null);
        } else {
            categoriesRow.put("lov_order", Long.valueOf(trim(lovOrder)));
        }
        return categoriesRow;
    }
}
