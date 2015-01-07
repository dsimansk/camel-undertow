package org.apache.camel.component;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowPrefixMatchingTest extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(UndertowComponentTest.class);

    @Test
    public void passOnExactPath() throws Exception {
        Exchange response = template.requestBody("http://localhost:8888/myapp/suffix", "Hello Camel!", Exchange.class);
        getMockEndpoint("mock:myapp").expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 200);
    }

    @Test
    public void failsOnPrefixPath() throws Exception {

        try {
            String response = template.requestBody("http://localhost:8888/myapp", "Hello Camel!", String.class);
            fail("Should fail, something is wrong");
        } catch (CamelExecutionException ex) {
            HttpOperationFailedException cause = assertIsInstanceOf(HttpOperationFailedException.class, ex.getCause());
            assertEquals(404, cause.getStatusCode());
        }
    }

    @Test
    public void passOnPrefixPath() throws Exception {
        Exchange response = template.requestBody("http://localhost:8888/bar/somethingNotImportant", "Hello Camel!", Exchange.class);
        getMockEndpoint("mock:myapp").expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 200);
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("undertow:http://localhost:8888/myapp/suffix?matchOnUriPrefix=false")
                        .transform(bodyAs(String.class).append(" Must match exact path"))
                        .to("mock:myapp");

                from("undertow:http://localhost:8888/bar")
                        .transform(bodyAs(String.class).append(" Matching prefix"))
                        .to("mock:bar");


            }
        };
    }

}
