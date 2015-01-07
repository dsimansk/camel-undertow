package org.apache.camel.component.undertow.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * Custom handler to inform client that no matching path was found
 *
 * @author David Simansky | dsimansk@redhat.com
 */
public class NotFoundHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.setResponseCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("No matching path found");
    }
}
