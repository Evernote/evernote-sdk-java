/*
 * Copyright 2013 Evernote Corporation
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
package com.evernote.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class EvernoteAuthTest {

  String response;

  @Before
  public void initialize() {
    response = "oauth_token=S%3Ds4%3AU%3Da1%3AE%3D12bfd68c6b6%3AC%3D12bf8426ab8%3AP%3D7%3AA%3Den_oauth_test%3AH%3D3df9cf6c0d7bc410824c80231e64dbe1&oauth_token_secret=&edam_noteStoreUrl=https%3A%2F%2Fsandbox.evernote.com%2Fshard%2Fs1%2Fnotestore&edam_userId=161&edam_webApiUrlPrefix=https%3A%2F%2Fsandbox.evernote.com%2Fshard%2Fs1%2F";
  }

  @Test
  public void testParseOAuthResponse() {
    EvernoteAuth auth = EvernoteAuth.parseOAuthResponse(
        EvernoteService.SANDBOX, response);
    assertEquals(
        "S=s4:U=a1:E=12bfd68c6b6:C=12bf8426ab8:P=7:A=en_oauth_test:H=3df9cf6c0d7bc410824c80231e64dbe1",
        auth.getToken());
    assertEquals("https://sandbox.evernote.com/shard/s1/notestore",
        auth.getNoteStoreUrl());
    assertEquals("https://sandbox.evernote.com/shard/s1/",
        auth.getWebApiUrlPrefix());
    assertEquals(161, auth.getUserId());
  }

  @Test
  public void testExtractToken() {
    assertEquals(
        "S=s4:U=a1:E=12bfd68c6b6:C=12bf8426ab8:P=7:A=en_oauth_test:H=3df9cf6c0d7bc410824c80231e64dbe1",
        EvernoteAuth.extractToken(response));
  }

  @Test
  public void testExtractNoteStoreUrl() {
    assertEquals("https://sandbox.evernote.com/shard/s1/notestore",
        EvernoteAuth.extractNoteStoreUrl(response));
  }

  @Test
  public void testExtractWebApiUrl() {
    assertEquals("https://sandbox.evernote.com/shard/s1/",
        EvernoteAuth.extractWebApiUrl(response));
  }

  @Test
  public void testExtractUserId() {
    assertEquals("161", EvernoteAuth.extractUserId(response));
  }

}
