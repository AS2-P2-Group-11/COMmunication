FROM gradle:4.7.0-jdk8-alpine
COPY --chown=gradle:gradle . /Agent

WORKDIR /Agent

RUN ./gradlew shadowJar


#ENTRYPOINT ["java", "-jar", "./build/libs/Agent-all.skill"]

