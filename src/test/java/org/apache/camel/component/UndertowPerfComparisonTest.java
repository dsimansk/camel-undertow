package org.apache.camel.component;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowPerfComparisonTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(UndertowPerfComparisonTest.class);

    private static long undertowDuration;
    private static long jettyDuration;
    private static long nettyHttpDuration;


    @AfterClass
    public static void publish() {
        LOG.info("*************** PERFORMANCE TEST ***************");
        LOG.info("Execution duration of Undertow in milisec: " +undertowDuration);
        LOG.info("Execution duration of Jetty in milisec: " +jettyDuration);
        LOG.info("Execution duration of Netty-HTTP in milisec: " +nettyHttpDuration);
        LOG.info("*************** PERFORMANCE TEST ***************");
    }


    @Test
    public void testUndertow() throws Exception {

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            String response = template.requestBody("http://localhost:8888/myapp", "Hello Camel!", String.class);

            assertNotNull(response);

            assertEquals("Hello Camel! Bye Camel!", response);

            MockEndpoint mockEndpoint = getMockEndpoint("mock:myapp");
            LOG.debug("Number of exchanges in mock:myapp" + mockEndpoint.getExchanges().size());

        }

        long finishTime = System.currentTimeMillis();

        long duration = (finishTime - startTime);
        undertowDuration = duration;
        LOG.info("Execution duration of Undertow in milisec: " +duration);

    }

    @Test
    public void testJetty() throws Exception {

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            String response = template.requestBody("http://localhost:8889/myapp", "Hello Camel!", String.class);

            assertNotNull(response);

            assertEquals("Hello Camel! Bye Camel!", response);

            MockEndpoint mockEndpoint = getMockEndpoint("mock:myapp");
            LOG.debug("Number of exchanges in mock:myapp" + mockEndpoint.getExchanges().size());

        }

        long finishTime = System.currentTimeMillis();

        long duration = (finishTime - startTime);
        jettyDuration = duration;
        LOG.info("Execution duration of Jetty in milisec: " +duration);

    }

    @Test
    public void testNettyHttp() throws Exception {

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            String response = template.requestBody("http://localhost:8887/myapp", "Hello Camel!", String.class);

            assertNotNull(response);

            assertEquals("Hello Camel! Bye Camel!", response);

            MockEndpoint mockEndpoint = getMockEndpoint("mock:myapp");
            LOG.debug("Number of exchanges in mock:myapp" + mockEndpoint.getExchanges().size());

        }

        long finishTime = System.currentTimeMillis();

        long duration = (finishTime - startTime);
        nettyHttpDuration = duration;
        LOG.info("Execution duration of Netty-HTTP in milisec: " +duration);

    }



    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("undertow:http://localhost:8888/myapp")
                        .transform(bodyAs(String.class).append(" Bye Camel!"))
                        .to("mock:myapp");

                from("jetty:http://localhost:8889/myapp")
                        .transform(bodyAs(String.class).append(" Bye Camel!"))
                        .to("mock:bar");

                from("netty-http:http://localhost:8887/myapp")
                        .transform(bodyAs(String.class).append(" Bye Camel!"))
                        .to("mock:bar");


            }
        };
    }


}
