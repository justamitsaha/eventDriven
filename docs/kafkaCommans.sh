docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment --replication-factor 3 --partitions 3


docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment.RETRY --replication-factor 3 --partitions 3

docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment.DLT --replication-factor 3 --partitions 3

docker exec --interactive --tty kafka1  kafka-console-consumer --bootstrap-server kafka1:19092 --topic payment --from-beginning
