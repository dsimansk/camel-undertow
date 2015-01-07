package org.apache.camel.component;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author David Simansky | dsimansk@redhat.com
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/SpringTest.xml"})
public class UndertowHttpsSpringTest {


    @Produce
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:input")
    private MockEndpoint mockEndpoint;


    @BeforeClass
    public static void setUpJaas() throws Exception {
        URL trustStoreUrl = UndertowHttpsSpringTest.class.getClassLoader().getResource("ssl/keystore.jks");
        System.setProperty("javax.net.ssl.trustStore", trustStoreUrl.toURI().getPath());
    }

    @AfterClass
    public static void tearDownJaas() throws Exception {
        System.clearProperty("java.security.auth.login.config");
    }

    @Test
    public void testSSLInOutWithNettyConsumer() throws Exception {
        mockEndpoint.expectedBodiesReceived("Hello World");

        String out = template.requestBody("https://localhost:9000/spring", "Hello World", String.class);
        assertEquals("Bye World", out);

        mockEndpoint.assertIsSatisfied();
    }

}

