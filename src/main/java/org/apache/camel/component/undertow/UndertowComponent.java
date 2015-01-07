package org.apache.camel.component.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.undertow.handlers.HttpCamelHandler;
import org.apache.camel.component.undertow.handlers.NotFoundHandler;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestConsumerFactory;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the component that manages {@link UndertowEndpoint}.
 */
public class UndertowComponent extends HttpComponent implements RestConsumerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowEndpoint.class);

    private UndertowHttpBinding undertowHttpBinding;

    private Map<Integer, UndertowRegistry> serversRegistry = new HashMap<Integer, UndertowRegistry>();

    public UndertowComponent() {
        this.undertowHttpBinding = new DefaultUndertowHttpBinding();
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        //extract parameters from URI
        Boolean matchOnUriPrefix = getAndRemoveParameter(parameters, "matchOnUriPrefix", Boolean.class);
        HeaderFilterStrategy headerFilterStrategy = resolveAndRemoveReferenceParameter(parameters, "headerFilterStrategy", HeaderFilterStrategy.class);
        SSLContextParameters sslContextParameters = resolveAndRemoveReferenceParameter(parameters, "sslContextParametersRef", SSLContextParameters.class);
        Boolean throwExceptionOnFailure = getAndRemoveParameter(parameters, "throwExceptionOnFailure", Boolean.class);
        Boolean transferException = getAndRemoveParameter(parameters, "transferException", Boolean.class);

        String httpMethodRestrict = getAndRemoveParameter(parameters, "httpMethodRestrict", String.class);

        System.out.println("Remaining: " + remaining);
        String address = remaining;
        URI httpUri = new URI(UnsafeUriCharactersEncoder.encodeHttpURI(address));
        URI endpointUri = URISupport.createRemainingURI(httpUri, parameters);

        UndertowEndpoint endpoint = new UndertowEndpoint(endpointUri.toString(), this, httpUri);

        if (endpoint.getUndertowHttpBinding() == null) {
            endpoint.setUndertowHttpBinding(undertowHttpBinding);
        }

        //set parameters if they exists in URI
        if (httpMethodRestrict != null) {
            endpoint.setHttpMethodRestrict(httpMethodRestrict);
        }
        if (matchOnUriPrefix != null) {
            endpoint.setMatchOnUriPrefix(matchOnUriPrefix);
        }
        if (headerFilterStrategy != null) {
            endpoint.setHeaderFilterStrategy(headerFilterStrategy);
        }
        if (sslContextParameters != null) {
            SSLContext sslContext = sslContextParameters.createSSLContext();
            endpoint.setSslContext(sslContext);
        }
        if (throwExceptionOnFailure != null) {
            endpoint.setThrowExceptionOnFailure(throwExceptionOnFailure);
        }
        if (transferException != null) {
            endpoint.setTransferException(transferException);
        }

        setProperties(endpoint, parameters);
        return endpoint;
    }

    @Override
    public Consumer createConsumer(CamelContext camelContext, Processor processor, String verb, String basePath, String uriTemplate,
                                   String consumes, String produces, Map<String, Object> parameters) throws Exception {
        String path = basePath;
        if (uriTemplate != null) {
            // make sure to avoid double slashes
            if (uriTemplate.startsWith("/")) {
                path = path + uriTemplate;
            } else {
                path = path + "/" + uriTemplate;
            }
        }
        path = FileUtil.stripLeadingSeparator(path);
        String scheme = "http";
        String host = "";
        int port = 0;
        RestConfiguration config = getCamelContext().getRestConfiguration();
        if (config.getComponent() == null || config.getComponent().equals("undertow")) {
            if (config.getScheme() != null) {
                scheme = config.getScheme();
            }
            if (config.getHost() != null) {
                host = config.getHost();
            }
            int num = config.getPort();
            if (num > 0) {
                port = num;
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        // build query string, and append any endpoint configuration properties
        if (config != null && (config.getComponent() == null || config.getComponent().equals("undertow"))) {
            // setup endpoint options
            if (config.getEndpointProperties() != null && !config.getEndpointProperties().isEmpty()) {
                map.putAll(config.getEndpointProperties());
            }
        }

        String query = URISupport.createQueryString(map);

        String url = "undertow:%s://%s:%s/%s";
        url = String.format(url, scheme, host, port, path);
        if (!query.isEmpty()) {
            url = url + "&" + query;
        }

        UndertowEndpoint endpoint = camelContext.getEndpoint(url, UndertowEndpoint.class);
        setProperties(endpoint, parameters);

        Consumer consumer = endpoint.createConsumer(processor);

        return consumer;

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    protected Undertow rebuildServer(UndertowRegistry registy) {
        Undertow.Builder result = Undertow.builder();
        int port = registy.getPort();
        if (registy.getSslContext() != null) {
            result = result.addHttpsListener(registy.getPort(), registy.getHost(), registy.getSslContext());
        } else {
            result = result.addHttpListener(registy.getPort(), registy.getHost());
        }
        PathHandler path = Handlers.path(new NotFoundHandler());
        for (URI key : registy.getConsumersRegistry().keySet()) {
            UndertowConsumer consumer = registy.getConsumersRegistry().get(key);
            URI httpUri = consumer.getEndpoint().getHttpURI();
            if (consumer.getEndpoint().getMatchOnUriPrefix()) {
                path.addPrefixPath(httpUri.getPath(), new HttpCamelHandler(consumer));

            } else {
                path.addExactPath(httpUri.getPath(), new HttpCamelHandler(consumer));

            }
            LOG.debug("::Rebuild for path: {}", httpUri.getPath());
        }
        result = result.setHandler(path);
        return result.build();
    }

    public void registerConsumer(UndertowConsumer consumer) {
        int port = consumer.getEndpoint().getHttpURI().getPort();
        if (serversRegistry.containsKey(port)) {
            //server listens on port, we need add configuration for path
            UndertowRegistry undertowRegistry = serversRegistry.get(port);
            undertowRegistry.registerConsumer(consumer);
        } else {
            //create new server to listen on specified port
            serversRegistry.put(port, new UndertowRegistry(consumer, port));
        }
    }

    public void unregisterConsumer(UndertowConsumer consumer) {
        int port = consumer.getEndpoint().getHttpURI().getPort();
        if (serversRegistry.containsKey(port)) {
            serversRegistry.get(port).unregisterConsumer(consumer);
        }
        if (serversRegistry.get(port).isEmpty()) {
            //if there no Consumer left, we can shut down server
            serversRegistry.get(port).getServer().stop();
            serversRegistry.remove(port);
        } else {
            //call startServer to rebuild otherwise
            startServer(consumer);
        }
    }

    public void startServer(UndertowConsumer consumer) {
        int port = consumer.getEndpoint().getHttpURI().getPort();
        LOG.info("Starting server on port: {}",port);
        UndertowRegistry undertowRegistry = serversRegistry.get(port);

        if (undertowRegistry.getServer() != null) {
            //server is running, we need to stop it first and then rebuild
            undertowRegistry.getServer().stop();
        }
        Undertow newServer = rebuildServer(undertowRegistry);
        newServer.start();
        undertowRegistry.setServer(newServer);
    }

}
