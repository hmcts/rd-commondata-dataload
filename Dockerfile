ARG APP_INSIGHTS_AGENT_VERSION=2.6.1
FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

# Optional
ENV JAVA_OPTS ""

#COPY lib/applicationinsights-agent-2.5.1-BETA.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/
COPY build/libs/rd-commondata-dataload.jar /opt/app/

EXPOSE 8100

CMD [ "rd-commondata-dataload.jar" ]
