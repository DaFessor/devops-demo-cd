# Fetch Java
FROM eclipse-temurin:25-jdk
RUN "./mvnw" package -f "/workspaces/devops-demo-cd/pom.xml" -Dmaven.test.skip=true
FROM alpine:3.23
RUN apk add --no-cache openjdk25-jre
# Expose port 8080
EXPOSE 8080
# Add the jar file
ADD /target/*.jar devops-demo-cd-v1.0.0.jar
# Start the application
ENTRYPOINT ["java", "-jar", "/devops-demo-cd-v1.0.0.jar"]