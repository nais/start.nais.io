FROM gcr.io/distroless/java17

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["app-all.jar"]
