package sscce.javalin.async;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.MessageHandler;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.jetbrains.annotations.NotNull;
import sscce.javalin.async.msg.ConsumerSession;
import sscce.javalin.async.msg.ProducerSession;
import sscce.javalin.async.msg.JsonUtil;
import sscce.javalin.async.msg.Request;
import sscce.javalin.async.msg.Response;
import sscce.javalin.async.util.FakeWork;
import java.io.IOException;

public class DispatchedWorker implements MessageHandler {
    public static final String INCOMING_QUEUE = "worker.incoming";

    // MONDAY QUITTING TIME - I WAS STARTING TO BUILD THIS TO HANDLE THE 'WORK' FROM A REQUEST MESSAGE
    private final ServerLocator locator;
    private final ClientSessionFactory factory;
    // ThreadLocal<ProducerSession> prodSession;
    // ThreadLocal<ConsumerSession> consumeSession;
    ProducerSession prodSession;
    ConsumerSession consumeSession;

    public DispatchedWorker() {
        locator = ActiveMQClient.createServerLocator(false,
            new TransportConfiguration("org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory")
        );

        try {
            factory = locator.createSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        prodSession = new ProducerSession(factory, App.RESPONSE_QUEUE);
        consumeSession = new ConsumerSession(factory, INCOMING_QUEUE, this);
        prodSession.start();
        consumeSession.start();
    }


    private void dispatchResponse(@NotNull Response resEvent) {
        var msg = prodSession.createMessage();
        msg.setAddress(App.RESPONSE_QUEUE);
        String json = JsonUtil.toJson(resEvent);
        msg.getBodyBuffer().writeString(json);

        try {
            System.out.println("Dispatching response message with id " + resEvent.id + "\n");
            prodSession.producer.send(msg);
            System.out.println("SENT! Message with id=" + resEvent.id + "\n");
        } catch (ActiveMQException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method called by our Consumer
     * @param message a message sent to our incoming queue for handling RequestEvents.
     */
    @Override
    public void onMessage(ClientMessage message) {
        // System.out.println("\nDispatchedWorker received message into " + INCOMING_QUEUE);
        var requestEventJson = message.getBodyBuffer().readString();
        try {
            Request re = JsonUtil.toRequest(requestEventJson);
            String requestId = re.id;
            // System.out.println("Doing work for request id=" + requestId);
            var randomString = FakeWork.work(20000);
            var response = new Response(requestId, randomString);
            // System.out.println("Dispatching outbound response message with id=" + requestId + ", content=" + randomString);
            dispatchResponse(response);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            if (!prodSession.session.isClosed()) prodSession.session.close();
            if (!prodSession.producer.isClosed()) prodSession.producer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
