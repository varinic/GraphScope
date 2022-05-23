/**
 * Copyright 2020 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.maxgraph.dataload.databuild;

import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;

public class SstRecordWriter {
    private SstFileWriter sstFileWriter;
    private OSS ossClient = null;
    private PutObjectRequest putObjectRequest;

    public void putMeta(String data,
                   String fileName, ArrayList<String> ossConf)
                                              throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        if (!file.exists()) {
            file.createNewFile();
        }
        fos.write(data.getBytes());
        fos.flush();
        fos.close();

        OSS ossClient = null;

        System.out.println(ossConf.get(0)+" "+ossConf.get(1)+" "+ossConf.get(2));
        try {
            ossClient = new OSSClientBuilder().build(ossConf.get(0),
                                                 ossConf.get(1), ossConf.get(2));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConf.get(3),
                                                   ossConf.get(4)+"/"+fileName, file);
            ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public SstRecordWriter(String fileName,
                         ArrayList<String> ossConf) throws IOException {
        this.ossClient = new OSSClientBuilder().build(ossConf.get(0),
                                              ossConf.get(1), ossConf.get(2));
        this.putObjectRequest = new PutObjectRequest(ossConf.get(3),
                             ossConf.get(4)+"/"+fileName, new File(fileName));
        Options options = new Options();
        options.setCreateIfMissing(true)
                .setWriteBufferSize(512 << 20)
                .setMaxWriteBufferNumber(8)
                .setTargetFileSizeBase(512 << 20);
        this.sstFileWriter = new SstFileWriter(new EnvOptions(), options);
        System.out.println("fileName: "+fileName);
        try {
            sstFileWriter.open(fileName);
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }

    public void write(String key, String value) throws IOException {
        try {
            byte newBytes[] = key.getBytes();
            String out = "";
            for (int i=0; i<newBytes.length; i++) {
                out += Integer.toHexString(newBytes[i]);
            }
            System.out.println("out: "+out);
            sstFileWriter.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }

    public void close() throws IOException {
        try {
            sstFileWriter.finish();
        } catch (RocksDBException e) {
            throw new IOException(e);
        }

        try {
            ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
