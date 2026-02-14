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

echo ">>> Docker pull все образы браузеров"

# Путь до файла (для Windows в Git Bash)
json_file="./config/browsers.json"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    echo "Установите jq через: winget install jq или chocolatey"
    exit 1
fi

# Извлекаем все значения .image через jq (универсальный способ)
echo ">>> Читаем образы из browsers.json..."
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")

# Проверяем, что нашли образы
if [ -z "$images" ]; then
    echo "❌ Не найдены образы в browsers.json"
    echo "Содержимое файла:"
    cat "$json_file"
    exit 1
fi

echo ">>> Найдены образы:"
echo "$images"

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image" || echo "⚠️  Не удалось скачать $image, но продолжаем..."
done

echo ""
echo ">>> Запуск Docker Compose"
docker compose -f docker-compose.yml up -d

echo "Ожидание запуска сервисов (15 секунд)...."
sleep 15