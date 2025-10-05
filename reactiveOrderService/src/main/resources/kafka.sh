docker exec -it kafka1 kafka-topics \
  --create \
  --topic order.events \
  --bootstrap-server kafka1:9092 \
  --partitions 3 \
  --replication-factor 3

#3. Verify topics
docker exec -it kafka1 kafka-topics --list --bootstrap-server kafka1:9092

docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --describe --topic order.events

docker exec -it kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:19092 \
  --topic order.events \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true