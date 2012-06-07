<%-- 

  Demonstrates the use of OAuth to authenticate to the Evernote API in 
  four steps:
  
  1. Obtain an OAuth request token from the Evernote service.
  2. Redirect the user to Evernote.com to authorize access to their account.
  3. Upon receiving a successful redirect back from Evernote.com, exchange
     the OAuth request token for an OAuth access token.
  4. Use the OAuth access token to access the Evernote API, in this case
     listing the notebooks in the user's Evernote account.
  
  In this sample, we use the Scribe library to perform the actual OAuth
  requests. You don't have to use Scribe; any OAuth consumer library should work.
  Read about Scribe at https://github.com/fernandezpablo85/scribe-java

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
<%@ page import='com.evernote.client.oauth.*' %>
<%@ page import='org.scribe.builder.ServiceBuilder' %>
<%@ page import='org.scribe.oauth.*' %>
<%@ page import='org.scribe.model.*' %>
<%!

  /*
   * Fill in your Evernote API key. To get an API key, go to
   * http://dev.evernote.com/documentation/cloud/
   */
  static final String consumerKey = "";
  static final String consumerSecret = "";
  
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
  String requestTokenSecret = (String)session.getAttribute("requestTokenSecret");
  String verifier = (String)session.getAttribute("verifier");

  String action = request.getParameter("action");

  if ("".equals(consumerKey)) {
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
    
      // Set up the Scribe OAuthService. To access the Evernote production service,
      // remove EvernoteSandboxApi from the provider class below.
      String thisUrl = request.getRequestURL().toString();
      String cbUrl = thisUrl.substring(0, thisUrl.lastIndexOf('/') + 1) + callbackUrl;
    
      Class providerClass = org.scribe.builder.api.EvernoteApi.Sandbox.class;
      if (urlBase.equals("https://www.evernote.com")) {
        providerClass = org.scribe.builder.api.EvernoteApi.class;
      }
      OAuthService service = new ServiceBuilder()
          .provider(providerClass)
          .apiKey(consumerKey)
          .apiSecret(consumerSecret)
          .callback(cbUrl)
          .build();

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
          Token scribeRequestToken = service.getRequestToken();
					
          out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + scribeRequestToken.getRawResponse() + "</span>");
          requestToken = scribeRequestToken.getToken();
          session.setAttribute("requestToken", requestToken);
          session.setAttribute("requestTokenSecret", scribeRequestToken.getSecret());

        } else if ("getAccessToken".equals(action)) {
          // Send an OAuth message to the Provider asking to exchange the
          // existing Request Token for an Access Token
          Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
          Verifier scribeVerifier = new Verifier(verifier);
          EvernoteAuthToken token = new EvernoteAuthToken(service.getAccessToken(scribeRequestToken, scribeVerifier));
          out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + token.getRawResponse() + "</span>");
          accessToken = token.getToken();
          String noteStoreUrl = token.getNoteStoreUrl();
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
User request token secret: <%= session.getAttribute("requestTokenSecret") %><br/>
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
