package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trim;

@Component
public class CategoriesMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object categoriesObj) {
        Categories categories = (Categories) categoriesObj;

        return CommonMapper.getMap(categories.getCategoryKey(), categories.getServiceId(), categories.getKey(),
            categories.getValueEN(), categories.getValueCY(), categories.getHintTextEN(), categories.getHintTextCY(),
            categories.getParentCategory(), categories.getParentKey(), categories.getActive(),
            categories.getLovOrder());
    }
}
