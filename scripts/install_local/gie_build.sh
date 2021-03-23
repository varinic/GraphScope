#!/bin/bash

TZ=Asia/Shanghai
ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

sudo apt update -y
sudo apt install -y perl \
                    openjdk-8-jdk \
                    maven \
                 && rm -rf /var/lib/apt/lists/*

# Java

cd ../../interactive_engine/ && \
mvn clean package -Pjava-release -DskipTests

# Rust
cd ../scripts/install_local
wget --no-verbose https://golang.org/dl/go1.15.5.linux-amd64.tar.gz
tar -C /usr/local -xzf go1.15.5.linux-amd64.tar.gz
rm go1.15.5.linux-amd64.tar.gz
curl -sf -L https://static.rust-lang.org/rustup.sh | sh -s -- -y --profile minimal --default-toolchain 1.48.0
echo "source ~/.cargo/env" >> ~/.bashrc
path='${PATH}'
echo "export PATH=/usr/local/go/bin:$path" >> ~/.bashrc

export PATH=${PATH}:/usr/local/go/bin
source ~/.bashrc
cd ../../interactive_engine/src/executor
export CMAKE_PREFIX_PATH=/usr/local/
export LIBRARY_PATH=$LIBRARY_PATH:/usr/local/lib
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
./exec.sh cargo build --all --release
