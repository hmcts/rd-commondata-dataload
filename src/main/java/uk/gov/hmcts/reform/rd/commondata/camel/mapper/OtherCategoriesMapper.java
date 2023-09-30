package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trim;

@Component
public class OtherCategoriesMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object categoriesObj) {
        Categories categories = (Categories) categoriesObj;
        Map<String, Object> categoriesRow = new HashMap<>();
        categoriesRow.put("categoryKey", trim(categories.getCategoryKey()));
        categoriesRow.put("serviceId", trim(categories.getServiceId()));
        categoriesRow.put("key", trim(categories.getKey()));
        categoriesRow.put("value_en", trim(categories.getValueEN()));
        categoriesRow.put("value_cy", trim(categories.getValueCY()));
        categoriesRow.put("hinttext_en", trim(categories.getHintTextEN()));
        categoriesRow.put("hinttext_cy", trim(categories.getHintTextCY()));
        categoriesRow.put("parentcategory", trim(categories.getParentCategory()));
        categoriesRow.put("parentkey", trim(categories.getParentKey()));
        categoriesRow.put("active", trim(categories.getActive()));
        if (isBlank(categories.getLovOrder())) {
            categoriesRow.put("lov_order", null);
        } else {
            categoriesRow.put("lov_order", Long.valueOf(trim(categories.getLovOrder())));
        }
        return categoriesRow;
    }
}

