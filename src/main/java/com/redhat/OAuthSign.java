package com.example;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpPost;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.signature.HmacSha256MessageSigner;

public class OAuthSign {
	public static String getAuthHeader(String uri) throws IOException {           
	    String consumer_key = "5c179380b0b2955dd1b379e9ce4a8f8f1fadad6d9bb2ae4bda878f468579eba2";
		String consumer_secret = "c95a5926e605b297c5fccdaa66fe34b48de30e39b209d56a29fe85b7a5a08ca6";
		String access_token = "b6bbb75dfc283fadd57ca34f22359b73939b9bd8089e5a905f6384c292945785";
		String access_secret= "6f86d81244f26659d3dffa38d131d0271b8617577258fb002bf36129e1d850b2";

	    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
	    consumer.setMessageSigner(new HmacSha256MessageSigner());
	    consumer.setTokenWithSecret(access_token, access_secret);
	    
	    HttpPost httppost= new HttpPost(uri);
	    
	    try {
	        HttpRequest signedReq = consumer.sign(httppost);
	        String realm = "OAuth realm=\"5298967_SB1\",";
	        return signedReq.getHeader("Authorization").toString().replace("OAuth", realm);
	    } catch (OAuthMessageSignerException ex) {
	        Logger.getLogger(HttpPost.class.getName()).log(Level.SEVERE, null, ex);
	        return ex.getMessage();
	    } catch (OAuthExpectationFailedException ex) {
	        Logger.getLogger(HttpPost.class.getName()).log(Level.SEVERE, null, ex);
	        return ex.getMessage();
	    } catch (OAuthCommunicationException ex) {
	        Logger.getLogger(HttpPost.class.getName()).log(Level.SEVERE, null, ex);
	        return ex.getMessage();
	    }
	    
	    // HttpParameters httpParams = consumer.getRequestParameters();
	    // Set<String> paramKeys = httpParams.keySet();
	    
	    // for (String k : paramKeys) {
	    // 	System.out.println(httpParams.getAsHeaderElement(k));
	    // }
	    // System.out.println(httpParams.getAsHeaderElement("oauth_signature"));
	}
}
