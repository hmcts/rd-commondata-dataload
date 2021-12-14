package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;

@Component
public class FlagServiceMapper implements IMapper {

    /**
     * Map CSV record to a Java Object.
     *
     * @param flagServiceObj flagServiceObj
     * @return Map Object with Key as table column name and value from CSV File
     */
    @Override
    public Map<String, Object> getMap(Object flagServiceObj) {
        FlagService flagService = (FlagService) flagServiceObj;
        Map<String, Object> flagServiceParamMap = new HashMap<>();
        flagServiceParamMap.put("id", Long.valueOf(trim(flagService.getID())));
        flagServiceParamMap.put("service_id", trim(flagService.getServiceId()));
        flagServiceParamMap.put("hearing_relevant", Boolean.valueOf(trim(flagService.getHearingRelevant())));
        flagServiceParamMap.put("request_reason", Boolean.valueOf(trim(flagService.getRequestReason())));
        flagServiceParamMap.put("flag_code", trim(flagService.getFlagCode()));
        return flagServiceParamMap;
    }

}
