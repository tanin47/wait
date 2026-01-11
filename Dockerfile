FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY ./build/staging-deploy/io/github/tanin47/**/**/*.jar .
RUN find . -name "*-javadoc.jar" -type f -delete
RUN find . -name "*-sources.jar" -type f -delete

ENTRYPOINT ["/bin/sh", "-c", "java -jar wait-*.jar"]
