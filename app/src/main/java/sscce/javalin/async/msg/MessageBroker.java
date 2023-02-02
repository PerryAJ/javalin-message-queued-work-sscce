package sscce.javalin.async.msg;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl;

/**
 * Simple embedded ActiveMQ Artemis message broker
 */
public class MessageBroker implements AutoCloseable {
    public static final String LOCAL_CONNECTION_ADDRESS = "vm://0";

    private final ActiveMQServerImpl messageServer;
    private final ServerLocator locator;

    public MessageBroker() {
        ConfigurationImpl config = null;
        try {
            config = new ConfigurationImpl()
                .setPersistenceEnabled(false)
                .setJournalDirectory("build/data/journal")
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("in-vm", LOCAL_CONNECTION_ADDRESS);


        messageServer = (ActiveMQServerImpl) ActiveMQServers.newActiveMQServer(config);
        locator = ActiveMQClient.createServerLocator(false, new TransportConfiguration(
            InVMConnectorFactory.class.getName()));
        messageServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (locator != null && !locator.isClosed()) locator.close();
        if (messageServer != null) messageServer.stop();
    }
}
