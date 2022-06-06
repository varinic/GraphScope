package com.alibaba.maxgraph.dataload;

import com.alibaba.graphscope.groot.sdk.MaxGraphClient;

import java.io.IOException;

public class IngestDataCommand extends DataCommand {

    public IngestDataCommand(String dataPath, boolean is_oss) throws IOException {
        super(dataPath, is_oss);
    }

    public void run() {
        MaxGraphClient client =
                MaxGraphClient.newBuilder()
                        .setHosts(graphEndpoint)
                        .setUsername(username)
                        .setPassword(password)
                        .build();
        client.ingestData(dataPath);
    }
}
