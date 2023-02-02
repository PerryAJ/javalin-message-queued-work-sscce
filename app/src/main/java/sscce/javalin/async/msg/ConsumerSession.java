package sscce.javalin.async.msg;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.MessageHandler;

public class ConsumerSession {
    public final ClientSession session;
    public final ClientConsumer consumer;
    public final ClientSessionFactory factory;
    public final MessageHandler handler;

    public ConsumerSession(ClientSessionFactory factory, String targetQueue, MessageHandler handler) {
        this.factory = factory;
        try {
            this.session = factory.createSession(true, true);
            if (!session.queueQuery(SimpleString.toSimpleString(targetQueue)).isExists()) {
                var incomingQueueConfig = new QueueConfiguration(targetQueue)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setAddress(targetQueue);
                session.createQueue(incomingQueueConfig);
            }
            this.consumer = this.session.createConsumer(targetQueue);
            this.handler = handler;
            consumer.setMessageHandler(handler);
        } catch (ActiveMQException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            session.start();
        } catch (ActiveMQException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (!session.isClosed()) {
            try {
                session.stop();
            } catch (ActiveMQException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
