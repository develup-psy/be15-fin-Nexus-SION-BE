#!/bin/sh

echo "🔧 pre-commit 훅을 .git/hooks에 복사합니다..."

HOOK_SOURCE=".hooks/pre-commit.sh"
HOOK_DEST=".git/hooks/pre-commit"

if [ ! -f "$HOOK_SOURCE" ]; then
  echo "❌ 훅 스크립트가 없습니다: $HOOK_SOURCE"
  exit 1
fi

# .git/hooks 디렉토리 존재 확인 (없으면 Git 초기화 안 되어 있는 상태일 수도 있음)
if [ ! -d ".git/hooks" ]; then
  echo "❌ .git/hooks 디렉토리가 없습니다. Git 저장소가 아니거나 초기화되지 않았습니다."
  exit 1
fi

# 훅 대상 파일이 없으면 새로 생성
if [ ! -f "$HOOK_DEST" ]; then
  echo "📁 pre-commit 파일이 없어 새로 생성합니다."
  touch "$HOOK_DEST"
fi

cp "$HOOK_SOURCE" "$HOOK_DEST"
chmod +x "$HOOK_DEST"

echo "✅ 훅 설치 완료: $HOOK_DEST"
