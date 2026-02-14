#echo ">>> Остановить Docker Compose"
#docker compose -f ./docker-compose.yml down
#
#echo ">>> Docker pull все образы браузеров"
#
## Путь до  файла
#json_file="./config/browsers.json"
#
## Проверяем, что jq установлен
#if ! command -v jq &> /dev/null; then
#    echo "❌ jq is not installed. Please install jq and try again."
#    exit 1
#fi
#
## Извлекаем все значения .image через jq
#images=$(jq -r '.chrome.versions[].image, .firefox.versions[].image, .opera.versions[].image' "$json_file" | grep -v '^$')
#
## Пробегаем по каждому образу и выполняем docker pull
#for image in $images; do
#    echo "Pulling $image..."
#    docker pull "$image"
#done
#
#echo ""
#echo ">>> Запуск Docker Compose окружения"
#docker compose -f docker-compose.yml up -d
#echo "Ожидание запуска сервисов (30 секунд)...."

echo ">>> Остановить Docker Compose"
docker compose -f ./docker-compose.yml down

echo ">>> Удаление старых неиспользуемых образов"
docker image prune -f

echo ">>> Удаление старых образов браузеров (если есть)"
docker rmi selenoid/chrome:latest selenoid/firefox:latest selenoid/opera:latest 2>/dev/null || true

echo ">>> Docker pull все образы браузеров (актуальные vnc версии)"

# Путь до файла
json_file="./config/browsers.json"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Installing..."
    sudo apt-get update && sudo apt-get install -y jq
fi

# Извлекаем все значения .image через jq (с поддержкой вложенных версий)
images=$(jq -r '
  .chrome.versions[]?.image,
  .firefox.versions[]?.image,
  .opera.versions[]?.image
' "$json_file" | grep -v '^$' | sort -u)

# Проверяем, что нашли образы
if [ -z "$images" ]; then
    echo "❌ Не найдены образы в browsers.json"
    echo "Используем образы по умолчанию:"
    images="selenoid/vnc_chrome:latest selenoid/vnc_firefox:latest selenoid/vnc_opera:latest"
fi

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image" || echo "⚠️  Не удалось скачать $image, но продолжаем..."
done

echo ""
echo ">>> Запуск Docker Compose окружения"
docker compose -f docker-compose.yml up -d
echo "Ожидание запуска сервисов (15 секунд)...."
sleep 15

echo ">>> Проверка запущенных контейнеров"
docker ps

echo ">>> Проверка доступности фронтенда из Selenoid"
if docker exec infra-selenoid-1 wget -O- -T 5 http://frontend:80 2>/dev/null | grep -q "html"; then
    echo "✅ Фронтенд доступен из Selenoid"
else
    echo "⚠️  Фронтенд не отвечает из Selenoid, но тесты могут работать через localhost"
fi

echo ">>> Готово!"
