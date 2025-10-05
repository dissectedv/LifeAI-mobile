FROM python:3.13.3-alpine3.21

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

COPY . /app

# Cria virtualenv, instala requirements, cria diretórios e permissões
RUN python -m venv /venv && \
    /venv/bin/pip install --upgrade pip && \
    /venv/bin/pip install -r /app/requirements.txt && \
    adduser --disabled-password --no-create-home duser && \
    mkdir -p /app/staticfiles /app/media && \
    chown -R duser:duser /venv /app/staticfiles /app/media && \
    chmod -R 755 /app/staticfiles /app/media && \
    chmod -R +x /app/scripts

ENV PATH="/app/scripts:/venv/bin:$PATH"
USER duser

EXPOSE 8000

CMD ["/app/scripts/commands.sh"]
