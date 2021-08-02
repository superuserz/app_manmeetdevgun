FROM alpine/git as repository
WORKDIR /app
RUN git clone -b master https://github.com/superuserz/app_manmeetdevgun.git

FROM maven:3.5-jdk-8-alpine as builder
WORKDIR /app
COPY --from=0 /app/app_manmeetdevgun /app
RUN mvn -q clean package -Dmaven.test.skip=true

FROM openjdk:8-jdk-alpine
EXPOSE 8080
EXPOSE 30157
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]