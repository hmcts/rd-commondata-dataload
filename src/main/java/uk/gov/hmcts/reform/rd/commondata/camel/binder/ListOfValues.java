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

import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ALLOW_TRUE_FALSE_REGEX;

@Component
@Setter
@Getter
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class List_Of_Values extends CommonCsvField implements Serializable {

    @DataField(pos = 1, columnName = "CategoryKey")
    @NotEmpty
    @SuppressWarnings("all")
    private String categoryKey;

    @DataField(pos = 2, columnName = "ServiceID")
    private String serviceId;

    @DataField(pos = 3, columnName = "Key")
    @NotNull
    private String key;

    @DataField(pos = 4, columnName = "Value_EN")
    private String value_EN;

    @DataField(pos = 5, columnName = "Value_CY")
    private String value_CY;

    @DataField(pos = 5, columnName = "HintText_EN")
    private String hintText_EN;

    @DataField(pos = 5, columnName = "HintText_CY")
    private String hintText_CY;

    @DataField(pos = 5, columnName = "Lov_Order")
    private String lov_Order;

    @DataField(pos = 5, columnName = "ParentCategory")
    private String parentCategory;

    @DataField(pos = 5, columnName = "ParentKey")
    @NotEmpty
    private String parentKey;

    @DataField(pos = 5, columnName = "Active")
    @NotEmpty
    private String active;

}
