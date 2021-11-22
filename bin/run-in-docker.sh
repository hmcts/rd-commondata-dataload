#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

clean_old_docker_artifacts() {

    docker stop rd-commondata-db

    docker rm rd-commondata-db

    docker rmi hmcts/rd-judicial-db

    docker volume rm rd-commondata-db-volume
}

execute_script() {

   clean_old_docker_artifacts

   docker-compose down -v

   docker system prune –af

  ./gradlew clean assemble

  if [ -f ~/.bash_functions ]; then
      . ~/.bash_functions
      get_az_keyvault_secrets 'rd'
  fi

  export SERVER_PORT="${SERVER_PORT:-8100}"

  pwd

  chmod +x bin/*

  docker-compose up
}

execute_script
