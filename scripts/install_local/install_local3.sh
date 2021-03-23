#!/bin/bash

# build analytical engine
cd ../../analytical_engine && \
mkdir -p build && \
cd build && \
cmake .. && \
make gsa_cpplint && \
make -j`nproc` && \
sudo make install && \
rm -fr CMake* && \
echo "Build and install analytical_engine done."


cd ../../python && \
python3 setup.py build_proto && \
python3 setup.py install && \
cd ../coordinator && \
python3 setup.py install && \
python3 setup.py build_builtin
