# Fetch Java
FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /workspaces/devops-demo-cd
RUN mkdir -p /workspaces/devops-demo-cd
COPY . /workspaces/devops-demo-cd
RUN cd /workspaces/devops-demo-cd && mvn package -f . -Dmaven.test.skip=true

FROM alpine:3.23
RUN apk add --no-cache openjdk25-jre
# Expose port 8080
EXPOSE 8080
# Add the jar file
RUN mkdir -p /app
COPY --from=build /workspaces/devops-demo-cd/target/devops-demo-cd-v1.0.0.jar /app/devops-demo-cd-v1.0.0.jar
# Start the application
ENTRYPOINT ["java", "-jar", "/app/devops-demo-cd-v1.0.0.jar"]