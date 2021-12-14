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

}
