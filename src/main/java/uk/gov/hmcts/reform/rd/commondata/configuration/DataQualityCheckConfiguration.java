package uk.gov.hmcts.reform.rd.commondata.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataQualityCheckConfiguration {

    @Value("${zero-byte-characters}")
    public List<String> zeroByteCharacters;
}
