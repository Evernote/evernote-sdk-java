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
package com.evernote.clients;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;

public class BusinessNoteStoreClientTest {

  // If set, test with actual API calls
  String token = null;

  BusinessNoteStoreClient client;

  @Before
  public void initialize() throws Exception {
    if (token == null) {
      NoteStoreClient noteStoreClient = mock(NoteStoreClient.class);

      SharedNotebook sharedNotebook = new SharedNotebook();

      Notebook createdNotebook = new Notebook();
      createdNotebook.setSharedNotebooks(Arrays.asList(sharedNotebook));
      stub(noteStoreClient.createNotebook(isA(Notebook.class))).toReturn(
          createdNotebook);

      NoteStoreClient personalClient = mock(NoteStoreClient.class);

      List<LinkedNotebook> listLinkedNotebooks = new ArrayList<LinkedNotebook>();
      stub(personalClient.listLinkedNotebooks()).toReturn(listLinkedNotebooks);

      LinkedNotebook createdLinkedNotebook = new LinkedNotebook();
      stub(personalClient.createLinkedNotebook(isA(LinkedNotebook.class)))
          .toReturn(createdLinkedNotebook);

      User user = new User();
      user.setUsername("username");
      user.setShardId("shardId");
      AuthenticationResult authenticationResult = new AuthenticationResult();
      authenticationResult.setUser(user);

      client = new BusinessNoteStoreClient(personalClient, noteStoreClient,
          authenticationResult);
    } else {
      EvernoteAuth auth = new EvernoteAuth(EvernoteService.SANDBOX, token);
      client = new ClientFactory(auth).createBusinessNoteStoreClient();
    }
  }

  @Test
  public void testCreateNotebook() throws Exception {
    Notebook notebook = new Notebook();
    notebook.setName("BusinessNoteStoreClientTest#testCreateNotebook");
    LinkedNotebook createdLinkedNotebook = client.createNotebook(notebook);
    assertNotNull(createdLinkedNotebook);
  }

  @Test
  public void testListNotebooks() throws Exception {
    List<LinkedNotebook> listLinkedNotebooks = client.listNotebooks();
    for (LinkedNotebook linkedNotebook : listLinkedNotebooks) {
      assertTrue(linkedNotebook.isSetBusinessId());
    }
  }

}
