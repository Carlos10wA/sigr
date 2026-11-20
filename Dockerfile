FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --gid 10001 sigr \
    && useradd --uid 10001 --gid sigr --no-create-home sigr \
    && mkdir -p /app/data \
    && chown -R sigr:sigr /app

COPY --chown=sigr:sigr target/sigr-0.0.1-SNAPSHOT.jar /app/sigr.jar

USER sigr

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/sigr.jar"]