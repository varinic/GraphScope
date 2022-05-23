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


import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.aliyun.odps.io.BytesWritable;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.data.TableInfo;
import com.aliyun.odps.mapred.JobClient;
import com.aliyun.odps.mapred.MapperBase;
import com.aliyun.odps.mapred.ReducerBase;
import com.aliyun.odps.mapred.TaskContext;
import com.aliyun.odps.mapred.conf.JobConf;
import com.aliyun.odps.mapred.utils.InputUtils;
import com.aliyun.odps.mapred.utils.OutputUtils;
import com.aliyun.odps.mapred.utils.SchemaUtils;
import com.aliyun.odps.mapred.TaskId;

import org.rocksdb.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public class DataBuildReducerOdps extends ReducerBase {
    private Record result = null;
    private String ossAccessId = null;
    private String ossAccessKey = null;
    private String ossEndPoint = null;
    private String ossBucketName = null;
    private String ossObjectName = null;

    private ArrayList<String> ossConf = null;
    private SstRecordWriter sstRecordWriter = null;
    private String taskId = null;

    //private String graphEndpoint = null;
    //private String schemaJson = null;
    //private String mappings = null;
    private String metaInfo = null;

    @Override
    public void setup(TaskContext context) throws IOException {
    //public void setup(ReduceContext<String, String> context) throws IOException {
        this.result = context.createOutputRecord();
        this.ossAccessId = context.getJobConf().get(OfflineBuildOdps.OSS_ACCESS_ID);
        this.ossAccessKey = context.getJobConf().get(OfflineBuildOdps.OSS_ACCESS_KEY);
        this.ossEndPoint = context.getJobConf().get(OfflineBuildOdps.OSS_ENDPOINT);
        this.ossBucketName = context.getJobConf().get(OfflineBuildOdps.OSS_BUCKET_NAME);
        this.ossObjectName = context.getJobConf().get(OfflineBuildOdps.OSS_OBJECT_NAME);

        //this.graphEndpoint = context.getJobConf().get(OfflineBuildOdps.GRAPH_ENDPOINT);
        //this.schemaJson = context.getJobConf().get(OfflineBuildOdps.SCHEMA_JSON);
        //this.mappings = context.getJobConf().get(OfflineBuildOdps.COLUMN_MAPPINGS);
        this.metaInfo = context.getJobConf().get(OfflineBuildOdps.META_INFO);

        ossEndPoint = "https://" + ossEndPoint;

        ossConf = new ArrayList<String>();
        ossConf.add(ossEndPoint);
        ossConf.add(ossAccessId);
        ossConf.add(ossAccessKey);
        ossConf.add(ossBucketName);

        this.taskId = context.getTaskID().toString();
        taskId = taskId.substring(taskId.length() - 5);
        System.out.println("taskId: "+taskId);
        //String fileName = "part-r-" + String.format("%05d", taskId) + ".sst";
        String fileName = "part-r-" + taskId + ".sst";

        Method[] methods = context.getClass().getDeclaredMethods();
        for (Method m : methods) {
            System.out.println("method: "+m.getName());
        }


        ossConf.add(ossObjectName);

        try {
            this.sstRecordWriter = new SstRecordWriter(fileName, ossConf);
        } catch (IOException e) {
            //throw e;
            //e.printStackTrace();
            //System.out.println(e.getStackTrace());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            System.out.println(sStackTrace);
        }
    }

    @Override
    public void reduce(Record key, Iterator<Record> values, TaskContext context)
        throws IOException {
        while (values.hasNext()) {
            Record value = values.next();
            result.set(0, "");
            result.set(1, "");
            try {
                sstRecordWriter.write((String)key.get(0), (String)value.get(0));
            } catch (IOException e) {
                //throw e;
                //e.printStackTrace();
                //System.out.println(e.getStackTrace());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }
        }
    }

    @Override
    public void cleanup(TaskContext context)
        throws IOException {
        try {
            sstRecordWriter.close();
        } catch (IOException e) {
            //throw e;
            //e.printStackTrace();
            //System.out.println(e.getStackTrace());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            System.out.println(sStackTrace);
        }
        if ("00000".equals(taskId)) {
            try {
                sstRecordWriter.putMeta(metaInfo, "META", ossConf);
            } catch (IOException e) {
                throw e;
            }
            System.out.println("metaInfo: "+metaInfo);
        }
    }
}
