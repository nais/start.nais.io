FROM cgr.dev/chainguard/jre:openjdk-jre-17

COPY build/libs/app-all.jar /app/

CMD ["-jar", "/app/app-all.jar"]
