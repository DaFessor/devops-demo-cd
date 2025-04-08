# Fetch Java
FROM eclipse-temurin:21-jdk-alpine
# Expose port 8080
EXPOSE 8080
# Add the jar file
ADD /target/*.jar devops-demo-cd-v1.0.0.jar
# Start the application
ENTRYPOINT ["java", "-jar", "/devops-demo-cd-v1.0.0.jar"]