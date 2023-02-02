FROM ${BASEIMAGE}

COPY build/libs/app-all.jar /app/

CMD ["-jar", "/app/app-all.jar"]
