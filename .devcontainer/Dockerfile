FROM alpine:3.21

# Add the necessary extra packages
RUN apk add --no-cache git maven openjdk21-jdk

# Create a user for non-root work inside the container. That way files
# created in the container won't be owned by root on the host.
RUN adduser -D --uid 1000 devuser
USER devuser

ENTRYPOINT [ "sh" ]
