# ---------- 构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 先只拷 pom，利用层缓存
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- 运行 ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
RUN mkdir -p /var/www/pic_bed && chown -R app:app /var/www/pic_bed

USER app
EXPOSE 8080

# JVM 参数由 JAVA_TOOL_OPTIONS 注入（见 compose），这里只兜底
ENTRYPOINT ["java", "-jar", "app.jar"]