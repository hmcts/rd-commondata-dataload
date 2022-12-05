package uk.gov.hmcts.reform.rd.commondata.camel.task;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommonDataFlagDetailsRouteTask extends BaseTasklet implements Tasklet {

    @Value("${commondata-flag-details-start-route}")
    String startRoute;

    @Value("${commondata-flag-details-routes-to-execute}")
    List<String> routesToExecute;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        return super.execute(startRoute, routesToExecute, Boolean.FALSE);
    }
}
