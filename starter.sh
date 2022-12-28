docker compose -f ./redis/docker-compose.yml up &
docker compose -f ./kafka/docker-compose.yml up &
docker compose -f ./manager/docker_init/docker-compose.yml up &
docker compose -f ./matrix/docker_init/docker-compose.yml up &
