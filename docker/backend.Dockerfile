 # === 构建阶段 ===
 FROM maven:3.9-eclipse-temurin-21 AS builder
 WORKDIR /app
 
 # 先复制 pom.xml 利用 Docker 层缓存
 COPY 校园小卖部/code2026/springboot/pom.xml .
 RUN mvn dependency:go-offline -B
 
 # 复制源码并打包
 COPY 校园小卖部/code2026/springboot/src ./src
 RUN mvn clean package -DskipTests
 
 # === 运行阶段 ===
 FROM eclipse-temurin:21-jre
 WORKDIR /app
 
 COPY --from=builder /app/target/*.jar app.jar
 EXPOSE 9090
 
 ENTRYPOINT ["java", "-jar", "app.jar"]
