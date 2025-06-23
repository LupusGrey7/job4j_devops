#!/bin/bash
set -eo pipefail  # Жесткий режим обработки ошибок

# Параметры (берутся из переменных окружения)
PG_USER="${DB_USERNAME:-postgres}"                          # Пользователь# Значение по умолчанию, если переменная не задана
PG_PASSWORD="${DB_PASSWORD:?ERROR: DB_PASSWORD is required}"# Не храните пароль в скрипте!
PG_DATABASE="${DB_NAME:-job4j_devops}"                      # Имя БД из .env.local
PG_HOST="${DB_HOST:-localhost}"                             # Т.к. БД на том же сервере (PostgreSQL in Debian)
BACKUP_DIR="${BACKUP_DIR:-/var/jenkins_home/backups/postgresql}"

TIMESTAMP=$(date +"%F_%H-%M-%S")
BACKUP_FILE="${BACKUP_DIR}/${PG_DATABASE}_${TIMESTAMP}.sql.gz"

# Создаем директорию если нет
mkdir -p "$BACKUP_DIR"

export PGPASSWORD="$PG_PASSWORD"
# Логирование
echo "=== Starting backup ==="
echo "Database: $PG_DATABASE"
echo "Host: $PG_HOST"
echo "Backup file: $BACKUP_FILE"

# Выполняем бэкап с таймером
time {
    if ! pg_dump -U "$PG_USER" -h "$PG_HOST" -d "$PG_DATABASE" | gzip > "$BACKUP_FILE"; then
        echo "!!! Backup failed !!!"
        exit 1
    fi
}

echo "=== Backup successful ==="
echo "File size: $(du -h "$BACKUP_FILE" | cut -f1)"
exit 0