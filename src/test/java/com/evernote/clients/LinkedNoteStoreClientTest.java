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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.NotebookRestrictions;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.userstore.AuthenticationResult;

public class LinkedNoteStoreClientTest {

  // If set, test with actual API calls
  String token = null;

  LinkedNotebook linkedNotebook = null;
  LinkedNoteStoreClient client;

  @Before
  public void initialize() throws Exception {
    if (token == null) {
      client = mock(LinkedNoteStoreClient.class);

      NoteStoreClient noteStoreClient = mock(NoteStoreClient.class);

      Note createdNote = new Note();
      createdNote.setGuid("guid");
      stub(noteStoreClient.createNote(isA(Note.class))).toReturn(createdNote);

      NotebookRestrictions restrictions = new NotebookRestrictions();
      restrictions.setNoCreateNotes(false);

      Notebook createdNotebook = new Notebook();
      createdNotebook.setRestrictions(restrictions);
      stub(noteStoreClient.createNotebook(isA(Notebook.class))).toReturn(
          createdNotebook);
      stub(noteStoreClient.getNotebook(anyString())).toReturn(createdNotebook);

      SharedNotebook sharedNotebook = new SharedNotebook();
      stub(noteStoreClient.getSharedNotebookByAuth()).toReturn(sharedNotebook);

      NoteStoreClient personalClient = mock(NoteStoreClient.class);

      List<LinkedNotebook> listLinkedNotebooks = new ArrayList<LinkedNotebook>();
      stub(personalClient.listLinkedNotebooks()).toReturn(listLinkedNotebooks);

      LinkedNotebook createdLinkedNotebook = new LinkedNotebook();
      stub(personalClient.createLinkedNotebook(isA(LinkedNotebook.class)))
          .toReturn(createdLinkedNotebook);

      AuthenticationResult authenticationResult = new AuthenticationResult();

      client = new LinkedNoteStoreClient(personalClient, noteStoreClient,
          authenticationResult);
    } else {
      EvernoteAuth auth = new EvernoteAuth(EvernoteService.SANDBOX, token);
      ClientFactory factory = new ClientFactory(auth);
      linkedNotebook = factory.createNoteStoreClient().listLinkedNotebooks()
          .get(0);
      client = factory.createLinkedNoteStoreClient(linkedNotebook);
    }
  }

  @Test
  public void testCreateNote() throws Exception {
    Note note = new Note();
    note.setTitle("LinkedNoteStoreClientTest#testCreateNote");
    note.setContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
        + "<en-note>" + "TEST" + "</en-note>");
    Note createdNote = client.createNote(note, linkedNotebook);
    assertNotNull(createdNote.getGuid());
  }

  @Test
  public void testListNotebooks() throws Exception {
    List<LinkedNotebook> listLinkedNotebooks = client.listNotebooks();
    assertNotNull(listLinkedNotebooks);
  }

  @Test
  public void testGetCorrespondingNotebook() throws Exception {
    Notebook notebook = client.getCorrespondingNotebook(linkedNotebook);
    assertNotNull(notebook);
  }

  @Test
  public void testIsNotebookWritable() throws Exception {
    boolean isNotebookWritable = client.isNotebookWritable(linkedNotebook);
    assertTrue(isNotebookWritable);
  }

}
