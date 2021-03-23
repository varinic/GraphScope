#!/bin/bash

cd /tmp && \
git clone https://github.com/alibaba/libgrape-lite.git --depth=1 && \
cd libgrape-lite && \
mkdir build && \
cd build && \
cmake .. && \
make -j`nproc` && \
make install && \
cd /tmp && \
git clone https://github.com/alibaba/libvineyard.git --depth=1 && \
cd libvineyard && \
git submodule update --init && \
mkdir -p /tmp/libvineyard/build && \
cd /tmp/libvineyard/build && \
cmake .. -DBUILD_VINEYARD_PYPI_PACKAGES=ON \
         -DBUILD_SHARED_LIBS=ON \
         -DBUILD_VINEYARD_IO_OSS=ON && \
make -j`nproc`
make vineyard_client_python -j`nproc`
sudo make install
#
## build & install vineyard for python
cd ..
python3 setup.py bdist_wheel
sudo pip3 install dist/*.whl -U
#
## build & install vineyard-io
cd modules/io
python3 setup.py bdist_wheel
sudo pip3 install dist/*.whl -U
