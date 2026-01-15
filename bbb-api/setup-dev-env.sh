#!/bin/bash
set -e -x

go_version=$(go version)

# Extract the version number
version_number=$(echo $go_version | awk '{print $3}' | cut -d "o" -f 2)

# Check if the version starts with 1.24
if [[ ! $version_number == 1.24* ]]; then
  echo "Installing Go 1.24"
  sudo rm -rf /usr/local/go
  wget https://go.dev/dl/go1.24.5.linux-amd64.tar.gz
  sudo tar -C /usr/local -xzf go1.24.5.linux-amd64.tar.gz
  export PATH=$PATH:/usr/local/go/bin
  source ~/.bashrc
  rm go1.24.5.linux-amd64.tar.gz
fi


go version

# Install Docker if not already installed
if ! command -v docker &> /dev/null; then
  echo "Installing Docker"
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl
  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  sudo chmod a+r /etc/apt/keyrings/docker.asc
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  sudo usermod -aG docker $USER
  echo "Docker installed. You may need to log out and back in for group changes to take effect."
fi

# Install golangci-lint if not already installed
if ! command -v golangci-lint &> /dev/null; then
  echo "Installing golangci-lint"
  curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sudo sh -s -- -b /usr/local/bin
fi

sudo apt-get install protobuf-compiler -y
sudo go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
sudo go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest

cd "$(dirname "$0")"

#export PATH=$PATH:$(go env GOPATH)/bin
sudo ./proto-gen.sh

sudo go get google.golang.org/grpc@v1.64.0
sudo go mod tidy

sudo cp core-api.nginx /usr/share/bigbluebutton/nginx
sudo cp ../build/packages-template/bbb-graphql-middleware/graphql.nginx /usr/share/bigbluebutton/nginx
sudo cp ../bigbluebutton-web/bbb-web.nginx /usr/share/bigbluebutton/nginx/web
#sudo cp ../build/packages-template/bbb-graphql-server/hasura-config.env /etc/default/bbb-graphql-server
#sudo ../bbb-graphql-server/update_graphql_data.sh
sudo systemctl restart nginx bbb-graphql-middleware

sudo cp ../akka-bbb-apps/src/universal/conf/application.conf /usr/share/bbb-apps-akka/conf/application.conf

mkdir -p /tmp/meteor-assets-nginx-cache

echo "Done"