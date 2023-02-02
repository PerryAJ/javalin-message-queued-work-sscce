package sscce.javalin.async;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.jupiter.api.Test;
import sscce.javalin.async.msg.JsonUtil;
import sscce.javalin.async.msg.MessageBroker;
import sscce.javalin.async.msg.Request;

public class DispatchedWorkerTest {

    @Test
    public void dispatchedWorkTimingTest() {
        // create the embedded message broker
        try (MessageBroker broker = new MessageBroker()) {
            // create a 'ServerLocator' that can resolve the broker via the in-memory connector
            try (ServerLocator locator = ActiveMQClient.createServerLocator(
                false,
                new TransportConfiguration("org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory")
            )) {
                // build
                ClientSessionFactory factory = locator.createSessionFactory();
                try (ClientSession session = factory.createSession(true, true)) {
                    // create the queue that handles 'responses' from the remote worker
                    if (!session.queueQuery(SimpleString.toSimpleString(App.RESPONSE_QUEUE)).isExists()) {
                        var incomingQueueConfig = new QueueConfiguration(App.RESPONSE_QUEUE)
                            .setRoutingType(RoutingType.ANYCAST)
                            .setAddress(DispatchedWorker.INCOMING_QUEUE);
                        session.createQueue(incomingQueueConfig);
                    }

                    ClientProducer requestProxyProducer = session.createProducer(DispatchedWorker.INCOMING_QUEUE);
                    ClientConsumer resProxyConsumer = session.createConsumer(App.RESPONSE_QUEUE);
                    DispatchedWorker worker = new DispatchedWorker();

                    var req = new Request("124");
                    var json = JsonUtil.toJson(req);
                    var msg = session.createMessage(false);
                    msg.setAddress(DispatchedWorker.INCOMING_QUEUE);
                    msg.getBodyBuffer().writeString(json);

                    var start = System.currentTimeMillis();

                    resProxyConsumer.setMessageHandler(message -> {
                        try {
                            var msgBody = message.getBodyBuffer().readString();
                            var res = JsonUtil.toResponse(msgBody);
                            var finish = System.currentTimeMillis() - start;
                            System.out.println("Dispatched work for req id=" + res.id + " completed in " + finish + "ms");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    session.start();
                    requestProxyProducer.send(msg);
                    session.commit();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
