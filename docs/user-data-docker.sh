#!/bin/bash
# Update the system
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
