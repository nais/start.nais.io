FROM gcr.io/distroless/java-debian10:11

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["app-all.jar"]
