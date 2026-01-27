# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.8

# Application image

ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:21-distroless

# Mandatory!
ENV APP rd-commondata-dataload.jar
ENV APPLICATION_TOTAL_MEMORY 512M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 48

# Optional
ENV JAVA_OPTS ""

#COPY lib/applicationinsights-agent-2.5.1-BETA.jar lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

EXPOSE 8100

CMD [ "rd-commondata-dataload.jar" ]
