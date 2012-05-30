package com.evernote.oauth.consumer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

/** 
 * A Scribe AccessTokenExtractor that allows empty token secrets. 
 */ 
public class AccessTokenExtractor implements org.scribe.extractors.AccessTokenExtractor {
  private static final Pattern TOKEN_REGEX = Pattern.compile("oauth_token=([^&]+)");
  private static final Pattern SECRET_REGEX = Pattern.compile("oauth_token_secret=([^&]*)");
  private static final Pattern NOTESTORE_REGEX = Pattern.compile("edam_noteStoreUrl=([^&]+)");
  private static final Pattern WEBAPI_REGEX = Pattern.compile("edam_webApiUrlPrefix=([^&]+)");
  private static final Pattern USERID_REGEX = Pattern.compile("edam_userId=([^&]+)");
  
  /**
   * {@inheritDoc} 
   */
  public Token extract(String response)
  {
    Preconditions.checkEmptyString(response, "Response body is incorrect. " +
        "Can't extract a token from an empty string");
    return new EvernoteAuthToken(extract(response, TOKEN_REGEX), 
        extract(response, SECRET_REGEX), extract(response, NOTESTORE_REGEX), 
        extract(response, WEBAPI_REGEX), extract(response, USERID_REGEX), response);
  }

  private String extract(String response, Pattern p)
  {
    Matcher matcher = p.matcher(response);
    if (matcher.find() && matcher.groupCount() >= 1)
    {
      return OAuthEncoder.decode(matcher.group(1));
    }
    else
    {
      throw new OAuthException("Response body is incorrect. " +
          "Can't extract token and secret from this: '" + response + "'", null);
    }
  }
  
  /** 
   * A Scribe AccessToken that contains Evernote-specific items from the OAuth response. 
   */
  public class EvernoteAuthToken extends Token {

    public String noteStoreUrl;
    public String webApiUrlPrefix;
    public String userId;
    
    public EvernoteAuthToken(String token, String secret,
        String noteStoreUrl, String webApiUrlPrefix, String userId, String rawResponse) {
      super(token, secret, rawResponse);
      this.noteStoreUrl = noteStoreUrl;
      this.webApiUrlPrefix = webApiUrlPrefix;
      this.userId = userId;
    }
  }
}