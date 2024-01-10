package uk.gov.hmcts.reform.rd.commondata.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties
public class DataQualityCheckConfiguration {

    @Value("${zero-byte-characters}")
    public List<String> zeroByteCharacters;
}
