/*
The MIT License

Copyright (c) 2010 Pablo Fernandez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Modified by Evernote for use with the Evernote API.
 */

package com.evernote.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Scribe AccessToken that contains Evernote-specific items from the OAuth
 * response.
 */
public class EvernoteAuth {

  private static final String CHARSET = "UTF-8";
  private static final String USER_STORE_PATH = "/edam/user";
  private static final Pattern NOTESTORE_REGEX = Pattern
      .compile("edam_noteStoreUrl=([^&]+)");
  private static final Pattern WEBAPI_REGEX = Pattern
      .compile("edam_webApiUrlPrefix=([^&]+)");
  private static final Pattern USERID_REGEX = Pattern
      .compile("edam_userId=([^&]+)");

  private EvernoteService service;
  private String token;
  private String noteStoreUrl;
  private String webApiUrlPrefix;
  private int userId;

  public EvernoteAuth(EvernoteService service, String token) {
    if (service == null || token == null) {
      throw new IllegalArgumentException(
          "EvernoteService and token must not be null.");
    }
    this.service = service;
    this.token = token;
  }

  public EvernoteAuth(EvernoteService service, String token, String rawResponse) {
    if (service == null || token == null) {
      throw new IllegalArgumentException(
          "EvernoteService and token must not be null.");
    }
    this.service = service;
    this.token = token;
    this.setNoteStoreUrl(extractNoteStoreUrl(rawResponse));
    this.webApiUrlPrefix = extractWebApiUrl(rawResponse);
    this.userId = Integer.parseInt(extractUserId(rawResponse));
  }
  
  String extractNoteStoreUrl(String response) {
    return extract(response, NOTESTORE_REGEX);
  }

  String extractWebApiUrl(String response) {
    return extract(response, WEBAPI_REGEX);
  }

  String extractUserId(String response) {
    return extract(response, USERID_REGEX);
  }

  private String extract(String response, Pattern p) {
    Matcher matcher = p.matcher(response);
    if (matcher.find() && matcher.groupCount() >= 1 && matcher.group(1) != null) {
      try {
        return URLDecoder.decode(matcher.group(1), CHARSET);
      } catch (UnsupportedEncodingException e) {
        throw new AuthException("Charset not found while decoding string: "
            + CHARSET, e);
      }
    } else {
      throw new AuthException("Response body is incorrect. "
          + "Can't extract token and secret from this: '" + response + "'",
          null);
    }
  }

  /**
   * Get the Evernote access token.
   */
  public String getToken() {
    return this.token;
  }

  /**
   * Get the Evernote web service UserStore URL response.
   */
  public String getUserStoreUrl() {
    return this.service.getEndpointUrl(USER_STORE_PATH);
  }

  /**
   * Get the Evernote web service NoteStore URL from the OAuth access token
   * response.
   */
  public String getNoteStoreUrl() {
    return this.noteStoreUrl;
  }

  /**
   * Set the Evernote web service NoteStore URL
   */
  public void setNoteStoreUrl(String noteStoreUrl) {
    this.noteStoreUrl = noteStoreUrl;
  }

  /**
   * Get the Evernote web API URL prefix from the OAuth access token response.
   */
  public String getWebApiUrlPrefix() {
    return this.webApiUrlPrefix;
  }

  /**
   * Get the numeric Evernote user ID from the OAuth access token response.
   */
  public int getUserId() {
    return this.userId;
  }

}
