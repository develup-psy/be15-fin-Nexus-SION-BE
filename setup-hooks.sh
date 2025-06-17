#!/bin/bash

echo "🔧 pre-commit 훅을 설치합니다..."

cp .hooks/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo "✅ 설치 완료!"
