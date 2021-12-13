FROM gcr.io/distroless/java17

COPY build/libs/app-all.jar /app/app-all.jar

WORKDIR /app

CMD ["app-all.jar"]
