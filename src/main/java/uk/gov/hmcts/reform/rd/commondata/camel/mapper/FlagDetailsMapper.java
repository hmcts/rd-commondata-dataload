package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.getDateTimeStamp;

@Component
public class FlagDetailsMapper implements IMapper {


    @Override
    public Map<String, Object> getMap(Object flagDetailsObj) {
        FlagDetails flagDetails = (FlagDetails) flagDetailsObj;
        Map<String, Object> flagDetailsParamMap = new HashMap<>();
        flagDetailsParamMap.put("id", Long.valueOf(trim(flagDetails.getId())));
        flagDetailsParamMap.put("flag_code", trim(flagDetails.getFlagCode()));
        flagDetailsParamMap.put("value_en", trim(flagDetails.getValueEn()));
        flagDetailsParamMap.put("value_cy", trim(flagDetails.getValueCy()));
        flagDetailsParamMap.put("category_id", Long.valueOf(trim(flagDetails.getCategoryId())));
        flagDetailsParamMap.put("mrd_created_time", getDateTimeStamp(flagDetails.getMrdCreatedTime()));
        flagDetailsParamMap.put("mrd_updated_time", getDateTimeStamp(flagDetails.getMrdUpdatedTime()));
        flagDetailsParamMap.put("mrd_deleted_time", getDateTimeStamp(flagDetails.getMrdDeletedTime()));
        return flagDetailsParamMap;
    }
}
