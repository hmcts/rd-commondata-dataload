package uk.gov.hmcts.reform.rd.commondata.configuration;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.Categories;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagDetails;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.OtherCategories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.data.ingestion.camel.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.setFileStatus;

@Component
@SuppressWarnings("unchecked")
public class DataQualityCheckConfiguration {

    public static final String ZERO_BYTE_CHARACTER_ERROR_MESSAGE =
        "Zero byte characters identified - check source file";

    @Value("${zero-byte-characters}")
    public List<String> zeroByteCharacters;

    public <T> void processExceptionRecords(Exchange exchange,
                                            List<Object> typeList,
                                            ApplicationContext applicationContext,
                                            JsrValidatorInitializer<T> validator) {

        List<Pair<String, Long>> zeroByteCharacterRecords = new ArrayList<>();

        if (((List) typeList.get(0)).get(0).getClass().getName().contains("OtherCategories")) {

            List<OtherCategories> otherCategoryList = (List<OtherCategories>) typeList.get(0);
            JsrValidatorInitializer<OtherCategories> lovServiceJsrValidatorInitializer =
                (JsrValidatorInitializer<OtherCategories>) validator;

            otherCategoryList.forEach(otherCategory -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (otherCategory.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            otherCategory.getKey(),
                            otherCategory.getRowId()
                        ));
                    }
                }));
            audit(zeroByteCharacterRecords,exchange,applicationContext,lovServiceJsrValidatorInitializer);

        } else if (((List) typeList.get(0)).get(0).getClass().getName().contains("FlagService")) {
            List<FlagService> flagServiceList = (List<FlagService>) typeList.get(0);
            JsrValidatorInitializer<FlagService> flagServiceJsrValidatorInitializer =
                (JsrValidatorInitializer<FlagService>) validator;

            flagServiceList.forEach(flagService -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (flagService.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            flagService.getFlagCode(),
                            flagService.getRowId()
                        ));
                    }
                }));
            audit(zeroByteCharacterRecords,exchange,applicationContext,flagServiceJsrValidatorInitializer);

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
            audit(zeroByteCharacterRecords,exchange,applicationContext,flagDetailsJsrValidatorInitializer);

        } else if (((List) typeList.get(0)).get(0).getClass().getName().contains("Categories")) {

            List<Categories> categoryList = (List<Categories>) typeList.get(0);
            JsrValidatorInitializer<Categories> lovServiceJsrValidatorInitializer =
                (JsrValidatorInitializer<Categories>) validator;

            categoryList.forEach(category -> zeroByteCharacters
                .forEach(zeroByteChar -> {
                    if (category.toString().contains(zeroByteChar)) {
                        zeroByteCharacterRecords.add(Pair.of(
                            category.getKey(),
                            category.getRowId()
                        ));
                    }
                }));

            audit(zeroByteCharacterRecords,exchange,applicationContext,lovServiceJsrValidatorInitializer);

        }

    }


    public <T> void audit(List<Pair<String, Long>> zeroByteCharacterRecords, Exchange exchange,
                      ApplicationContext applicationContext,
                      JsrValidatorInitializer<T> validator) {
        List<Pair<String, Long>> distinctZeroByteCharacterRecords = zeroByteCharacterRecords.stream()
            .distinct().collect(Collectors.toList());
        if (!distinctZeroByteCharacterRecords.isEmpty()) {
            setFileStatus(exchange, applicationContext, FAILURE);
            validator.auditJsrExceptions(distinctZeroByteCharacterRecords, null,
                ZERO_BYTE_CHARACTER_ERROR_MESSAGE, exchange);
        }
    }


}
