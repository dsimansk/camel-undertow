package org.apache.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowMethodRestricTest extends CamelTestSupport {


    private String url = "http://localhost:8888/methodRestrict";

    @Test
    public void testProperHttpMethod() throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod httpPost = new PostMethod(url);

        StringRequestEntity reqEntity = new StringRequestEntity("This is a test", null, null);
        httpPost.setRequestEntity(reqEntity);

        int status = httpClient.executeMethod(httpPost);

        assertEquals(200, status);

        String result = httpPost.getResponseBodyAsString();
        assertEquals("This is a test response", result);
    }

    @Test
    public void testImproperHttpMethod() throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod httpGet = new GetMethod(url);
        int status = httpClient.executeMethod(httpGet);

        assertEquals("Get a wrong response status", 405, status);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("undertow://http://localhost:8888/methodRestrict?httpMethodRestrict=POST").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Message in = exchange.getIn();
                        String request = in.getBody(String.class);
                        exchange.getOut().setBody(request + " response");
                    }
                });
            }
        };
    }
}
