FROM alpine:3.23

# Add the necessary extra packages
RUN apk add --no-cache doas make git curl wget maven docker-cli docker-cli-buildx docker-cli-compose openjdk21-jdk

# Create a user for non-root work inside the container. That way files
# created in the container won't be owned by root on the host.
RUN adduser -D --uid 1000 vscode && addgroup vscode wheel

# Give the vscode user passwordless doas access to the necessary commands
RUN echo "permit nopass :wheel" > /etc/doas.conf && \
    chown -c root:root /etc/doas.conf && \
    chmod -c 0400 /etc/doas.conf

ENV MAVEN_HOME=/usr/share/java/maven-3
ENV MAVEN_CONFIG=/workspaces/devops-demo-cd/.mvn

# Set up Maven settings
RUN mkdir -p /workspaces/devops-demo-cd/.m2 && chown -R vscode:vscode /workspaces/devops-demo-cd/.m2

ENTRYPOINT [ "sh" ]
