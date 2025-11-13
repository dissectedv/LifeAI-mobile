#!/bin/sh
set -e

# Espera o Postgres iniciar
while ! nc -z $DB_HOST $DB_PORT; do
  echo "🟡 Waiting for Postgres Database Startup ($DB_HOST $DB_PORT) ..."
  sleep 2
done

echo "✅ Postgres Database Started Successfully ($DB_HOST:$DB_PORT)"

# Roda migrations e coletar static
python manage.py collectstatic --noinput
python manage.py migrate --noinput

# Roda servidor Django
python manage.py runserver 0.0.0.0:8000