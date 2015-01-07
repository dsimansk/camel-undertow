package org.apache.camel.component.undertow;

import io.undertow.Undertow;
import org.apache.camel.RuntimeCamelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowRegistry.class);

    int port;
    SSLContext sslContext;
    String host;
    Undertow server;
    Map<URI, UndertowConsumer> consumersRegistry = new HashMap<URI, UndertowConsumer>();

    public UndertowRegistry(UndertowConsumer consumer, int port) {
        registerConsumer(consumer);
        this.port = port;
        if (consumer.getEndpoint().getSslContext() != null) {
            sslContext = consumer.getEndpoint().getSslContext();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void registerConsumer(UndertowConsumer consumer) {
        URI httpUri = consumer.getEndpoint().getHttpURI();
        if (host != null && !host.equals(httpUri.getHost())) {
            throw new RuntimeCamelException("Can't register UndertowConsumer on different host and same port: {}" +host+" "+httpUri.getHost());
        } else {
            host = httpUri.getHost();
        }
        LOG.info("Adding consumer to consumerRegistry: {}", httpUri);
        consumersRegistry.put(httpUri, consumer);
        if (sslContext != null && consumer.getEndpoint().getSslContext() != null) {
            throw new RuntimeCamelException("Can't register UndertowConsumer with different SSL config");
        }

    }

    public void unregisterConsumer(UndertowConsumer consumer) {
        URI httpUri = consumer.getEndpoint().getHttpURI();
        if (consumersRegistry.containsKey(httpUri)) {
            consumersRegistry.remove(httpUri);
        } else {
            throw new RuntimeCamelException("This consumer is not registered");
        }
    }

    public boolean isEmpty(){
        return consumersRegistry.isEmpty();
    }

    public Undertow getServer() {
        return server;
    }

    public void setServer(Undertow server) {
        this.server = server;
    }

    public Map<URI, UndertowConsumer> getConsumersRegistry() {
        return consumersRegistry;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
}
