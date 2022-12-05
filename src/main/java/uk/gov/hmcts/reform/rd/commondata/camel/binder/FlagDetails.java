package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.DATE_PATTERN;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.DATE_TIME_FORMAT;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.domain.CommonCsvField;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.DatePattern;


@Component
@Setter
@Getter
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlagDetails extends CommonCsvField implements Serializable {

    @DataField(pos = 1, columnName = "id")
    @SuppressWarnings("all")
    private String id;

    @DataField(pos = 2, columnName = "flag_code")
    @NotEmpty
    private String flagCode;

    @DataField(pos = 3, columnName = "value_en")
    private String valueEn;

    @DataField(pos = 4, columnName = "value_cy")
    private String valueCy;

    @DataField(pos = 5, columnName = "category_id")
    private String categoryId;

    @DataField(pos = 6, columnName = "MRD_Created_Time")
    @DatePattern(isNullAllowed = "true", regex = DATE_PATTERN,
        message = "date pattern should be " + DATE_TIME_FORMAT)
    private String mrdCreatedTime;

    @DataField(pos = 7, columnName = "MRD_Updated_Time")
    @DatePattern(isNullAllowed = "true", regex = DATE_PATTERN,
        message = "date pattern should be " + DATE_TIME_FORMAT)
    private String mrdUpdatedTime;

    @DataField(pos = 8, columnName = "MRD_Deleted_Time")
    @DatePattern(isNullAllowed = "true", regex = DATE_PATTERN,
        message = "date pattern should be " + DATE_TIME_FORMAT)
    private String mrdDeletedTime;

}
