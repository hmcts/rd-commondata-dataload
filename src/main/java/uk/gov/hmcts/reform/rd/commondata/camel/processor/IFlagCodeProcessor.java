package uk.gov.hmcts.reform.rd.commondata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.data.ingestion.camel.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.rd.commondata.camel.binder.FlagService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.camel.util.ObjectHelper.isNotEmpty;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.ROUTER_NAME;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadUtils.filterDomainObjects;

public interface IFlagCodeProcessor<T> {

    @Slf4j
    final class Logger {
    }

    default void handleListWithConstraintViolations(
        List<T> validatedDomains,
        List<T> objectsWithIntegrityViolations,
        Exchange exchange,
        String fieldName, String exceptionMessage,
        JsrValidatorInitializer<T> jsrValidatorInitializer) {

        Type mySuperclass = getType();
        validatedDomains.removeAll(objectsWithIntegrityViolations);
        if (isNotEmpty(objectsWithIntegrityViolations)
            && ((Class) mySuperclass).getCanonicalName().equals(FlagService.class.getCanonicalName())) {

            List<Pair<String, Long>> invalidFlagCode = new ArrayList<>();
            objectsWithIntegrityViolations
                    .stream()
                    .map(FlagService.class::cast)
                    .forEach(invalidCodeList -> invalidFlagCode.add(Pair.of(
                        invalidCodeList.getFlagCode(),
                        invalidCodeList.getRowId()
                    )));

            jsrValidatorInitializer.auditJsrExceptions(invalidFlagCode,
                                                           fieldName, exceptionMessage, exchange
            );
        }
    }

    @SuppressWarnings("java:S6201")
    private Type getType() {
        var genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType = null;
        while (parametrizedType == null) {
            if ((genericSuperClass instanceof ParameterizedType)) {
                parametrizedType = (ParameterizedType) genericSuperClass;
            } else {
                genericSuperClass = ((Class<?>) genericSuperClass).getGenericSuperclass();
            }
        }

        return parametrizedType.getActualTypeArguments()[0];

    }

    /**
     * Check for Foregin Key.
     *
     * @param validatedDomains        List of Records after jsr validation
     * @param predicate               Compare Parent Table & Child Table Id
     * @param field                   Field Name
     * @param exceptionMessage        Error Message
     * @param exchange                Exchange Object
     * @param jsrValidatorInitializer jsrValidatorInitializer Object
     */
    default void checkForeignKeyConstraint(List<T> validatedDomains,
                                           Predicate<T> predicate,
                                           String field, String exceptionMessage,
                                           Exchange exchange,
                                           JsrValidatorInitializer<T> jsrValidatorInitializer) {

        var predicateCheckFailedFlagCodes =
            filterDomainObjects(validatedDomains, predicate);
        Logger.log.info("{}:: Number of valid records after applying the flag code filter {}::",
                        ROUTER_NAME, validatedDomains.size() - predicateCheckFailedFlagCodes.size()
        );

        handleListWithConstraintViolations(validatedDomains, predicateCheckFailedFlagCodes, exchange,
                                           field,
                                           exceptionMessage,
                                           jsrValidatorInitializer
        );
    }

    default List<String> getIdList(JdbcTemplate jdbcTemplate, String query) {
        return jdbcTemplate.queryForList(query, String.class);
    }

}
