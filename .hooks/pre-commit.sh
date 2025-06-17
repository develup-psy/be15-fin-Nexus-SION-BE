#!/bin/sh

echo "📦 테스트 실행 중..."

# gradlew 실행 (윈도우에선 gradlew.bat, 유닉스계에선 ./gradlew)
if [ -f "./gradlew" ]; then
  ./gradlew test
elif [ -f "./gradlew.bat" ]; then
  ./gradlew.bat test
else
  echo "❌ gradlew 실행 파일을 찾을 수 없습니다. 커밋 중단됨!"
  exit 1
fi


if [ $? -ne 0 ]; then
  echo "❌ 테스트 실패로 커밋 중단됨!"
  exit 1
else
  echo "✅ 테스트 성공! 커밋 계속 진행됨."
fi
