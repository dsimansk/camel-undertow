package org.apache.camel.component.undertow;

import io.undertow.client.ClientExchange;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.util.Map;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public interface UndertowHttpBinding {

    Message toCamelMessage(HttpServerExchange httpExchange, Exchange exchange) throws Exception;

    Message toCamelMessage(ClientExchange clientExchange, Exchange exchange) throws Exception;

    void populateCamelHeaders(HttpServerExchange httpExchange, Map<String, Object> headerMap, Exchange exchange) throws Exception;

    void populateCamelHeaders(ClientResponse response, Map<String, Object> headerMap, Exchange exchange) throws Exception;

    Object toHttpResponse(HttpServerExchange httpExchange, Message message);

    Object toHttpRequest(ClientRequest clientRequest, Message message);



}
