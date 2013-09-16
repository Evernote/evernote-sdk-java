/*
 * Copyright 2013 Evernote Corporation All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.clients;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

public class LinkedNoteStoreClientTest {

  // Please set developer token before testing
  String token = null;

  LinkedNotebook linkedNotebook = null;
  LinkedNoteStoreClient client;

  @Before
  public void initialize() throws Exception {    
    assertNotNull(token);

    EvernoteAuth auth = new EvernoteAuth(EvernoteService.SANDBOX, token);
    ClientFactory factory = new ClientFactory(auth);
    linkedNotebook = factory.createNoteStoreClient().listLinkedNotebooks().get(0);
    client = factory.createLinkedNoteStoreClient(linkedNotebook);

  }

  @Test
  public void testCreateNote() throws Exception {
    Note note = new Note();
    note.setTitle("LinkedNoteStoreClientTest#testCreateNote");
    note.setContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
        + "<en-note>" + "TEST" + "</en-note>");
    Note createdNote = client.createNote(note);
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
