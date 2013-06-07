/*
 * Copyright 2012 Evernote Corporation.
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

import java.util.List;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.thrift.TException;

/**
 * This is a wrapper/helper class that manages the connection to a linked
 * notestore. It maintains two {@link LinkedNoteStoreClient} objects, one points
 * to the users personal store and the other to linked notebooks shard.
 * 
 * 
 * @author @tylersmithnet
 * @author kentaro suzuki
 */
public class LinkedNoteStoreClient {
  /**
   * References users main note store
   */
  private NoteStoreClient mainNoteStoreClient;
  private NoteStoreClient linkedNoteStoreClient;
  private AuthenticationResult authenticationResult;

  LinkedNoteStoreClient(NoteStoreClient mainNoteStoreClient,
      NoteStoreClient linkedNoteStoreClient,
      AuthenticationResult authenticationResult) {
    this.mainNoteStoreClient = mainNoteStoreClient;
    this.linkedNoteStoreClient = linkedNoteStoreClient;
    this.authenticationResult = authenticationResult;
  }

  /**
   * Returns the {@link NoteStoreClient} object that has been instantiated to
   * the appropriate shard
   * 
   * @return
   */
  public NoteStoreClient getClient() {
    return linkedNoteStoreClient;
  }

  NoteStoreClient getPersonalClient() {
    return mainNoteStoreClient;
  }

  AuthenticationResult getAuthenticationResult() {
    return authenticationResult;
  }

  String getToken() {
    return getAuthenticationResult().getAuthenticationToken();
  }

  /**
   * Helper method to create a note in a linked notebook
   * 
   * @param note
   * @param linkedNotebook
   * @return
   * @throws com.evernote.edam.error.EDAMUserException
   * 
   * @throws com.evernote.edam.error.EDAMSystemException
   * 
   * @throws com.evernote.thrift.TException
   * @throws com.evernote.edam.error.EDAMNotFoundException
   * 
   */
  public Note createNote(Note note, LinkedNotebook linkedNotebook)
      throws EDAMUserException, EDAMSystemException, TException,
      EDAMNotFoundException {

    SharedNotebook sharedNotebook = getClient().getSharedNotebookByAuth();
    note.setNotebookGuid(sharedNotebook.getNotebookGuid());
    return getClient().createNote(note);

  }

  /**
   * Helper method to list linked notebooks
   * 
   * @see {@link com.evernote.edam.notestore.NoteStore.Client#listLinkedNotebooks(String)}
   * 
   */
  public List<LinkedNotebook> listNotebooks() throws EDAMUserException,
      EDAMSystemException, TException, EDAMNotFoundException {
    return getPersonalClient().listLinkedNotebooks();
  }

  /**
   * Will return the {@link com.evernote.edam.type.Notebook} associated with the
   * {@link com.evernote.edam.type.LinkedNotebook} from the linked account
   * 
   * @param linkedNotebook
   */
  public Notebook getCorrespondingNotebook(LinkedNotebook linkedNotebook)
      throws TException, EDAMUserException, EDAMSystemException,
      EDAMNotFoundException {
    SharedNotebook sharedNotebook = getClient().getSharedNotebookByAuth();
    return getClient().getNotebook(sharedNotebook.getNotebookGuid());
  }

  /**
   * Checks writable permissions of {@link LinkedNotebook} on Linked account
   * 
   * @param linkedNotebook
   */
  public boolean isNotebookWritable(LinkedNotebook linkedNotebook)
      throws EDAMUserException, TException, EDAMSystemException,
      EDAMNotFoundException {
    Notebook notebook = getCorrespondingNotebook(linkedNotebook);
    return !notebook.getRestrictions().isNoCreateNotes();
  }

}
