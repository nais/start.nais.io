FROM gcr.io/distroless/java17-debian11:nonroot

COPY build/libs/app-all.jar /app/app-all.jar

CMD ["/app/app-all.jar"]
