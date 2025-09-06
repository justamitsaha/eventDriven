1\.	Create a VM

gcloud compute instances create kafka-vm \\

&nbsp; --zone=us-central1-a \\

&nbsp; --machine-type=e2-medium \\

&nbsp; --image-project=debian-cloud \\

&nbsp; --image-family=debian-11

2\.	SSH into VM

gcloud compute ssh kafka-vm --zone=us-central1-a

3\.	Install Docker \& Docker Compose

sudo apt update

sudo apt install -y docker.io docker-compose

sudo usermod -aG docker $USER

newgrp docker

4\.	Copy your docker-compose.yml to the VM

If you’re on your local machine:

gcloud compute scp docker-compose.yml kafka-vm:~/ --zone=us-central1-a

5\.	Run Kafka

docker-compose up -d

6\.	Open firewall for Kafka

o	By default, GCE blocks external traffic. Run:

gcloud compute firewall-rules create kafka-ports \\

&nbsp; --allow=tcp:9092,tcp:9093,tcp:9094,tcp:2181 \\

&nbsp; --target-tags=kafka-vm

o	Or update the VM’s VPC firewall rules in the GCP Console.

7\.	Change KAFKA\_ADVERTISED\_LISTENERS in your YAML

Replace localhost with the external IP of your VM (check with gcloud compute instances list).



