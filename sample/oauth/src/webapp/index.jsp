<%-- 

  Demonstrates the use of OAuth to authenticate to the Evernote API.

  Learn more at http://dev.evernote.com/documentation/cloud/chapters/Authentication.php
  
  Copyright 2008-2012 Evernote Corporation. All rights reserved.

--%>
<%@ page import='java.util.*' %>
<%@ page import='java.net.*' %>
<%@ page import='org.apache.thrift.*' %>
<%@ page import='org.apache.thrift.protocol.TBinaryProtocol' %>
<%@ page import='org.apache.thrift.transport.THttpClient' %>
<%@ page import='com.evernote.edam.type.*' %>
<%@ page import='com.evernote.edam.notestore.*' %>
<%@ page import='com.evernote.oauth.consumer.*' %>
<%!

  /*
   * Replace these values with the API consumer key and consumer secret that you
   * receive from Evernote. If you do not have an Evernote API key, you can request
   * one at http://www.evernote.com/about/developer/api/
   */
  static final String consumerKey = "en-oauth-test";
  static final String consumerSecret = "0123456789abcdef";
  
  /*
   * Replace this value with https://www.evernote.com to switch from the Evernote
   * sandbox server to the Evernote production server.
   */
  static final String urlBase = "https://sandbox.evernote.com";
  
  static final String requestTokenUrl = urlBase + "/oauth";
  static final String accessTokenUrl = urlBase + "/oauth";
  static final String authorizationUrlBase = urlBase + "/OAuth.action";
  
  static final String callbackUrl = "index.jsp?action=callbackReturn";
%>
<%
  String accessToken = (String)session.getAttribute("accessToken");
  String requestToken = (String)session.getAttribute("requestToken");
  String verifier = (String)session.getAttribute("verifier");

  String action = request.getParameter("action");

  if ("en-oauth-test".equals(consumerKey)) {
%>
    <span style="color:red">
    	Before using this sample code you must edit the file index.jsp
      and replace consumerKey and consumerSecret with the values that you received from Evernote.
      If you do not have an API key, you can request one from 
      <a href="http://dev.evernote.com/documentation/cloud/">http://dev.evernote.com/documentation/cloud/</a>
    </span>
<%
  } else {
	  if (action != null) {    
%>

    <hr/>
    <h3>Action results:</h3>
    <pre><%
    
      try {
        if ("reset".equals(action)) {
          System.err.println("Resetting");
          // Empty the server's stored session information for the current
          // browser user so we can redo the test.
          for (Enumeration<?> names = session.getAttributeNames();
               names.hasMoreElements(); ) {
            session.removeAttribute((String)names.nextElement());
          }
          accessToken = null;
          requestToken = null;
          verifier = null;
          out.println("Removed all attributes from user session");

        } else if ("getRequestToken".equals(action)) {
          // Send an OAuth message to the Provider asking for a new Request
          // Token because we don't have access to the current user's account.
          SimpleOAuthRequest oauthRequestor =
            new SimpleOAuthRequest(requestTokenUrl, consumerKey, consumerSecret, null);
            
          // Set the callback URL
          String thisUrl = request.getRequestURL().toString();
					String cbUrl = thisUrl.substring(0, thisUrl.lastIndexOf('/') + 1) + callbackUrl;
					oauthRequestor.setParameter("oauth_callback", cbUrl);
					
          out.println("<b>Request:</b> <br/><span style=\"word-wrap: break-word\">" + oauthRequestor.encode() + "</span>");
          Map<String,String> reply = oauthRequestor.sendRequest();
          out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + reply + "</span>");
          requestToken = reply.get("oauth_token");
          session.setAttribute("requestToken", requestToken);

        } else if ("getAccessToken".equals(action)) {
          // Send an OAuth message to the Provider asking to exchange the
          // existing Request Token for an Access Token
          SimpleOAuthRequest oauthRequestor =
            new SimpleOAuthRequest(requestTokenUrl, consumerKey, consumerSecret, null);
          oauthRequestor.setParameter("oauth_token",
              (String)session.getAttribute("requestToken"));
          oauthRequestor.setParameter("oauth_verifier", 
              (String)session.getAttribute("verifier"));
          out.println("<b>Request:</b> <br/><span style=\"word-wrap: break-word\">" + oauthRequestor.encode() + "</span>");
          Map<String,String> reply = oauthRequestor.sendRequest();
          out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + reply + "</span>");
          accessToken = reply.get("oauth_token");
          String noteStoreUrl = reply.get("edam_noteStoreUrl");
          session.setAttribute("accessToken", accessToken);
          session.setAttribute("noteStoreUrl", noteStoreUrl);
         
        } else if ("callbackReturn".equals(action)) {
          requestToken = request.getParameter("oauth_token");
          verifier = request.getParameter("oauth_verifier");
          session.setAttribute("verifier", verifier);
        } else if ("listNotebooks".equals(action)) {
          String noteStoreUrl = (String)session.getAttribute("noteStoreUrl");
          out.println("Listing notebooks from: " + noteStoreUrl);
          THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
          TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
          NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
          List<?> notebooks = noteStore.listNotebooks(accessToken);
          for (Object notebook : notebooks) {
            out.println("Notebook: " + ((Notebook)notebook).getName());
          }
          
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println(e.toString());
      }
  
    %></pre>
    <hr/>
<% } %>

<!-- Information used by consumer -->
<h3>Evernote EDAM API Web Test State</h3>
Consumer key: <%= consumerKey %><br/>
Request token URL: <%= requestTokenUrl %><br/>
Access token URL: <%= accessTokenUrl %><br/>
Authorization URL Base: <%= authorizationUrlBase %><br/>
<br/>
User request token: <%= session.getAttribute("requestToken") %><br/>
User oauth verifier: <%= session.getAttribute("verifier") %><br/>
User access token: <%= session.getAttribute("accessToken") %><br/>
User NoteStore URL: <%= session.getAttribute("noteStoreUrl") %>


<!-- Manual operation controls -->
<hr/>
<h3>Actions</h3>

<ol>
	
	<!-- Step 1 in OAuth authorization: obtain an unauthorized request token from the provider -->
	<li>
<% if (requestToken == null && accessToken == null) { %>
  <a href='?action=getRequestToken'>Get OAuth Request Token from Provider</a>
<% } else { %>
	Get OAuth Request Token from Provider
<% } %>
	</li>

	<!-- Step 2 in OAuth authorization: redirect the user to the provider to authorize the request token -->
	<li>
<%
	if (requestToken != null && verifier == null && accessToken == null) {
		String authorizationUrl = authorizationUrlBase + "?oauth_token=" + requestToken;
%>
    <a href='<%= authorizationUrl %>'>Send user to get authorization</a>    
<%
	} else {
%>
	Send user to get authorization
<% } %>
	</li>

	<!-- Step 3 in OAuth authorization: exchange the authorized request token for an access token -->
	<li>
<% if (requestToken != null && verifier != null && accessToken == null) { %>
    <a href="?action=getAccessToken">
      Get OAuth Access Token from Provider
    </a>
<% } else { %>
      Get OAuth Access Token from Provider
<% } %>
	</li>

	<!-- Step 4 in OAuth authorization: use the access token that you obtained -->
	<!-- In this sample, we simply list the notebooks in the user's Evernote account -->
	<li>
<% if (accessToken != null) { %>
  <a href="?action=listNotebooks">List notebooks in account</a><br/>
<% } else { %>
	List notebooks in account
<% } %>
	</li>
</ol>

<a href="?action=reset">Reset user session</a>

<% } %>
