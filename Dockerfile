FROM ibm-semeru-runtimes:open-25-jdk

WORKDIR /app

COPY ./build/staging-deploy/io/github/tanin47/**/**/*.jar .
RUN find . -name "*-javadoc.jar" -type f -delete
RUN find . -name "*-sources.jar" -type f -delete
RUN mv $(find . -name "wait-*.jar") wait.jar

ENTRYPOINT ["java", "-jar", "wait.jar"]
