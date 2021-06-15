FROM adoptopenjdk/openjdk15:x86_64-ubuntu-jre-15.0.2_7
COPY target /my-app/
ENTRYPOINT ["java", "-jar", "/my-app/netty-1000client-0.0.1-SNAPSHOT.jar"]
