package uk.gov.hmcts.reform.rd.commondata.camel.util;

public final class CommonDataLoadConstants {

    private CommonDataLoadConstants() {

    }

    public static final String IS_READY_TO_AUDIT = "IS_READY_TO_AUDIT";
    public static final String FLAG_CODE = "flag_code";
    public static final String FLAG_CODE_NOT_EXISTS = "flag_code does not exist in parent table";
    public static final String ROUTER_NAME = "RD_Common_Data";
    public static final String ALLOW_TRUE_FALSE_REGEX = "^(?i)\\s*(true|false)\\s*$";
    public static final String ALLOW_NUMERIC_REGEX = "[0-9]+";
    public static final String ACTIVE_FLAG_D = "D";
    public static final String INVALID_JSR_PARENT_ROW = "Record is deleted as Active flag was 'D'";
    public static final String SELECT_CATEGORY_BY_STATUS =
        "SELECT categorykey, key, serviceid FROM list_of_values WHERE TRIM(active) = ?";
    public static final String DELETE_CATEGORY_BY_STATUS =
        "DELETE FROM list_of_values WHERE TRIM(active) = ?";
    public static final String ACTIVE_Y = "Y";
    public static final String DATE_PATTERN = "\\d{2}-\\d{2}-\\d{4}\\s\\d{2}:\\d{2}:\\d{2}";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
}
