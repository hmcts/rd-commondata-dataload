#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbcommondata database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE dbcommondata WITH PASSWORD 'dbcommondata';
  CREATE DATABASE dbcommondata ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE dbcommondata TO dbcommondata;
  ALTER ROLE dbcommondata WITH LOGIN;
EOSQL

echo "Done creating database dbcommondata."
