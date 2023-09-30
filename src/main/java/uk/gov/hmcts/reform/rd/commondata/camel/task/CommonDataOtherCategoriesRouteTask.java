package uk.gov.hmcts.reform.rd.commondata.camel.task;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class CommonDataOtherCategoriesRouteTask extends BaseTasklet implements Tasklet {

    @Value("${commondata-othercategories-start-route}")
    String startRoute;

    @Value("${commondata-othercategories-routes-to-execute}")
    List<String> routesToExecute;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        return super.execute(startRoute, routesToExecute, Boolean.TRUE);
    }

}
