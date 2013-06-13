/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, mClient
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    mClient list of conditions and the following disclaimer in the documentation
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

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.PremiumInfo;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.BootstrapInfo;
import com.evernote.edam.userstore.PublicUserInfo;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TProtocol;

/**
 * A wrapper for {@link UserStore.Client}
 * 
 * @author kentaro suzuki
 */
public class UserStoreClient {

  private final String token;
  private final UserStore.Client client;

  UserStoreClient(TProtocol prot, String token) {
    if (prot == null || token == null) {
      throw new IllegalArgumentException(
          "TProtocol and Token must not be null.");
    }
    this.client = new UserStore.Client(prot);
    this.token = token;
  }

  UserStoreClient(TProtocol iprot, TProtocol oprot, String token) {
    if (iprot == null || oprot == null || token == null) {
      throw new IllegalArgumentException(
          "TProtocol and Token must not be null.");
    }
    this.client = new UserStore.Client(iprot, oprot);
    this.token = token;
  }

  /**
   * If direct access to the Note Store is needed, all of these calls are
   * synchronous
   * 
   * @return {@link UserStore.Client}
   */
  UserStore.Client getClient() {
    return client;
  }

  /**
   * @return authToken inserted into calls
   */
  String getToken() {
    return token;
  }

  /**
   * Determine if a user belongs to a business account
   * 
   * @return the result of a user belonging to a business account
   */
  public boolean isBusinessUser() throws TException, EDAMUserException,
      EDAMSystemException {
    return getClient().getUser(getToken()).isSetBusinessUserInfo();
  }

  /**
   * @see UserStore.Client#checkVersion(String, short, short)
   */
  public boolean checkVersion(final String clientName,
      final short edamVersionMajor, final short edamVersionMinor)
      throws TException {
    return getClient().checkVersion(clientName, edamVersionMajor,
        edamVersionMinor);
  }

  /**
   * @see UserStore.Client#getBootstrapInfo(String)
   */
  public BootstrapInfo getBootstrapInfo(final String locale) throws TException {
    return getClient().getBootstrapInfo(locale);
  }

  /**
   * @see UserStore.Client#authenticate(String, String, String, String, boolean)
   */
  public AuthenticationResult authenticate(final String username,
      final String password, final String consumerKey,
      final String consumerSecret, final boolean supportsTwoFactor)
      throws EDAMUserException, EDAMSystemException, TException {
    return getClient().authenticate(username, password, consumerKey,
        consumerSecret, supportsTwoFactor);
  }

  /**
   * @see UserStore.Client#authenticateLongSession(String, String, String,
   *      String, String, String, boolean)
   */
  public AuthenticationResult authenticateLongSession(final String username,
      final String password, final String consumerKey,
      final String consumerSecret, final String deviceIdentifier,
      final String deviceDescription, final boolean supportsTwoFactor)
      throws EDAMUserException, EDAMSystemException, TException {
    return getClient().authenticateLongSession(username, password, consumerKey,
        consumerSecret, deviceIdentifier, deviceDescription, supportsTwoFactor);
  }

  /**
   * @see UserStore.Client#authenticateToBusiness(String)
   */
  public AuthenticationResult authenticateToBusiness()
      throws EDAMUserException, EDAMSystemException, TException {
    return getClient().authenticateToBusiness(getToken());
  }

  /**
   * @see UserStore.Client#refreshAuthentication(String)
   */
  public AuthenticationResult refreshAuthentication() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().refreshAuthentication(getToken());
  }

  /**
   * @see UserStore.Client#getUser(String)
   */
  public User getUser() throws EDAMUserException, EDAMSystemException,
      TException {
    return getClient().getUser(getToken());
  }

  /**
   * @see UserStore.Client#getPublicUserInfo(String)
   */
  public PublicUserInfo getPublicUserInfo(final String username)
      throws EDAMNotFoundException, EDAMSystemException, EDAMUserException,
      TException {
    return getClient().getPublicUserInfo(username);
  }

  /**
   * @see UserStore.Client#getPremiumInfo(String)
   */
  public PremiumInfo getPremiumInfo() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().getPremiumInfo(getToken());
  }

  /**
   * @see UserStore.Client#getNoteStoreUrl(String)
   */
  public String getNoteStoreUrl() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().getNoteStoreUrl(getToken());
  }

  /**
   * @see UserStore.Client#revokeLongSession(String)
   */
  public void revokeLongSession() throws EDAMUserException,
      EDAMSystemException, TException {
    getClient().revokeLongSession(getToken());
  }

  /**
   * @see UserStore.Client#completeTwoFactorAuthentication(String, String,
   *      String, String)
   */
  public void completeTwoFactorAuthentication(final String authenticationToken,
      final String oneTimeCode, final String deviceIdentifier,
      final String deviceDescription) throws EDAMUserException,
      EDAMSystemException, TException {
    getClient().completeTwoFactorAuthentication(authenticationToken,
        oneTimeCode, deviceIdentifier, deviceDescription);
  }

}
