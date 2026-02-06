#!/bin/bash
# push-tests.sh

# Загружаем из .env файла
if [ -f .env ]; then
    source .env
fi

# Проверяем
if [ -z "${DOCKERHUB_TOKEN}" ]; then
    echo "Ошибка: Токен не найден"
    echo "Создайте файл .env с содержимым:"
    echo "DOCKERHUB_TOKEN=ваш_токен"
    echo "DOCKERHUB_USERNAME=ваш_логин"
    exit 1
fi

# Конфигурация
LOCAL_IMAGE_NAME="nbank-tests"
DOCKERHUB_USERNAME="ohacksley"
IMAGE_NAME="nbank-tests"
TAG="latest"

echo "=== ОТПРАВКА ОБРАЗА В DOCKER HUB ==="
echo ""

# Авторизация в Docker Hub
echo ">>> Авторизация в Docker Hub..."
echo "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
echo "✅ Успешная авторизация"
echo ""

# Тегирование образа
echo ">>> Тегирование образа..."
docker tag "$LOCAL_IMAGE_NAME" "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
echo "✅ Образ тегирован как: $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
echo ""

# Отправка образа в Docker Hub
echo ">>> Отправка образа в Docker Hub..."
docker push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
echo "✅ Образ успешно отправлен в Docker Hub!"
echo ""

# Финальное сообщение
echo "=== ОБРАЗ УСПЕШНО ОПУБЛИКОВАН ==="
echo ""
echo "Для использования образа выполните команду:"
echo "  docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
echo ""
echo "Для запуска тестов:"
echo "  docker run --rm \\"
echo "    -e TEST_PROFILE=api \\"
echo "    -e APIBASEURL=ваш_сервер \\"
echo "    -e UIBASEURL=ваш_сервер \\"
echo "    $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
echo ""
echo "Для выхода из Docker Hub: docker logout"