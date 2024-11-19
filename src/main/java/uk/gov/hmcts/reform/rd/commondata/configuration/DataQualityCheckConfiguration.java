package uk.gov.hmcts.reform.rd.commondata.configuration;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class DataQualityCheckConfiguration {

    @Value("${zero-byte-characters}")
    public List<String> zeroByteCharacters;

    public <T> List<Pair<String, Long>> processExceptionRecords(List<Object> typeList,
                                            JsrValidatorInitializer<T> validator) {

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
            JsrValidatorInitializer<FlagDetails> flagDetailsJsrValidatorInitializer =
                (JsrValidatorInitializer<FlagDetails>) validator;

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

    public List<Categories> getExistingListFromTable(JdbcTemplate jdbcTemplate) {
        String query = "Select * from list_of_values";
        var listOfValues = jdbcTemplate.queryForList(query);
        List<Categories> listOfExistingCategoriesInTable = new ArrayList();

        for (Map<String, Object> category : listOfValues) {
            Categories categories = new Categories();
            categories.setActive(category.get("active") != null ? (String)category.get("active") : "");
            categories.setCategoryKey(category.get("categorykey") != null ? (String)category.get("categorykey") : "");
            categories.setHintTextEN(category.get("hinttext_en") != null ? (String)category.get("hinttext_en") : "");
            categories.setHintTextCY(category.get("hinttext_cy") != null ? (String)category.get("hinttext_cy") : "");
            categories.setKey(category.get("key") != null ? (String)category.get("key") : "");
            categories.setValueCY(category.get("value_cy") != null ? (String)category.get("value_cy") : "");
            categories.setValueEN(category.get("value_en") != null ? (String)category.get("value_en") : "");
            categories.setLovOrder(category.get("lov_order") != null ? (String)category.get("lov_order") : null);
            categories.setServiceId(category.get("serviceid") != null ? (String)category.get("serviceid") : "");
            categories.setParentCategory(category.get("parentcategory") != null
                ? (String)category.get("parentcategory") : "");
            categories.setParentKey(category.get("parentkey") != null ? (String)category.get("parentkey") : "");
            categories.setExternalReferenceType(category.get("external_reference_type") != null
                ? (String)category.get("external_reference_type") : "");
            categories.setExternalReference(category.get("external_reference") != null
                ? (String)category.get("external_reference") : "");
            listOfExistingCategoriesInTable.add(categories);
        }

        return listOfExistingCategoriesInTable;
    }


}
