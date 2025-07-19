FROM gradle:jdk17 AS build

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean build -x test -x spotlessJavaCheck --no-daemon

FROM openjdk:17


WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./

RUN mv $(ls *.jar | grep -v plain) app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
