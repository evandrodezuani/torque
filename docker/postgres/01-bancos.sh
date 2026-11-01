#!/bin/sh
# Roda só na primeira subida, com o volume do banco vazio. O banco torque é criado pela própria imagem.
# torque_api_test: banco dos testes de API, separado dos dados do dia a dia.
# sonar: banco do SonarQube, com usuário próprio.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE torque_api_test;
	CREATE USER $SONAR_DB_USER WITH PASSWORD '$SONAR_DB_PASSWORD';
	CREATE DATABASE sonar OWNER $SONAR_DB_USER;
EOSQL
