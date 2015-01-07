package org.apache.camel.component;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowError500Test extends CamelTestSupport {

    @Test
    public void testHttp500Error() throws Exception {
        getMockEndpoint("mock:input").expectedBodiesReceived("Hello World");

        try {
            template.requestBody("http://localhost:8888/foo", "Hello World", String.class);
            fail("Should have failed");
        } catch (CamelExecutionException e) {
//            NettyHttpOperationFailedException cause = assertIsInstanceOf(NettyHttpOperationFailedException.class, e.getCause());
//            assertEquals(500, cause.getStatusCode());
//            assertEquals("Camel cannot do this", context.getTypeConverter().convertTo(String.class, cause.getResponse().getContent()));
        }

        assertMockEndpointsSatisfied();
    }





    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("undertow:http://localhost:8888/foo")
                        .to("mock:input")
                                // trigger failure by setting error code to 500
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                        .setBody().constant("Camel cannot do this");
            }
        };
    }
}
