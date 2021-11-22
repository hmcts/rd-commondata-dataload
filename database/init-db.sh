#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating refcommondata database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE refcommondata WITH PASSWORD 'refcommondata';
  CREATE DATABASE refcommondata ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE refcommondata TO refcommondata;
  ALTER ROLE refcommondata WITH LOGIN;
EOSQL

echo "Done creating database refcommondata."
