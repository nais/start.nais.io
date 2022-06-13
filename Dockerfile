FROM gcr.io/distroless/java17

COPY build/libs/app-all.jar /app/app-all.jar

WORKDIR /app

USER nonroot

CMD ["app-all.jar"]
