FROM gradle:jdk17 AS build

WORKDIR /app
COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean build -x test -x spotlessJavaCheck --no-daemon

# 2단계: 실제 실행 이미지
FROM openjdk:17

WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./

RUN mv $(ls *.jar | grep -v plain) app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
