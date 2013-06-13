/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.clients;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.evernote.auth.EvernoteAuth;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.Constants;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.protocol.TProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.evernote.thrift.transport.TTransportException;

/**
 * A class to produce User and Note store clients.
 * 
 * @author kentaro suzuki
 */

public class ClientFactory {

  private static final String USER_AGENT_KEY = "User-Agent";
  private static final Pattern CONSUMER_KEY_REGEX = Pattern
      .compile(":A=([^:]+):");

  private EvernoteAuth evernoteAuth;
  private String userAgent;
  private Map<String, String> customHeaders;

  /**
   * @param auth
   */
  public ClientFactory(EvernoteAuth auth) {
    if (auth == null) {
      throw new IllegalArgumentException("token must not be null");
    }
    this.evernoteAuth = auth;
  }

  /**
   * Create a new UserStore client. Each call to this method will return a new
   * UserStore.Client instance. The returned client can be used for any number
   * of API calls, but is NOT thread safe.
   * 
   * @param url
   *          to connect to
   * 
   * @throws TTransportException
   *           if an error occurs setting up the connection to the Evernote
   *           service.
   * 
   */
  public UserStoreClient createUserStoreClient() throws TTransportException {
    String serviceUrl = this.evernoteAuth.getUserStoreUrl();
    return createStoreClient(UserStoreClient.class, serviceUrl,
        this.evernoteAuth.getToken());
  }

  /**
   * Create a new NoteStore client. Each call to this method will return a new
   * NoteStore.Client instance. The returned client can be used for any number
   * of API calls, but is NOT thread safe.
   * 
   * @throws TException
   * @throws EDAMSystemException
   * @throws EDAMUserException
   */
  public NoteStoreClient createNoteStoreClient() throws EDAMUserException,
      EDAMSystemException, TException {
    String noteStoreUrl = this.evernoteAuth.getNoteStoreUrl();
    if (noteStoreUrl == null) {
      noteStoreUrl = createUserStoreClient().getNoteStoreUrl();
      this.evernoteAuth.setNoteStoreUrl(noteStoreUrl);
    }
    return createStoreClient(NoteStoreClient.class, noteStoreUrl,
        this.evernoteAuth.getToken());
  }

  /**
   * Creates a LinkedNoteStoreClient from a {@link LinkedNotebook} synchronously
   * 
   * @param linkedNotebook
   */
  public LinkedNoteStoreClient createLinkedNoteStoreClient(
      LinkedNotebook linkedNotebook) throws EDAMUserException,
      EDAMSystemException, TException, EDAMNotFoundException {

    NoteStoreClient mainNoteStoreClient = createNoteStoreClient();
    AuthenticationResult sharedAuth = mainNoteStoreClient
        .authenticateToSharedNotebook(linkedNotebook.getShareKey());
    NoteStoreClient linkedNoteStoreClient = createStoreClient(
        NoteStoreClient.class, linkedNotebook.getNoteStoreUrl(),
        sharedAuth.getAuthenticationToken());

    return new LinkedNoteStoreClient(mainNoteStoreClient,
        linkedNoteStoreClient, sharedAuth);
  }

  /**
   * Create a new Business NoteStore client. Each call to this method will
   * return a new NoteStore.Client instance. The returned client can be used for
   * any number of API calls, but is NOT thread safe.
   * 
   * This method will check expiration time for the business authorization
   * token, this is a network request
   * 
   * This method is synchronous
   * 
   * @throws TException
   * @throws EDAMUserException
   * @throws EDAMSystemException
   *           User is not part of a business
   */
  public BusinessNoteStoreClient createBusinessNoteStoreClient()
      throws TException, EDAMUserException, EDAMSystemException {

    NoteStoreClient mainNoteStoreClient = createNoteStoreClient();
    AuthenticationResult businessAuthResult = createUserStoreClient()
        .authenticateToBusiness();
    NoteStoreClient linkedNoteStoreClient = createStoreClient(
        NoteStoreClient.class, businessAuthResult.getNoteStoreUrl(),
        businessAuthResult.getAuthenticationToken());

    return new BusinessNoteStoreClient(mainNoteStoreClient,
        linkedNoteStoreClient, businessAuthResult);
  }

  /**
   * @param clientClass
   * @param url
   * @param token
   * @return
   * @throws TTransportException
   */
  <T> T createStoreClient(Class<T> clientClass, String url, String token)
      throws TTransportException {
    THttpClient transport = new THttpClient(url);

    transport.setCustomHeader(USER_AGENT_KEY, generateUserAgent());
    if (customHeaders != null) {
      for (Map.Entry<String, String> header : customHeaders.entrySet()) {
        transport.setCustomHeader(header.getKey(), header.getValue());
      }
    }

    TProtocol protocol = new TBinaryProtocol(transport);
    try {
      return clientClass.getDeclaredConstructor(TProtocol.class,
          TProtocol.class, String.class).newInstance(protocol, protocol, token);
    } catch (Throwable e) {
      throw new RuntimeException("Couldn't create " + clientClass.getName()
          + " due to the error.", e);
    }
  }

  /**
   * The user agent defined for the connection
   */
  String generateUserAgent() {
    String id = userAgent;
    if (id == null) {
      Matcher matcher = CONSUMER_KEY_REGEX.matcher(evernoteAuth.getToken());
      if (matcher.find() && matcher.groupCount() >= 1
          && matcher.group(1) != null) {
        id = matcher.group(1);
      } else {
        id = "";
      }
    }

    String sdkVersion = getClass().getPackage().getImplementationVersion();
    if (sdkVersion == null) {
      sdkVersion = Constants.EDAM_VERSION_MAJOR + "."
          + Constants.EDAM_VERSION_MINOR;
    }
    String javaVersion = System.getProperty("java.version");
    return id + " / " + sdkVersion + "; Java / " + javaVersion;
  }

  /**
   * Set a custom UserAgent String for the client connection
   * 
   * @param userAgent
   */
  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Allows custom headers to be defined for the Client connection
   * 
   * @param customHeaders
   */
  public void setCustomHeaders(Map<String, String> customHeaders) {
    this.customHeaders = customHeaders;
  }

}
