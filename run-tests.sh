#!/bin/bash

IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

echo ">>> Сборка тестов запущена"
docker build -t $IMAGE_NAME .

# Создаем директории
mkdir -p "$TEST_OUTPUT_DIR"

echo ">>> Тесты запущены"

# Запускаем контейнер в фоне
CONTAINER_ID=$(docker run -d \
  --name nbank-tests-$TIMESTAMP \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://192.168.1.103:4111 \
  -e UIBASEURL=http://192.168.1.103:3000 \
  $IMAGE_NAME)

# Выводим логи в реальном времени
docker logs -f $CONTAINER_ID

# Ждем завершения
EXIT_CODE=$(docker wait $CONTAINER_ID)

echo ">>> Копирую результаты..."

# Создаем поддиректории
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Копируем результаты из контейнера
docker cp $CONTAINER_ID:/app/logs/. "$TEST_OUTPUT_DIR/logs/" 2>/dev/null || echo "Не удалось скопировать логи"
docker cp $CONTAINER_ID:/app/target/surefire-reports/. "$TEST_OUTPUT_DIR/results/" 2>/dev/null || echo "Не удалось скопировать результаты"
docker cp $CONTAINER_ID:/app/target/site/. "$TEST_OUTPUT_DIR/report/" 2>/dev/null || echo "Не удалось скопировать отчет"

# Удаляем контейнер
docker rm $CONTAINER_ID > /dev/null 2>&1

echo ""
echo "=== ТЕСТЫ ЗАВЕРШЕНЫ ==="
echo "Выходной код: $EXIT_CODE"
echo "Директория с результатами: $TEST_OUTPUT_DIR"
ls -la "$TEST_OUTPUT_DIR"
