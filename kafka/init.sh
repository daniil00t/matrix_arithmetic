docker run --platform linux/amd64 --rm --entrypoint="" bitnami/kafka:2.8.1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --bootstrap-server 192.168.1.106:29092 --topic matrix.worker.rq_matrix --partitions 1 --replication-factor 1
docker run --platform linux/amd64 --rm --entrypoint="" bitnami/kafka:2.8.1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --bootstrap-server 192.168.1.106:29092 --topic matrix.worker.rq_number --partitions 1 --replication-factor 1
docker run --platform linux/amd64 --rm --entrypoint="" bitnami/kafka:2.8.1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --bootstrap-server 192.168.1.106:29092 --topic matrix.worker.rs_topic --partitions 1 --replication-factor 1