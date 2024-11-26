package uk.gov.hmcts.reform.rd.commondata.configuration;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("unchecked")
public class DataQualityCheckConfiguration {

    @Value("${zero-byte-characters}")
    public List<String> zeroByteCharacters;

    public <T> List<Pair<String, Long>> processExceptionRecords(List<Object> typeList) {

        List<Pair<String, Long>> zeroByteCharacterRecords = new ArrayList<>();

        if (((List) typeList.get(0)).get(0).getClass().getName().contains("OtherCategories")) {
            List<OtherCategories> otherCategoryList = (List<OtherCategories>) typeList.get(0);
            otherCategoryList.forEach(otherCategory -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (otherCategory.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            otherCategory.getKey(),
                            otherCategory.getRowId()
                        ));
                    }
                }));
        } else if (((List) typeList.get(0)).get(0).getClass().getName().contains("FlagService")) {
            List<FlagService> flagServiceList = (List<FlagService>) typeList.get(0);
            flagServiceList.forEach(flagService -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (flagService.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            flagService.getFlagCode(),
                            flagService.getRowId()
                        ));
                    }
                }));
        } else if (((List) typeList.get(0)).get(0).getClass().getName().contains("FlagDetails")) {
            List<FlagDetails> flagDetailList = (List<FlagDetails>) typeList.get(0);
            flagDetailList.forEach(flagDetail -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (flagDetail.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            flagDetail.getFlagCode(),
                            flagDetail.getRowId()
                        ));
                    }
                }));
        } else if (((List) typeList.get(0)).get(0).getClass().getName().contains("Categories")) {
            List<Categories> categoryList = (List<Categories>) typeList.get(0);
            categoryList.forEach(category -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (category.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            category.getKey(),
                            category.getRowId()
                        ));
                    }
                }));
        }
        return zeroByteCharacterRecords;
    }




}
