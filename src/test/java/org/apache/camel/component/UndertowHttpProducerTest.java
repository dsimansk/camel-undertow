package org.apache.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowHttpProducerTest extends CamelTestSupport {

    @Test
    public void testHttpSimple() throws Exception {
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_METHOD, "GET");

        String out = template.requestBody("http://localhost:8888/foo", null, String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testHttpSimpleHeader() throws Exception {
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_METHOD, "POST");

        String out = template.requestBodyAndHeader("http://localhost:8888/foo", null, Exchange.HTTP_METHOD, "POST", String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testHttpSimpleHeaderAndBody() throws Exception {

        getMockEndpoint("mock:input").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:input").expectedHeaderReceived(Exchange.HTTP_METHOD, "POST");

        String out = template.requestBodyAndHeader("http://localhost:8888/foo", "Hello World", Exchange.HTTP_METHOD, "POST", String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("undertow:http://localhost:8888/foo")
                        .to("mock:input")
                        .transform().constant("Bye World");
            }
        };
    }
}
