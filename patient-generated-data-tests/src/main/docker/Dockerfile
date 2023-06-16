FROM ghcr.io/department-of-veterans-affairs/health-apis-docker-octopus/java-test-base:v2-jdk17

COPY maven/ /sentinel
RUN chmod 755 /sentinel/*sh
ENTRYPOINT ["/sentinel/entrypoint.sh"]
CMD []
