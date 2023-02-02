package sscce.javalin.async.msg;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;

/**
 * Wrapper for an ActiveMQ session and a message producer
 */
public class ProducerSession {
    public final ClientSession session;
    public final ClientProducer producer;
    public final ClientSessionFactory factory;

    public ProducerSession(ClientSessionFactory factory, String targetQueue) {
        this.factory = factory;
        try {
            this.session = factory.createSession(true, true);
            this.producer = this.session.createProducer(targetQueue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ClientMessage createMessage() {
        return this.session.createMessage(false);
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
