package uk.gov.hmcts.reform.rd.commondata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
//@SuppressWarnings ("PMD.DoNotCallSystemExit")
@Slf4j
public class RdCommondataLoadApplication implements ApplicationRunner {
    private static String logComponentName = "Rd Commondata Load";

    public static void main(final String[] args) throws InterruptedException {

        ApplicationContext context = SpringApplication.run(RdCommondataLoadApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(7000);
        int exitCode = SpringApplication.exit(context);
        log.info("{}:: Application exiting with exit code {} ", logComponentName, exitCode);
        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("{}:: RdCommondataLoadApplication run started {} ");
    }


}
