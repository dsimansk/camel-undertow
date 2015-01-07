package org.apache.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowHeaderTest extends CamelTestSupport {

    @Test
    public void testHttpHeaders() throws Exception {
        getMockEndpoint("mock:input").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:input").expectedHeaderReceived("param", "true");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_METHOD, "POST");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_URL, "http://localhost:8888/headers");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_URI, "/headers");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_QUERY, "param=true");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_PATH, "/headers");

        String out = template.requestBody("http://localhost:8888/headers?param=true", "Hello World", String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("undertow:http://localhost:8888/headers")
                        .to("mock:input")
                        .transform().constant("Bye World");
            }
        };
    }

}
