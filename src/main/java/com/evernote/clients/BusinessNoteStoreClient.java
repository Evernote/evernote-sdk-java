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

import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.thrift.TException;

/**
 * This is a wrapper/helper class that manages the connection to a business
 * notestore. It maintains two {@link AsyncLinkedNoteStoreClient} objects, one
 * points to the users personal store and the other to the business shard.
 * 
 * These helper methods make network calls across both shards to return the
 * appropriate data.
 * 
 * @author @tylersmithnet
 * @author kentaro suzuki
 */
public class BusinessNoteStoreClient extends LinkedNoteStoreClient {

  BusinessNoteStoreClient(NoteStoreClient mainNoteStoreClient,
      NoteStoreClient linkedNoteStoreClient,
      AuthenticationResult authenticationResult) {
    super(mainNoteStoreClient, linkedNoteStoreClient, authenticationResult);
  }

  /**
   * Create Linked Notebook from a Notebook
   * 
   * @return {@link LinkedNotebook} with guid from server
   */
  public LinkedNotebook createNotebook(Notebook notebook) throws TException,
      EDAMUserException, EDAMSystemException, EDAMNotFoundException {

    Notebook originalNotebook = getClient().createNotebook(notebook);

    SharedNotebook sharedNotebook = originalNotebook.getSharedNotebooks()
        .get(0);
    LinkedNotebook linkedNotebook = new LinkedNotebook();
    linkedNotebook.setShareKey(sharedNotebook.getShareKey());
    linkedNotebook.setShareName(originalNotebook.getName());
    linkedNotebook.setUsername(getAuthenticationResult().getUser()
        .getUsername());
    linkedNotebook.setShardId(getAuthenticationResult().getUser().getShardId());

    return getPersonalClient().createLinkedNotebook(linkedNotebook);
  }

  /**
   * Helper method to list business notebooks synchronously
   * 
   * @return
   * @throws EDAMUserException
   * @throws EDAMSystemException
   * @throws TException
   * @throws EDAMNotFoundException
   */
  @Override
  public List<LinkedNotebook> listNotebooks() throws EDAMUserException,
      EDAMSystemException, TException, EDAMNotFoundException {

    List<LinkedNotebook> linkedNotebooks = new ArrayList<LinkedNotebook>();
    for (LinkedNotebook notebook : super.listNotebooks()) {
      if (notebook.isSetBusinessId()) {
        linkedNotebooks.add(notebook);
      }
    }
    return linkedNotebooks;
  }

}
