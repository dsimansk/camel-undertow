package org.apache.camel.component.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import org.apache.camel.Processor;
import org.apache.camel.component.undertow.handlers.HttpCamelHandler;
import org.apache.camel.component.undertow.handlers.NotFoundHandler;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;

/**
 * The Undertow consumer.
 *
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowConsumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowConsumer.class);

    public UndertowConsumer(UndertowEndpoint endpoint, Processor processor) {
        super(endpoint, processor);

    }

    @Override
    public UndertowEndpoint getEndpoint() {
        return (UndertowEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        LOG.debug("Undertow consumer is starting");
        getEndpoint().getComponent().registerConsumer(this);
        getEndpoint().getComponent().startServer(this);
    }

    @Override
    protected void doStop() {
        LOG.debug("Undertow consumer is stopping");
        getEndpoint().getComponent().unregisterConsumer(this);
    }


}
