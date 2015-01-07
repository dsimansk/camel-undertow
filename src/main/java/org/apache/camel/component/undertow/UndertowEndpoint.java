package org.apache.camel.component.undertow;

import io.undertow.client.UndertowClient;
import io.undertow.server.HttpServerExchange;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategyAware;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a Undertow endpoint.
 *
 * @author David Simansky | dsimansk@redhat.com
 */
@UriEndpoint(scheme = "undertow", consumerClass = UndertowConsumer.class)
public class UndertowEndpoint extends DefaultEndpoint implements HeaderFilterStrategyAware {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowEndpoint.class);

    private URI httpURI;
    private UndertowHttpBinding undertowHttpBinding;
    private UndertowComponent component;

    @UriParam
    private String httpMethodRestrict;
    @UriParam
    private Boolean matchOnUriPrefix = true;
    private HeaderFilterStrategy headerFilterStrategy;
    private SSLContext sslContext;
    @UriParam
    private Boolean throwExceptionOnFailure;
    @UriParam
    private Boolean transferException;

    public UndertowEndpoint(String uri, UndertowComponent component, URI httpURI) throws URISyntaxException {
        super(uri, component);
        this.component = component;
        this.httpURI = httpURI;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new UndertowProducer(this);
    }

    public Exchange createExchange(HttpServerExchange httpExchange) throws Exception {
        Exchange exchange = createExchange();

        Message in = getUndertowHttpBinding().toCamelMessage(httpExchange, exchange);

        exchange.setProperty(Exchange.CHARSET_NAME, httpExchange.getRequestCharset());
        in.setHeader(Exchange.HTTP_CHARACTER_ENCODING, httpExchange.getRequestCharset());

        exchange.setIn(in);
        return exchange;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new UndertowConsumer(this, processor);
    }

    @Override
    public PollingConsumer createPollingConsumer() throws Exception {
        //throw exception as polling consumer is not supported
        throw new UnsupportedOperationException("This component does not support polling consumer");
    }

    public boolean isSingleton() {
        return true;
    }

    public URI getHttpURI() {
        return httpURI;
    }

    public void setHttpURI(URI httpURI) {
        this.httpURI = httpURI;
    }


    public String getHttpMethodRestrict() {
        return httpMethodRestrict;
    }

    public void setHttpMethodRestrict(String httpMethodRestrict) {
        this.httpMethodRestrict = httpMethodRestrict;
    }

    public Boolean getMatchOnUriPrefix() {
        return matchOnUriPrefix;
    }

    public void setMatchOnUriPrefix(Boolean matchOnUriPrefix) {
        this.matchOnUriPrefix = matchOnUriPrefix;
    }

    public HeaderFilterStrategy getHeaderFilterStrategy() {
        return headerFilterStrategy;
    }

    public void setHeaderFilterStrategy(HeaderFilterStrategy headerFilterStrategy) {
        this.headerFilterStrategy = headerFilterStrategy;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public Boolean getThrowExceptionOnFailure() {
        return throwExceptionOnFailure;
    }

    public void setThrowExceptionOnFailure(Boolean throwExceptionOnFailure) {
        this.throwExceptionOnFailure = throwExceptionOnFailure;
    }

    public Boolean getTransferException() {
        return transferException;
    }

    public void setTransferException(Boolean transferException) {
        this.transferException = transferException;
    }

    public UndertowHttpBinding getUndertowHttpBinding() {
        return undertowHttpBinding;
    }

    public void setUndertowHttpBinding(UndertowHttpBinding undertowHttpBinding) {
        this.undertowHttpBinding = undertowHttpBinding;
    }

    @Override
    public UndertowComponent getComponent() {
        return component;
    }

    public void setComponent(UndertowComponent component) {
        this.component = component;
    }
}
