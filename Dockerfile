FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY . .

RUN gradle clean build -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && \
    apt-get install -y wget ffmpeg python3 && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O yt-dlp_linux && \
    chmod +x yt-dlp_linux

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]