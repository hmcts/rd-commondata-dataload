package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.ListOfValues;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;
@Component

public class ListOfValuesMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object listOfValuesObj) {
        ListOfValues listOfValues = (ListOfValues) listOfValuesObj;
        Map<String, Object> listOfValuesRow = new HashMap<>();
        listOfValuesRow.put("categoryKey", trim(listOfValues.getCategoryKey()));
        listOfValuesRow.put("serviceId", trim(listOfValues.getServiceId()));
        listOfValuesRow.put("key", trim(listOfValues.getKey()));
        listOfValuesRow.put("value_en", trim(listOfValues.getValue_EN()));
        listOfValuesRow.put("value_cy", trim(listOfValues.getValue_CY()));
        listOfValuesRow.put("hinttext_en", trim(listOfValues.getHintText_EN()));
        listOfValuesRow.put("hinttext_cy", trim(listOfValues.getHintText_CY()));
        listOfValuesRow.put("lov_order", trim(listOfValues.getLov_Order()));
        listOfValuesRow.put("parentcategory", trim(listOfValues.getParentCategory()));
        listOfValuesRow.put("parentkey", trim(listOfValues.getParentKey()));
        listOfValuesRow.put("active", trim(listOfValues.getActive()));
        return listOfValuesRow;
    }

}
