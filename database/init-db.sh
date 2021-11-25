#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbrdcommondata database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE dbrdcommondata WITH PASSWORD 'dbrdcommondata';
  CREATE DATABASE dbrdcommondata ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE dbrdcommondata TO dbrdcommondata;
  ALTER ROLE dbrdcommondata WITH LOGIN;
EOSQL

echo "Done creating database dbrdcommondata."
