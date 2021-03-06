/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jafka.network.handlers;

import io.jafka.api.FetchRequest;
import io.jafka.api.RequestKeys;
import io.jafka.common.ErrorMapping;
import io.jafka.log.ILog;
import io.jafka.log.LogManager;
import io.jafka.message.MessageSet;
import io.jafka.mx.BrokerTopicStat;
import io.jafka.network.MessageSetSend;
import io.jafka.network.Receive;
import io.jafka.network.Send;

/**
 * handler for fetch request
 * 
 * @author adyliu (imxylz@gmail.com)
 * @since 1.0
 */
public class FetchHandler extends AbstractHandler {

    public FetchHandler(LogManager logManager) {
        super(logManager);
    }

    public Send handler(RequestKeys requestType, Receive request) {
        FetchRequest fetchRequest = FetchRequest.readFrom(request.buffer());
        if (logger.isDebugEnabled()) {
            logger.debug("Fetch request " + fetchRequest.toString());
        }
        return readMessageSet(fetchRequest);
    }

    protected MessageSetSend readMessageSet(FetchRequest fetchRequest) {
        final String topic = fetchRequest.topic;
        MessageSetSend response = null;
        try {
            ILog log = logManager.getLog(topic, fetchRequest.partition);
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching log segment for request=" + fetchRequest + ", log=" + log);
            }
            if (log != null) {
                response = new MessageSetSend(log.read(fetchRequest.offset, fetchRequest.maxSize));
                BrokerTopicStat.getInstance(topic).recordBytesOut(response.messages.getSizeInBytes());
                BrokerTopicStat.getBrokerAllTopicStat().recordBytesOut(response.messages.getSizeInBytes());
            } else {
                response = new MessageSetSend();
            }
        } catch (Exception e) {
            logger.error("error when processing request " + fetchRequest, e);
            BrokerTopicStat.getInstance(topic).recordFailedFetchRequest();
            BrokerTopicStat.getBrokerAllTopicStat().recordFailedFetchRequest();
            response = new MessageSetSend(MessageSet.Empty, ErrorMapping.valueOf(e));
        }
        return response;
    }
}
