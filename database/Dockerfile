FROM postgres:11

COPY init-db.sh /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d dbcommondata -U dbcommondata

EXPOSE 5432
