FROM gradle:8.0.2-jdk17 as builder

WORKDIR /home/gradle
ADD . /home/gradle
RUN gradle test shadowJar

FROM cgr.dev/chainguard/jre:openjdk-jre-17

COPY --from=builder /home/gradle/build/libs/app-all.jar ./app-all.jar
CMD ["-jar", "./app-all.jar"]
