package com.redhat;

//import com.redhat.dto.Customer;
//import com.redhat.dto.CustomerSuccess;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import java.util.Map;

@Component
public class Routes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    restConfiguration()
      .component("netty-http")
      .port("8080")
      .bindingMode(RestBindingMode.auto);
    
    String erpUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=582&deploy=1";

    rest()
      .path("/").consumes("application/json").produces("application/json")
        .put("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer")
        .get("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:get-customer");
    
    from("direct:post-customer")
      .setHeader("HTTP_METHOD", constant("POST"))
      .to("direct:request");
    from("direct:put-customer")
      .setHeader("HTTP_METHOD", constant("PUT"))
      .to("direct:request");
    from("direct:get-customer")
      .setHeader("HTTP_METHOD", constant("GET"))
      .to("direct:request");

    from("direct:request")
      .removeHeader(Exchange.HTTP_URI)
      .setHeader("backend", simple("{{redhat.backend}}"))
      .setHeader("script", simple("582"))
      .setHeader("deploy", simple("1"))
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          String authHeader = OAuthSign.getAuthHeader(erpUri);
            exchange.getMessage().setHeader("Authorization", authHeader);
        }
      })
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          Message exchangeIn = exchange.getIn();
          Map<String, Object> headers = exchangeIn.getHeaders();
          headers.put("accept", "*/*");
          headers.put(Exchange.HTTP_QUERY,  "bridgeEndpoint=true");
          headers.put(Exchange.HTTP_QUERY,  "throwExceptionOnFailure=false");
          headers.put(Exchange.HTTP_METHOD, "GET");
          headers.put(Exchange.CONTENT_TYPE, "application/json");
        }
      })
      .to("log:DEBUG?showBody=true&showHeaders=true")
      //.toD("https://${header.backend}&bridgeEndpoint=true&throwExceptionOnFailure=false")
      .toD("https://${header.backend}")
      .to("log:DEBUG?showBody=true&showHeaders=true");
      
//      .choice()
//        .when(simple("${header.CamelHttpResponseCode} != 201 && ${header.CamelHttpResponseCode} != 202"))
//          .log("err")
//          .transform(constant("Error"))
//        .otherwise()
//          .log("ok")
//      .endChoice();
  }
}