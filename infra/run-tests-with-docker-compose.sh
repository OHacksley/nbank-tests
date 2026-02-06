#!/bin/bash

echo ">>> Остановить Docker Compose"
docker compose -f ./docker-compose.yml down

echo ">>> Docker pull все образы браузеров"

# Путь до файла
json_file="./config/browsers.json"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

# Извлекаем все значения .image через jq
images=$(jq -r '.chrome.versions[].image, .firefox.versions[].image, .opera.versions[].image' "$json_file" | grep -v '^$')

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image"
done

echo ""
echo ">>> Запуск Docker Compose окружения"
docker compose -f ./docker-compose.yml up -d
echo "Ожидание запуска сервисов (30 секунд)..."
sleep 30

IMAGE_NAME=nbank-tests
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

echo ">>> Сборка тестов запущена"
docker build -t $IMAGE_NAME -f ../Dockerfile ../

# Создаем директории
mkdir -p "$TEST_OUTPUT_DIR"

echo ">>> Тесты запущены"

# Запускаем контейнер в фоне
CONTAINER_ID=$(docker run -d \
  --name "nbank-tests-$TIMESTAMP" \
  --network nbank-network \
  -e TEST_PROFILE="api,ui" \
  -e APIBASEURL="http://backend:4111" \
  -e UIBASEURL="http://nginx:80" \
  -e SELENOID_URL="http://selenoid:4444/wd/hub" \
  -e SELENOID_UI_URL="http://selenoid-ui:8080/wd/hub" \
  nbank-tests mvn test -P api,ui)

echo ">>> Вывод логов тестов:"
docker logs -f "$CONTAINER_ID"

# Ждем завершения
EXIT_CODE=$(docker wait "$CONTAINER_ID")

echo ">>> Копирую результаты..."

# Создаем поддиректории
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Копируем результаты из контейнера
docker cp "$CONTAINER_ID:/app/logs/." "$TEST_OUTPUT_DIR/logs/" 2>/dev/null || echo "Не удалось скопировать логи"
docker cp "$CONTAINER_ID:/app/target/surefire-reports/." "$TEST_OUTPUT_DIR/results/" 2>/dev/null || echo "Не удалось скопировать результаты"
docker cp "$CONTAINER_ID:/app/target/site/." "$TEST_OUTPUT_DIR/report/" 2>/dev/null || echo "Не удалось скопировать отчет"

echo ""
echo "=== ТЕСТЫ ЗАВЕРШЕНЫ ==="
echo "Выходной код: $EXIT_CODE"
echo "Директория с результатами: $TEST_OUTPUT_DIR"
ls -la "$TEST_OUTPUT_DIR"

docker compose -f ./docker-compose.yml down

# Удаляем контейнер
echo ""
echo ">>> Очистка контейнеров..."
docker rm "$CONTAINER_ID" > /dev/null 2>&1

