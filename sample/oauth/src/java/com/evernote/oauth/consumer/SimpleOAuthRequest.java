/**
 * Copyright 2008 by EverNote Corporation.  All rights reserved.
 */

package com.evernote.oauth.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This is a very simple implementation of an OAuth consumer request which can
 * be used to ask an OAuth service provider for either a Request Token or
 * an Access Token.  It only handles PLAINTEXT authentication, and it only goes
 * over a GET transport.  As a result, it should only be used over SSL.
 * 
 * @author Dave Engberg
 */
public class SimpleOAuthRequest {

  /**
   * Random number generator for creating OAuth nonces
   */
  private static final Random random = new Random();
  
  /**
   * The URL of the OAuth service Provider that we should hit to request a
   * token.
   */
  private String providerUrl;

  /**
   * A mapping containing all of the OAuth parameters that will be passed in
   * the reply.
   */
  private Map<String, String> parameters = new HashMap<String, String>();
  
  /**
   * Constructs a request object that can be used to make token requests from
   * an OAuth provider.
   * 
   * @param providerUrl  the base URL to request a Request or Access token
   * @param consumerKey  the OAuth consumer key, given by the Service Provider
   * @param consumerSecret  the OAuth consumer secret, given by the Provider
   * @param tokenSecret  if non-null, this is the previous oauth_token_secret
   *   that should be used in signing this request.  If null, this will assume
   *   that this message does not include a token secret in its signature
   */
  public SimpleOAuthRequest(String providerUrl, String consumerKey,
      String consumerSecret, String tokenSecret) {
    this.providerUrl = providerUrl;
    setParameter("oauth_consumer_key", consumerKey);
    String signature = consumerSecret + "&";
    if (tokenSecret != null) {
      signature += tokenSecret;
    }
    setParameter("oauth_signature", signature);
    setParameter("oauth_signature_method", "PLAINTEXT");
    setParameter("oauth_timestamp",
        Long.toString(System.currentTimeMillis() / 1000));
    setParameter("oauth_nonce",
        Long.toHexString(random.nextLong()));
    setParameter("oauth_version", "1.0");
  }
  
  /**
   * Sets one of the query string parameters for the request that will be
   * made to the OAuth provider.  The value will be URL encoded before adding
   * to the URL.
   * 
   * @param name  the name of the parameter to be set
   * @param value  the string value, unencoded
   */
  public void setParameter(String name, String value) {
    parameters.put(name, value);
  }
  
  /**
   * Encodes this request as a single URL that can be opened.
   */
  public String encode() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(providerUrl);
    boolean firstParam = providerUrl.indexOf('?') < 0;
    for (Map.Entry<String, String> parameter : parameters.entrySet()) {
      if (firstParam) {
        sb.append('?');
        firstParam = false;
      } else {
        sb.append('&');
      }
      sb.append(parameter.getKey());
      sb.append('=');
      sb.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
    }
    return sb.toString();
  }

  /**
   * Sends the request to the OAuth Provider, and returns the set of reply
   * parameters, mapped from name to decoded value.
   *
   * @throws IOException  if a problem occurs making the request or getting the
   *   reply.
   */
  public Map<String,String> sendRequest() throws IOException {
    HttpURLConnection connection =
      (HttpURLConnection)(new URL(encode())).openConnection();
    int responseCode = connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Server returned error code: " + responseCode +
          " " + connection.getResponseMessage());
    }
    BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(connection.getInputStream()));
    String result = bufferedReader.readLine();
    Map<String,String> responseParameters = new HashMap<String,String>();
    for (String param : result.split("&")) {
      int equalsAt = param.indexOf('=');
      if (equalsAt > 0) {
        String name = param.substring(0, equalsAt);
        String value =
          URLDecoder.decode(param.substring(equalsAt + 1), "UTF-8");
        responseParameters.put(name, value);
      }
    }
    return responseParameters;
  }
  
}
