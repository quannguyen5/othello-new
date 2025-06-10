FROM maven:3.8.3-openjdk-17 as build
WORKDIR ./src
COPY . .
RUN mvn install

## run stage ##
FROM openjdk:17-alpine
RUN ln -sf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime
COPY --from=build src/target/botservice-0.0.1-SNAPSHOT.jar /run/botservice-0.0.1-SNAPSHOT.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/run/botservice-0.0.1-SNAPSHOT.jar"]