package uk.gov.hmcts.reform.rd.commondata.camel.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommonDataCaseLinkingRouteTask extends BaseTasklet implements Tasklet {

    @Value("${commondata-caselinking-start-route}")
    String startRoute;

    @Value("${commondata-caselinking-routes-to-execute}")
    List<String> routesToExecute;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        return super.execute(startRoute, routesToExecute, Boolean.TRUE);
    }
}
