package org.apache.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowComponentTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(UndertowComponentTest.class);

    @Test
    public void testUndertow() throws Exception {


        String response = template.requestBody("undertow://http://localhost:8888/myapp", "Hello Camel!", String.class);
//        MockEndpoint mockEndpoint = getMockEndpoint("mock:myapp");
//        assertTrue(mockEndpoint.getExchanges().size() == 1);
//        for (Exchange exchange : mockEndpoint.getExchanges()) {
//            assertEquals("Bye Camel", exchange.getIn().getBody(String.class));
//        }

        assertNotNull(response);

        assertEquals("Hello Camel!", response);

        MockEndpoint mockEndpoint = getMockEndpoint("mock:myapp");
        mockEndpoint.expectedHeaderReceived(Exchange.HTTP_METHOD, "GET");
        LOG.debug("Number of exchanges in mock:myapp" +mockEndpoint.getExchanges().size());

        for (Exchange exchange : mockEndpoint.getExchanges()) {
            assertEquals("Hello Camel! Bye Camel!", exchange.getIn().getBody(String.class));
        }

//        Exchange out = template.request("undertow:http://localhost:8888/myapp", new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                exchange.getIn().setBody("Hello World!");
//            }
//        });


        mockEndpoint.assertIsSatisfied();

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("undertow:http://localhost:8888/myapp")
                        .transform().constant("Bye Camel!")
                        .to("mock:myapp");

//                from("undertow:http://localhost:8888/bar")
//                        .transform(bodyAs(String.class).append(" Bar Camel!"))
//                        .to("mock:bar");



            }
        };
    }


}
