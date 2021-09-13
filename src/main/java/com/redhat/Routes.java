package com.redhat;

//import com.redhat.dto.Customer;
//import com.redhat.dto.CustomerSuccess;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;

@Component
public class Routes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    restConfiguration()
      .component("netty-http")
      .port("8080")
      .bindingMode(RestBindingMode.auto);
    
    String erpUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl";
    String script = "582";
    String deploy = "1";

    rest()
      .path("/").consumes("application/json").produces("application/json")
        .put("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer");
        .get("/order")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer");
    
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
      .setHeader("backend", simple("{{redhat.backend}}"))
      .setHeader(Exchange.HTTP_QUERY, expression("script=${script}"))
      .setHeader(Exchange.HTTP_QUERY, expression("deploy=${deploy}"))
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          String authHeader = OAuthSign.getAuthHeader(erpUri);
            exchange.getMessage().setHeader("Authorization", authHeader);
        }
      })
      .to("log:DEBUG?showBody=true&showHeaders=false")
      .toD("http://${header.backend}?bridgeEndpoint=true&throwExceptionOnFailure=false")
      .to("log:DEBUG?showBody=true&showHeaders=false");
      
//      .choice()
//        .when(simple("${header.CamelHttpResponseCode} != 201 && ${header.CamelHttpResponseCode} != 202"))
//          .log("err")
//          .transform(constant("Error"))
//        .otherwise()
//          .log("ok")
//      .endChoice();
  }
}