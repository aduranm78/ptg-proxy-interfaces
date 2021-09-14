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
    
    String erpUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=580&deploy=2&bridgeEndpoint=true&throwExceptionOnFailure=false";
    String NSUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl";
    String  method ="POST";

    rest()
      .path("/").consumes("application/json").produces("application/json")
        .put("/get-lead")
        //.type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/get-lead")
        //.type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer")
        .get("/get-lead")
        //.type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:get-customer");
    
    from("direct:post-customer")
      .setHeader("HTTP_METHOD", constant("POST"))
      .to("direct:request");
      method="POST";
    from("direct:put-customer")
      .setHeader("HTTP_METHOD", constant("PUT"))
      .to("direct:request");
      method="POST";
    from("direct:get-customer")
      .setHeader("HTTP_METHOD", constant("GET"))
      .to("direct:request");
      method="GET";

    from("direct:request")
      /*.removeHeader(Exchange.HTTP_URI)
      .setHeader("backend", simple("{{redhat.backend}}"))
      .setHeader("script", simple("582"))
      .setHeader("deploy", simple("1"))*/
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          String authHeader = OAuthSign.getAuthHeader(erpUri,"GET"); 
          exchange.getMessage().setHeader("Authorization", authHeader);
          exchange.getMessage().setHeader(Exchange.HTTP_QUERY, "script=580&deploy=2&bridgeEndpoint=true&throwExceptionOnFailure=false");
    	    exchange.getMessage().setHeader(Exchange.HTTP_URI, NSUri);
        }
      })
      .to("log:DEBUG?showBody=true&showHeaders=true")
      //.toD("https://${header.backend}&bridgeEndpoint=true&throwExceptionOnFailure=false")
      .toD("https://NSUri")
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