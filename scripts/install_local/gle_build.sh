#!/bin/bash

# build & install graph-learn library
cd ../../learning_engine && \
cd graph-learn/ && \
git submodule update --init && \
git submodule update --init third_party/pybind11 && \
mkdir -p cmake-build && \
cd cmake-build && \
cmake -DCMAKE_PREFIX_PATH=/usr/local/ \
      -DCMAKE_INSTALL_PREFIX=/usr/local/ \
      -DWITH_VINEYARD=ON \
      -DTESTING=OFF .. && \
make graphlearn_shared && \
sudo make install -j
