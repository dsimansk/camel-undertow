package org.apache.camel.component.undertow;

import io.undertow.client.*;
import io.undertow.util.Headers;
import io.undertow.util.Protocols;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Undertow producer.
 * <p/>
 * The implementation of Producer is considered as experimental. The Undertow client classes are not thread safe,
 * their purpose is for the reverse proxy usage inside Undertow itself. This may change in the future versions and
 * general purpose HTTP client wrapper will be added. Therefore this Producer may be changed too.
 *
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(UndertowProducer.class);
    private UndertowEndpoint endpoint;

    public UndertowProducer(UndertowEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public UndertowEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(UndertowEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("Producer endpoint uri " + endpoint.getHttpURI());

        final UndertowClient client = UndertowClient.getInstance();
        XnioWorker worker = Xnio.getInstance().createWorker(OptionMap.EMPTY);
        IoFuture<ClientConnection> connect = client.connect(endpoint.getHttpURI(), worker, new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, 8192, 8192 * 8192), OptionMap.EMPTY);

        ClientRequest request = new ClientRequest();
        request.setProtocol(Protocols.HTTP_1_1);

        Object body = getRequestBody(request, exchange);


        TypeConverter tc = endpoint.getCamelContext().getTypeConverter();
        ByteBuffer bodyAsByte = tc.convertTo(ByteBuffer.class, body);

        if (body != null) {
            request.getRequestHeaders().put(Headers.CONTENT_LENGTH, bodyAsByte.array().length);
        }

        connect.get().sendRequest(request, new UndertowProducerCallback(bodyAsByte, exchange));

    }

    private Object getRequestBody(ClientRequest request, Exchange camelExchange) {
        Object result;
        result = endpoint.getUndertowHttpBinding().toHttpRequest(request, camelExchange.getIn());
        return result;
    }

    /**
     * Everything important happens in callback
     */
    private class UndertowProducerCallback implements ClientCallback<ClientExchange> {

        private ByteBuffer body;
        private Exchange camelExchange;

        public UndertowProducerCallback(ByteBuffer body, Exchange camelExchange) {
            this.body = body;
            this.camelExchange = camelExchange;
        }

        @Override
        public void completed(ClientExchange clientExchange) {
            clientExchange.setResponseListener(new ClientCallback<ClientExchange>() {
                @Override
                public void completed(ClientExchange clientExchange) {
                    Message message = null;
                    try {
                        message = endpoint.getUndertowHttpBinding().toCamelMessage(clientExchange, camelExchange);
                    } catch (Exception e) {
                        camelExchange.setException(e);
                    }
                    if (ExchangeHelper.isOutCapable(camelExchange)) {
                        camelExchange.setOut(message);
                    } else {
                        camelExchange.setIn(message);
                    }

                }
              @Override
                public void failed(IOException e) {
                    camelExchange.setException(e);

                }
            });
            try {
                //send body if exists
                if (body != null) {
                    clientExchange.getRequestChannel().write(body);
                }
            } catch (IOException e) {
                LOG.error("Failed with: " + e.getMessage());
                camelExchange.setException(e);
            }

        }

        @Override
        public void failed(IOException e) {
            LOG.error("Failed with: " + e.getMessage());
            camelExchange.setException(e);
        }
    }


}
