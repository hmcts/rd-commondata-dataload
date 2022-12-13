package uk.gov.hmcts.reform.rd.commondata.camel.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.getDateTimeStamp;

@ExtendWith(MockitoExtension.class)
class FlagDetailsMapperTest {

    @Spy
    FlagDetailsMapper flagDetailsMapper = new FlagDetailsMapper();

    @Test
    void testMapper() {
        FlagDetails flagDetails = FlagDetails.builder()
            .id("1")
            .flagCode("ABC001")
            .valueEn("ABC001")
            .valueCy("ABC001")
            .categoryId("1")
            .mrdCreatedTime("17-06-2022 13:33:00")
            .mrdUpdatedTime("17-06-2022 13:33:00")
            .mrdDeletedTime("17-06-2022 13:33:00")
            .build();
        var expected = new HashMap<String, Object>();
        expected.put("id", Long.valueOf(trim(flagDetails.getId())));
        expected.put("flag_code", trim(flagDetails.getFlagCode()));
        expected.put("value_en", trim(flagDetails.getValueEn()));
        expected.put("value_cy", trim(flagDetails.getValueCy()));
        expected.put("category_id", Long.valueOf(trim(flagDetails.getCategoryId())));
        expected.put("mrd_created_time", getDateTimeStamp(trim(flagDetails.getMrdCreatedTime())));
        expected.put("mrd_updated_time", getDateTimeStamp(trim(flagDetails.getMrdUpdatedTime())));
        expected.put("mrd_deleted_time", getDateTimeStamp(trim(flagDetails.getMrdDeletedTime())));
        Map<String, Object> actual = flagDetailsMapper.getMap(flagDetails);
        verify(flagDetailsMapper, times(1)).getMap(flagDetails);
        Assertions.assertThat(actual).hasSize(8).isEqualTo(expected);
    }
}
