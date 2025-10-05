<------------ Basic Kafka operations ------------>

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
<------------ pattition and routing key ------------->
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


<------------ Dead letter Topic ------------->

#DLT
docker exec -it kafka1 kafka-topics \
  --create \
  --topic smsEvent.DLT \
  --bootstrap-server kafka1:9092 \
  --partitions 3 \
  --replication-factor 3

Testing DLQ
Produce a good message

curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentStatus": "SUCCESS",
        "amount": 500,
        "createdDate": "2025-10-02T12:10:00",
        "updatedDate": "2025-10-02T12:15:00"
      }'
#✅ This will be consumed normally.

#2️⃣ Produce a bad message (amount < 0)
curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentStatus": "FAILED",
        "amount": -200,
        "createdDate": "2025-10-02T12:10:00",
        "updatedDate": "2025-10-02T12:15:00"
      }'
#	• Consumer will throw exception.
#	• After max retries → message lands in DLQ topic smsEvent.DLT.

#3️⃣ Check DLQ Run console consumer:
docker exec -it kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:19092 \
  --topic smsEvent.DLT \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true
#You’ll see the failed message + headers (including error cause).


<------------ Filter messages------------>
#✅ Case 1: Should be processed
curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentStatus": "FAILED",
        "amount": 200,
        "createdDate": "2025-10-02T12:10:00",
        "updatedDate": "2025-10-02T12:15:00"
      }'
#❌ Case 2: Should be dropped
curl -X POST http://localhost:8081/smsProducer \
  -H "Content-Type: application/json" \
  -d '{
        "paymentStatus": "COMPLETED",
        "amount": 500,
        "createdDate": "2025-10-02T12:10:00",
        "updatedDate": "2025-10-02T12:15:00"
      }'

#verify in console
docker exec -it kafka1 kafka-console-consumer \
  --bootstrap-server kafka1:19092 \
  --topic smsEvent \
  --from-beginning \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true


# Delete topics
docker exec -it kafka1 kafka-topics \
  --delete \
  --topic emailProducer-in-0 \
  --bootstrap-server kafka1:19092

docker exec -it kafka1 kafka-topics \
  --delete \
  --topic smsProducer-in-0 \
  --bootstrap-server kafka1:19092