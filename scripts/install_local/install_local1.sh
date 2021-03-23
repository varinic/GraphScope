#!/bin/bash


TZ=Asia/Shanghai
ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


apt update
apt upgrade
apt install git vim sudo

sudo apt update -y
sudo apt install -y \
                    binutils-dev \
                    build-essential \
                    gdb-dbg \
                    ca-certificates \
                    cmake \
                    curl \
                    git \
                    iputils-ping \
                    libapr1-dev \
                    libaprutil1-dev \
                    libboost-all-dev \
                    libdouble-conversion-dev \
                    libevent-dev \
                    libgoogle-glog-dev \
                    libgflags-dev \
                    libgtest-dev \
                    libiberty-dev \
                    libjemalloc-dev \
                    liblz4-dev \
                    liblzma-dev \
                    libmxml-dev \
                    librdkafka-dev \
                    libsnappy-dev \
                    libssl-dev \
                    libunwind-dev \
                    lsb-release \
                    openssl \
                    openssh-client \
                    openssh-server \
                    pkg-config \
                    python3 \
                    python3-pip \
                    telnet \
                    vim \
                    wget \
                    zip \
                    zlib1g-dev

sudo apt update -y
sudo apt install -y ccache \
                    doxygen \
                    libcurl4-openssl-dev \
                    libgmock-dev \
                    libkrb5-dev \
                    libprotobuf-dev \
                    libgsasl7-dev \
                    libxml2-dev \
                    libz-dev \
                    protobuf-compiler-grpc \
                    uuid-dev \
                 && rm -rf /var/lib/apt/lists/*

# libtinfo5
sudo apt update -y
sudo apt install -y libtinfo5

# pip dependencies
pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple pip -U && \
pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple && \
pip3 install -U pip && \
pip3 install yapf==0.30.0 sphinx sphinx_rtd_theme breathe gcovr && \
pip3 install auditwheel daemons grpcio-tools gremlinpython hdfs3 fsspec oss2 s3fs ipython kubernetes \
    libclang networkx==2.4 numpy pandas parsec pycryptodome pyorc pytest scipy scikit_learn wheel pycrypto && \
pip3 install Cython --pre -U

if [ $? -ne 0 ]; then
    echo "Failed to install python packages using pip3, please check errors or try again!"
    exit 1
fi

# install apache-arrow
wget https://apache.bintray.com/arrow/$(lsb_release --id --short | tr 'A-Z' 'a-z')/apache-arrow-archive-keyring-latest-$(lsb_release --codename --short).deb
sudo apt install -y -V ./apache-arrow-archive-keyring-latest-$(lsb_release --codename --short).deb
sudo apt update
sudo apt install -y libarrow-dev=1.0.1-1 libarrow-python-dev=1.0.1-1
rm ./apache-arrow-archive-keyring-latest-$(lsb_release --codename --short).deb

# install pyarrow from scratch
sudo pip3 install --no-binary pyarrow pyarrow==1.0.1

if [ $? -ne 0 ]; then
    echo "Failed to install python packages using pip3, please check errors or try again!"
    exit 1
fi

# grpc
git clone -b v1.33.1 --recursive --depth=1 https://github.com/grpc/grpc /tmp/grpc && \
cd /tmp/grpc && \
mkdir -p cmake/build && \
cd cmake/build && \
cmake ../.. -DBUILD_SHARED_LIBS=ON -DgRPC_INSTALL=ON -DCMAKE_INSTALL_PREFIX=/usr && \
make -j`nproc` install


# install etcd
wget https://github.com/etcd-io/etcd/releases/download/v3.4.13/etcd-v3.4.13-linux-amd64.tar.gz
tar zxvf etcd-v3.4.13-linux-amd64.tar.gz
sudo mv etcd-v3.4.13-linux-amd64/etcd /usr/local/bin/
sudo mv etcd-v3.4.13-linux-amd64/etcdctl /usr/local/bin/
rm -rf ./etcd-v3.4.13-linux-amd64*


# fmt v7.0.3, required by folly
cd /tmp && \
wget https://github.com/fmtlib/fmt/archive/7.0.3.tar.gz && \
tar zxvf 7.0.3.tar.gz && \
cd fmt-7.0.3/ && \
mkdir build && \
cd build && \
cmake .. -DBUILD_SHARED_LIBS=ON && \
make install -j && \
cd /tmp && \
rm -fr /tmp/7.0.3.tar.gz /tmp/fmt-7.0.3

# folly v2020.10.19.00
cd /tmp && \
wget https://github.com/facebook/folly/archive/v2020.10.19.00.tar.gz && \
tar zxvf v2020.10.19.00.tar.gz && \
cd folly-2020.10.19.00 && mkdir _build && \
cd _build && \
cmake -DBUILD_SHARED_LIBS=ON -DCMAKE_POSITION_INDEPENDENT_CODE=ON .. && \
make install -j && \
cd /tmp && \
rm -fr /tmp/v2020.10.19.00.tar.gz /tmp/folly-2020.10.19.00


# openmpi v4.0.5
cd /tmp && \
wget https://download.open-mpi.org/release/open-mpi/v4.0/openmpi-4.0.5.tar.gz && \
tar zxvf openmpi-4.0.5.tar.gz && \
cd openmpi-4.0.5 && ./configure && \
make -j`nproc`

if [ $? -ne 0 ]; then
    echo "Failed to install openmpi, please check errors or try again!"
    cd /tmp && \
    rm -fr /tmp/openmpi-4.0.5 /tmp/openmpi-4.0.5.tar.gz
    exit 1
fi

make install && \
cd /tmp && \
rm -fr /tmp/openmpi-4.0.5 /tmp/openmpi-4.0.5.tar.gz


# install hdfs runtime library
cd /tmp && \
git clone https://github.com/7br/libhdfs3-downstream.git && \
cd libhdfs3-downstream/libhdfs3 && \
mkdir -p /tmp/libhdfs3-downstream/libhdfs3/build && \
cd /tmp/libhdfs3-downstream/libhdfs3/build && \
cmake .. -DBUILD_SHARED_LIBS=ON \
         -DBUILD_HDFS3_TESTS=OFF && \
make install -j && \
cd /tmp && \
rm -rf /tmp/libhdfs3-downstream

