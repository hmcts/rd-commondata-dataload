package uk.gov.hmcts.reform.rd.commondata.camel.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.FailedToCreateRouteException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.route.DataLoadRoute;
import uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataExecutor;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.rd.commondata.camel.util.CommonDataLoadConstants.IS_READY_TO_AUDIT;

@Slf4j
@Component
public class BaseTasklet {

    @Autowired
    CamelContext camelContext;

    @Autowired
    CommonDataExecutor commonDataExecutor;

    @Autowired
    DataLoadRoute dataLoadRoute;

    @Value("${logging-component-name}")
    String logComponentName;

    public RepeatStatus execute(String startRoute, List<String> routesToExecute, Boolean doAudit)
        throws FailedToCreateRouteException {

        log.info("{}:: Route Task starts::", logComponentName);
        doAudit = (isEmpty(doAudit)) ? Boolean.FALSE : doAudit;
        camelContext.getGlobalOptions().put(IS_READY_TO_AUDIT, doAudit.toString());
        dataLoadRoute.startRoute(startRoute, routesToExecute);
        var status = commonDataExecutor.execute(camelContext, "CommonData Route", startRoute);
        log.info("{}:: Route Task completes with status::{}", logComponentName, status);
        return RepeatStatus.FINISHED;
    }
}
