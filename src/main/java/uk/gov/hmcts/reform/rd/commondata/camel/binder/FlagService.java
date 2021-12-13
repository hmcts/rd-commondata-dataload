package uk.gov.hmcts.reform.rd.commondata.camel.binder;

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

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ALLOW_NUMERIC_REGEX;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ALLOW_TRUE_FALSE_REGEX;

@Component
@Setter
@Getter
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlagService extends CommonCsvField implements Serializable {

    @DataField(pos = 1, columnName = "ID")
    @NotEmpty
    @Pattern(regexp = ALLOW_NUMERIC_REGEX, message = "allowed numeric value only")
    @SuppressWarnings("all")
    private String ID;

    @DataField(pos = 2, columnName = "ServiceID")
    @NotEmpty
    private String serviceId;

    @DataField(pos = 3, columnName = "HearingRelevant")
    @NotNull
    @Pattern(regexp = ALLOW_TRUE_FALSE_REGEX, message = "allowed input true or false")
    private String hearingRelevant;

    @DataField(pos = 4, columnName = "RequestReason")
    @NotNull()
    @Pattern(regexp = ALLOW_TRUE_FALSE_REGEX, message = "allowed input true or false")
    private String requestReason;

    @DataField(pos = 5, columnName = "FlagCode")
    @NotEmpty
    private String flagCode;

}
