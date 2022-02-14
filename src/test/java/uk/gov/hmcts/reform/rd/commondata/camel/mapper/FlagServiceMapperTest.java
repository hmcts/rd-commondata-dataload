package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlagServiceMapperTest {

    @Spy
    FlagServiceMapper flagServiceMapper = new FlagServiceMapper();

    @Test
    void testMapper() {
        FlagService flagService = FlagService.builder()
            .ID("1")
            .serviceId("XXXX")
            .hearingRelevant("TRUE")
            .requestReason("FALSE")
            .flagCode("TEST001")
            .build();
        var expected = new HashMap<String, Object>();
        expected.put("id", Long.valueOf(trim(flagService.getID())));
        expected.put("service_id", trim(flagService.getServiceId()));
        expected.put("hearing_relevant", Boolean.valueOf(trim(flagService.getHearingRelevant())));
        expected.put("request_reason", Boolean.valueOf(trim(flagService.getRequestReason())));
        expected.put("flag_code", trim(flagService.getFlagCode()));

        Map<String, Object> actual = flagServiceMapper.getMap(flagService);
        verify(flagServiceMapper, times(1)).getMap(flagService);
        Assertions.assertThat(actual).hasSize(5).isEqualTo(expected);
    }
}
