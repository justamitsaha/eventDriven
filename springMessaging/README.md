Set up the Kafka on the host
    
    1> Set up docker, docker compose user data for EC2
```shell
#!/bin/bash
# Update the system
yum update -y
yum install -y httpd
systemctl start httpd
systemctl enable httpd
echo "<h1>Hello World from $(hostname -f)</h1>" > /var/www/html/index.html

sudo yum update -y

# Install Docker
sudo amazon-linux-extras enable docker
sudo yum install -y docker

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add the EC2 user to the docker group to run docker commands without sudo
sudo usermod -aG docker ec2-user

# Download the latest stable version of Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Make it executable
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker-compose --version


# Verify installations
docker --version
docker-compose --version

# Optional: Reboot the instance to apply group changes
sudo reboot
```
    2> Add the IP of the host in the .env file. For e.g. if it is a EC2 instance add the public IP. This IP will be used in docker compose file and kafka will use it to broadcast brokers
    3> Keep the docker-compose.yml in the same location and do docker compose up 
    4> Set up Kafka topics

Kafka commands to set up kafka
```shell
#Creare topic
docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment --replication-factor 3 --partitions 3

docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment.RETRY --replication-factor 3 --partitions 3

docker exec --interactive --tty kafka1  kafka-topics --bootstrap-server kafka1:19092 --create --topic payment.DLT --replication-factor 3 --partitions 3

#Create consumer
docker exec --interactive --tty kafka1  kafka-console-consumer --bootstrap-server kafka1:19092 --topic payment --from-beginning

#Describe topic
docker exec --interactive --tty kafka1 kafka-topics --bootstrap-server kafka1:19092 --describe

docker exec --interactive --tty kafka1 kafka-topics --bootstrap-server kafka1:19092 --describe --topic test-topic

```