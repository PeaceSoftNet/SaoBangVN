/*
 * Copyright 2012 Hadoop Vietnam <admin@hadoopvietnam.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elasticsearch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchClient {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
    private Client client;
    private String elasticIndex = "logging-index";
    private String elasticType = "logging";
    private String url = "localhost";
    private int port = 9300;

    public ElasticSearchClient() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "graylog2").build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(url, port));
    }

    protected void writeBasic(Map<String, Object> json, LoggingEvent event) throws JSONException {
        json.put("threadName", event.getThreadName());
        json.put("level", event.getLevel().toString());
        json.put("timestamp", event.getTimeStamp());
        json.put("message", event.getMessage());
        json.put("logger", event.getLoggerName());
    }

    protected void writeThrowable(Map<String, Object> json, LoggingEvent event) throws JSONException {
        ThrowableInformation ti = event.getThrowableInformation();
        if (ti != null) {
            Throwable t = ti.getThrowable();
            json.put("className", t.getClass().getCanonicalName());
            json.put("stackTrace", getStackTrace(t));
        }
    }

    protected String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Method is called by ExecutorService and publishes message on RabbitMQ
     *
     * @return
     * @throws Exception
     */
    public void call() throws Exception {
        // Set up the es index response 
        String uuid = UUID.randomUUID().toString();
        IndexRequestBuilder response = client.prepareIndex(elasticIndex, elasticType, uuid);
        Map<String, Object> data = new HashMap<String, Object>();

//        writeBasic(data, loggingEvent);
//        writeThrowable(data, loggingEvent);
//        logger.
        data.put("level", "INFO");
        data.put("timestamp", System.currentTimeMillis());
        data.put("message", "Process time " + System.currentTimeMillis());

        // insert the document into elasticsearch
        response.setSource(data);
        response.execute();
    }

    public static void main(String[] args) throws Exception {
        ElasticSearchClient client = new ElasticSearchClient();
        client.call();
    }
}
