package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.util.Map;

@Component
public class OtherCategoriesMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object categoriesObj) {
        OtherCategories categories = (OtherCategories) categoriesObj;

        return CommonMapper.getMap(categories.getCategoryKey(), categories.getServiceId(), categories.getKey(),
            categories.getValueEN(), categories.getValueCY(), categories.getHintTextEN(),
            categories.getHintTextCY(), categories.getParentCategory(),
            categories.getParentKey(), categories.getActive(), categories.getLovOrder());
    }
}

