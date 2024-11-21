package uk.gov.hmcts.reform.rd.commondata.camel.binder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.domain.CommonCsvField;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

@Component
@Setter
@Getter
@ToString
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OtherCategories extends CommonCsvField implements Serializable {

    @DataField(pos = 1, columnName = "categorykey")
    @NotEmpty
    private String categoryKey;

    @DataField(pos = 2, columnName = "serviceid")
    private String serviceId;

    @DataField(pos = 3, columnName = "key")
    @NotEmpty
    private String key;

    @DataField(pos = 4, columnName = "value_en")
    private String valueEN;

    @DataField(pos = 5, columnName = "value_cy")
    private String valueCY;

    @DataField(pos = 6, columnName = "hintText_en")
    private String hintTextEN;

    @DataField(pos = 7, columnName = "hintText_cy")
    private String hintTextCY;

    @DataField(pos = 8, columnName = "lov_order")
    private String lovOrder;

    @DataField(pos = 9, columnName = "parentcategory")
    private String parentCategory;

    @DataField(pos = 10, columnName = "parentkey")
    private String parentKey;

    @DataField(pos = 11, columnName = "active")
    @NotEmpty
    private String active;

    @DataField(pos = 12, columnName = "external_reference")
    @NotEmpty
    private String externalReference;

    @DataField(pos = 13, columnName = "external_reference_type")
    @NotEmpty
    private String externalReferenceType;
}
