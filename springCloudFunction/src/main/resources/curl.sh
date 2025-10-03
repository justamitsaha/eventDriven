curl -X POST http://localhost:8081/sms -H "Content-Type: application/json" -d '{"paymentUuid": 1234, "paymentStatus": "PROCESSING", "amount": 9999}'

#1. Create topics
docker exec -it kafka1 kafka-topics \
  --create \
  --topic smsEvent \
  --bootstrap-server kafka1:9092 \
  --partitions 3 \
  --replication-factor 3

docker exec -it kafka1 kafka-topics \
  --create \
  --topic emailEvent \
  --bootstrap-server kafka1:9092 \
  --partitions 3 \
  --replication-factor 3

#3. Verify topics
docker exec -it kafka1 kafka-topics --list --bootstrap-server kafka1:9092

docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --describe --topic emailEvent
docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --describe --topic smsEvent

#consumer
docker exec -it kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:19092 \
  --topic emailEvent \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true

docker exec -it kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:19092 \
  --topic smsEvent \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true


#test
curl -X POST http://localhost:8081/emailProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentUuid": 2323323323,
        "paymentStatus": "PENDING",
        "amount": 500,
        "createdDate": "2025-10-02T12:00:00",
        "updatedDate": "2025-10-02T12:05:00"
      }'

curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentStatus": "PENDING",
        "amount": 500,
        "createdDate": "2025-10-02T12:00:00",
        "updatedDate": "2025-10-02T12:05:00"
      }'

#Copy UUID from the response of the first request and use it in the next request it will go to same partition
curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentUuid":"a2163367-8535-4c4f-8fed-20a35ce0695c",
        "paymentStatus": "SUCCESS",
        "amount": 750,
        "createdDate": "2025-10-02T12:10:00",
        "updatedDate": "2025-10-02T12:15:00"
      }'


#producer
docker exec -it kafka1 kafka-console-producer \
  --bootstrap-server kafka1:19092 \
  --topic emailEvent \
  --property "parse.key=true" \
  --property "key.separator=:"

1:{"paymentUuid":2001,"paymentStatus":"SUCCESS","amount":750,"createdDate":"2025-10-02T12:10:00","updatedDate":"2025-10-02T12:15:00"}

#without key
docker exec -it kafka1 kafka-console-producer \
  --bootstrap-server kafka1:19092 \
  --topic emailEvent

{"paymentUuid":2001,"paymentStatus":"SUCCESS","amount":750,"createdDate":"2025-10-02T12:10:00","updatedDate":"2025-10-02T12:15:00"}



# Delete topics
docker exec -it kafka1 kafka-topics \
  --delete \
  --topic emailProducer-in-0 \
  --bootstrap-server kafka1:19092

docker exec -it kafka1 kafka-topics \
  --delete \
  --topic smsProducer-in-0 \
  --bootstrap-server kafka1:19092