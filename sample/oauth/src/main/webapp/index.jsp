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

  Learn more at http://dev.evernote.com/start/core/authentication.php
  
  Copyright 2008-2012 Evernote Corporation. All rights reserved.

--%>
<%@ page import='java.util.*' %>
<%@ page import='java.net.*' %>
<%@ page import='com.evernote.auth.EvernoteAuth' %>
<%@ page import='com.evernote.auth.EvernoteService' %>
<%@ page import="com.evernote.clients.NoteStoreClient" %>
<%@ page import="com.evernote.clients.ClientFactory" %>
<%@ page import='com.evernote.edam.type.*' %>
<%@ page import='org.scribe.builder.api.EvernoteApi' %>
<%@ page import='org.scribe.builder.ServiceBuilder' %>
<%@ page import='org.scribe.model.*' %>
<%@ page import='org.scribe.oauth.OAuthService' %>
<%!
/*
 * Fill in your Evernote API key. To get an API key, go to
 * http://dev.evernote.com/documentation/cloud/
 */
static final String CONSUMER_KEY = "";
static final String CONSUMER_SECRET = "";

/*
 * Replace this value with EvernoteService.PRODUCTION to switch from the Evernote
 * sandbox server to the Evernote production server.
 */
static final EvernoteService EVERNOTE_SERVICE = EvernoteService.SANDBOX;
static final String CALLBACK_URL = "index.jsp?action=callbackReturn";
%>
<%
String accessToken = (String)session.getAttribute("accessToken");
String requestToken = (String)session.getAttribute("requestToken");
String requestTokenSecret = (String)session.getAttribute("requestTokenSecret");
String verifier = (String)session.getAttribute("verifier");
String noteStoreUrl = (String)session.getAttribute("noteStoreUrl");

String action = request.getParameter("action");

if ("".equals(CONSUMER_KEY)) {
%>
<span style="color: red"> Before using this sample code you must
  edit the file index.jsp and replace CONSUMER_KEY and CONSUMER_SECRET with
  the values that you received from Evernote. If you do not have an API
  key, you can request one from <a
  href="http://dev.evernote.com/documentation/cloud/">http://dev.evernote.com/documentation/cloud/</a>
</span>
<%
} else {
  if (action != null) {
%>

<hr />
<h3>Action results:</h3>
<pre>

<%
    // Set up the Scribe OAuthService.
    String thisUrl = request.getRequestURL().toString();
    String cbUrl = thisUrl.substring(0, thisUrl.lastIndexOf('/') + 1) + CALLBACK_URL;
        
    Class<? extends EvernoteApi> providerClass = EvernoteApi.Sandbox.class;
    if (EVERNOTE_SERVICE == EvernoteService.PRODUCTION) {
      providerClass = org.scribe.builder.api.EvernoteApi.class;
    }
    OAuthService service = new ServiceBuilder()
      .provider(providerClass)
      .apiKey(CONSUMER_KEY)
      .apiSecret(CONSUMER_SECRET)
      .callback(cbUrl)
      .build();

    try {
      if ("reset".equals(action)) {
        System.err.println("Resetting");
        // Empty the server's stored session information for the current
        // browser user so we can redo the test.
        for (Enumeration<?> names = session.getAttributeNames(); names.hasMoreElements();) {
          session.removeAttribute((String)names.nextElement());
        }
        accessToken = null;
        requestToken = null;
        requestTokenSecret = null;
        verifier = null;
        noteStoreUrl = null;
        out.println("Removed all attributes from user session");

      } else if ("getRequestToken".equals(action)) {
        // Send an OAuth message to the Provider asking for a new Request
        // Token because we don't have access to the current user's account.
        Token scribeRequestToken = service.getRequestToken();
    
        out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + scribeRequestToken.getRawResponse() + "</span>");
        requestToken = scribeRequestToken.getToken();
        requestTokenSecret = scribeRequestToken.getSecret();
        session.setAttribute("requestToken", requestToken);
        session.setAttribute("requestTokenSecret", requestTokenSecret);

      } else if ("getAccessToken".equals(action)) {
        // Send an OAuth message to the Provider asking to exchange the
        // existing Request Token for an Access Token
        Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
        Verifier scribeVerifier = new Verifier(verifier);
        Token scribeAccessToken = service.getAccessToken(scribeRequestToken, scribeVerifier);
        EvernoteAuth evernoteAuth = EvernoteAuth.parseOAuthResponse(EVERNOTE_SERVICE, scribeAccessToken.getRawResponse());
        out.println("<br/><b>Reply:</b> <br/> <span style=\"word-wrap: break-word\">" + scribeAccessToken.getRawResponse() + "</span>");
        accessToken = evernoteAuth.getToken();
        noteStoreUrl = evernoteAuth.getNoteStoreUrl();
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("noteStoreUrl", noteStoreUrl);
       
      } else if ("callbackReturn".equals(action)) {
        requestToken = request.getParameter("oauth_token");
        verifier = request.getParameter("oauth_verifier");
        session.setAttribute("verifier", verifier);

      } else if ("listNotebooks".equals(action)) {
        out.println("Listing notebooks from: " + noteStoreUrl);
        EvernoteAuth evernoteAuth = new EvernoteAuth(EVERNOTE_SERVICE, accessToken);
        NoteStoreClient noteStoreClient = new ClientFactory(evernoteAuth).createNoteStoreClient();
        
        List<Notebook> notebooks = noteStoreClient.listNotebooks();
        for (Notebook notebook : notebooks) {
          out.println("Notebook: " + notebook.getName());
        }
        
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println(e.toString());
    }
%>

</pre>
<hr />

<%
  }
%>

<!-- Information used by consumer -->
<h3>Evernote EDAM API Web Test State</h3>
Consumer key: <%= CONSUMER_KEY %><br />
Request token URL: <%= EVERNOTE_SERVICE.getRequestTokenEndpoint() %> <br />
Access token URL: <%= EVERNOTE_SERVICE.getAccessTokenEndpoint() %> <br />
Authorization URL Base: <%= EVERNOTE_SERVICE.getHost() %> <br />
<br />
User request token: <%= requestToken %><br />
User request token secret: <%= requestTokenSecret %><br />
User oauth verifier: <%= verifier %><br />
User access token: <%= accessToken %><br />
User NoteStore URL: <%= noteStoreUrl %>

<!-- Manual operation controls -->
<hr />
<h3>Actions</h3>

<ol>

  <!-- Step 1 in OAuth authorization: obtain an unauthorized request token from the provider -->
  <li>
<%
  if (requestToken == null && accessToken == null) {
%>
    <a href='?action=getRequestToken'>Get OAuth Request Token from Provider</a>
<%
  } else {
%>
    Get OAuth Request Token from Provider
<%
  }
%>
  </li>

  <!-- Step 2 in OAuth authorization: redirect the user to the provider to authorize the request token -->
  <li>
<%
  if (requestToken != null && verifier == null && accessToken == null) {
%>
    <a href='<%= EVERNOTE_SERVICE.getAuthorizationUrl(requestToken) %>'>Send user to get authorization</a>
<%
  } else {
%>
    Send user to get authorization
<%
  }
%>
  </li>

  <!-- Step 3 in OAuth authorization: exchange the authorized request token for an access token -->
  <li>
<%
  if (requestToken != null && verifier != null && accessToken == null) {
%>
    <a href="?action=getAccessToken"> Get OAuth Access Token from Provider </a>
<%
  } else {
%>
    Get OAuth Access Token from Provider
<%
  }
%>
  </li>

  <!-- Step 4 in OAuth authorization: use the access token that you obtained -->
  <!-- In this sample, we simply list the notebooks in the user's Evernote account -->
  <li>
<%
  if (accessToken != null) {
%>
    <a href="?action=listNotebooks">List notebooks in account</a><br />
<%
  } else {
%>
    List notebooks in account
<%
  }
%>
  </li>
</ol>

<a href="?action=reset">Reset user session</a>

<%
}
%>
