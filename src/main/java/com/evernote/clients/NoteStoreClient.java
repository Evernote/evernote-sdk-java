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

import java.util.List;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.ClientUsageMetrics;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteEmailParameters;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.notestore.RelatedQuery;
import com.evernote.edam.notestore.RelatedResult;
import com.evernote.edam.notestore.RelatedResultSpec;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.LazyMap;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.SharedNotebookRecipientSettings;
import com.evernote.edam.type.Tag;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TProtocol;

/**
 * A wrapper for {@link NoteStore.Client}
 * 
 * @author kentaro suzuki
 */
public class NoteStoreClient {

  protected String token;
  protected final NoteStore.Client client;

  NoteStoreClient(TProtocol prot, String token) {
    if (prot == null || token == null) {
      throw new IllegalArgumentException(
          "TProtocol and Token must not be null.");
    }
    this.client = new NoteStore.Client(prot);
    this.token = token;
  }

  NoteStoreClient(TProtocol iprot, TProtocol oprot, String token) {
    if (iprot == null || oprot == null || token == null) {
      throw new IllegalArgumentException(
          "TProtocol and Token must not be null.");
    }
    this.client = new NoteStore.Client(iprot, oprot);
    this.token = token;
  }

  /**
   * If direct access to the Note Store is needed, all of these calls are
   * synchronous
   * 
   * @return {@link NoteStore.Client}
   */
  public NoteStore.Client getClient() {
    return client;
  }

  /**
   * @return authToken inserted into calls
   */
  String getToken() {
    return token;
  }

  /**
   * @see NoteStore.Client#getSyncState(String)
   */
  public SyncState getSyncState() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().getSyncState(getToken());
  }

  /**
   * @see NoteStore.Client##getSyncStateWithMetrics(com.evernote.edam.notestore
   *      .ClientUsageMetrics, OnClientCallback)
   */
  public SyncState getSyncStateWithMetrics(ClientUsageMetrics clientMetrics)
      throws EDAMUserException, EDAMSystemException, TException {
    return getClient().getSyncStateWithMetrics(getToken(), clientMetrics);
  }

  /**
   * @see NoteStore.Client#getSyncChunk(String, int, int, boolean)
   */
  public SyncChunk getSyncChunk(int afterUSN, int maxEntries,
      boolean fullSyncOnly) throws EDAMUserException, EDAMSystemException,
      TException {
    return getClient().getSyncChunk(getToken(), afterUSN, maxEntries,
        fullSyncOnly);
  }

  /**
   * @see NoteStore.Client#getFilteredSyncChunk(String, int, int,
   *      com.evernote.edam.notestore.SyncChunkFilter)
   */
  public SyncChunk getFilteredSyncChunk(int afterUSN, int maxEntries,
      SyncChunkFilter filter) throws EDAMUserException, EDAMSystemException,
      TException {
    return getClient().getFilteredSyncChunk(getToken(), afterUSN, maxEntries,
        filter);
  }

  /**
   * @see NoteStore.Client#getLinkedNotebookSyncState(String,
   *      com.evernote.edam.type.LinkedNotebook)
   */
  public SyncState getLinkedNotebookSyncState(LinkedNotebook linkedNotebook)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getLinkedNotebookSyncState(getToken(), linkedNotebook);
  }

  /**
   * @return
   * @see NoteStore.Client#getLinkedNotebookSyncChunk(String,
   *      com.evernote.edam.type.LinkedNotebook, int, int, boolean)
   */
  public SyncChunk getLinkedNotebookSyncChunk(LinkedNotebook linkedNotebook,
      int afterUSN, int maxEntries, boolean fullSyncOnly)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getLinkedNotebookSyncChunk(getToken(), linkedNotebook,
        afterUSN, maxEntries, fullSyncOnly);
  }

  /**
   * @see NoteStore.Client#listNotebooks(String)
   */
  public List<Notebook> listNotebooks() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().listNotebooks(getToken());
  }

  /**
   * @see NoteStore.Client#getNotebook(String, String)
   */
  public Notebook getNotebook(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getNotebook(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getDefaultNotebook(String)
   */
  public Notebook getDefaultNotebook() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().getDefaultNotebook(getToken());
  }

  /**
   * @see NoteStore.Client#createNotebook(String,
   *      com.evernote.edam.type.Notebook)
   */
  public Notebook createNotebook(Notebook notebook) throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().createNotebook(getToken(), notebook);
  }

  /**
   * @see NoteStore.Client#updateNotebook(String,
   *      com.evernote.edam.type.Notebook)
   */
  public int updateNotebook(Notebook notebook) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().updateNotebook(getToken(), notebook);
  }

  /**
   * @see NoteStore.Client#expungeNotebook(String, String)
   */
  public int expungeNotebook(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().expungeNotebook(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#listTags(String)
   */
  public List<Tag> listTags() throws EDAMUserException, EDAMSystemException,
      TException {
    return getClient().listTags(getToken());
  }

  /**
   * @see NoteStore.Client#listTagsByNotebook(String, String)
   */
  public List<Tag> listTagsByNotebook(String notebookGuid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().listTagsByNotebook(getToken(), notebookGuid);
  }

  /**
   * @see NoteStore.Client#getTag(String, String)
   */
  public Tag getTag(String guid) throws EDAMUserException, EDAMSystemException,
      EDAMNotFoundException, TException {
    return getClient().getTag(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#createTag(String, com.evernote.edam.type.Tag)
   */
  public Tag createTag(Tag tag) throws EDAMUserException, EDAMSystemException,
      EDAMNotFoundException, TException {
    return getClient().createTag(getToken(), tag);
  }

  /**
   * @see NoteStore.Client#updateTag(String, com.evernote.edam.type.Tag)
   */
  public int updateTag(Tag tag) throws EDAMUserException, EDAMSystemException,
      EDAMNotFoundException, TException {
    return getClient().updateTag(getToken(), tag);
  }

  /**
   * @see NoteStore.Client#untagAll(String, String)
   */
  public void untagAll(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    getClient().untagAll(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#expungeTag(String, String)
   */
  public int expungeTag(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().expungeTag(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#listSearches(String)
   */
  public List<SavedSearch> listSearches() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().listSearches(getToken());
  }

  /**
   * @see NoteStore.Client#getSearch(String, String)
   */
  public SavedSearch getSearch(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getSearch(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#createSearch(String,
   *      com.evernote.edam.type.SavedSearch)
   */
  public SavedSearch createSearch(SavedSearch search) throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().createSearch(getToken(), search);
  }

  /**
   * @see NoteStore.Client#updateSearch(String,
   *      com.evernote.edam.type.SavedSearch)
   */
  public int updateSearch(SavedSearch search) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().updateSearch(getToken(), search);
  }

  /**
   * @see NoteStore.Client#expungeSearch(String, String)
   */
  public int expungeSearch(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().expungeSearch(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#findNotes(String,
   *      com.evernote.edam.notestore.NoteFilter, int, int)
   */
  public NoteList findNotes(NoteFilter filter, int offset, int maxNotes)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().findNotes(getToken(), filter, offset, maxNotes);
  }

  /**
   * @see NoteStore.Client#findNoteOffset(String,
   *      com.evernote.edam.notestore.NoteFilter, String)
   */
  public int findNoteOffset(NoteFilter filter, String guid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().findNoteOffset(getToken(), filter, guid);
  }

  /**
   * @see NoteStore.Client#findNotesMetadata(String,
   *      com.evernote.edam.notestore.NoteFilter, int, int,
   *      com.evernote.edam.notestore.NotesMetadataResultSpec)
   */
  public NotesMetadataList findNotesMetadata(NoteFilter filter, int offset,
      int maxNotes, NotesMetadataResultSpec resultSpec)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().findNotesMetadata(getToken(), filter, offset, maxNotes,
        resultSpec);
  }

  /**
   * @see NoteStore.Client#findNoteCounts(String,
   *      com.evernote.edam.notestore.NoteFilter, boolean)
   */
  public NoteCollectionCounts findNoteCounts(NoteFilter filter,
      boolean withTrash) throws EDAMUserException, EDAMSystemException,
      EDAMNotFoundException, TException {
    return getClient().findNoteCounts(getToken(), filter, withTrash);
  }

  /**
   * @see NoteStore.Client#getNote(String, String, boolean, boolean, boolean,
   *      boolean)
   */
  public Note getNote(String guid, boolean withContent,
      boolean withResourcesData, boolean withResourcesRecognition,
      boolean withResourcesAlternateData) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient()
        .getNote(getToken(), guid, withContent, withResourcesData,
            withResourcesRecognition, withResourcesAlternateData);
  }

  /**
   * @see NoteStore.Client#getNoteApplicationData(String, String)
   */
  public LazyMap getNoteApplicationData(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getNoteApplicationData(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getNoteApplicationDataEntry(String, String, String)
   */
  public String getNoteApplicationDataEntry(String guid, String key)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getNoteApplicationDataEntry(getToken(), guid, key);
  }

  /**
   * @see NoteStore.Client#setNoteApplicationDataEntry(String, String, String,
   *      String)
   */
  public int setNoteApplicationDataEntry(String guid, String key, String value)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient()
        .setNoteApplicationDataEntry(getToken(), guid, key, value);
  }

  /**
   * @see NoteStore.Client#unsetNoteApplicationDataEntry(String, String, String)
   */
  public int unsetNoteApplicationDataEntry(String guid, String key)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().unsetNoteApplicationDataEntry(getToken(), guid, key);
  }

  /**
   * @see NoteStore.Client#getNoteContent(String, String)
   */
  public String getNoteContent(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getNoteContent(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getNoteSearchText(String, String, boolean, boolean)
   */
  public String getNoteSearchText(String guid, boolean noteOnly,
      boolean tokenizeForIndexing) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getNoteSearchText(getToken(), guid, noteOnly,
        tokenizeForIndexing);
  }

  /**
   * @see NoteStore.Client#getResourceSearchText(String, String)
   */
  public String getResourceSearchText(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getResourceSearchText(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getNoteTagNames(String, String)
   */
  public List<String> getNoteTagNames(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getNoteTagNames(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#createNote(String, com.evernote.edam.type.Note)
   */
  public Note createNote(Note note) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().createNote(getToken(), note);
  }

  /**
   * @see NoteStore.Client#updateNote(String, com.evernote.edam.type.Note)
   */
  public Note updateNote(Note note) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().updateNote(getToken(), note);
  }

  /**
   * @see NoteStore.Client#deleteNote(String, String)
   */
  public int deleteNote(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().deleteNote(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#expungeNote(String, String)
   */
  public int expungeNote(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().expungeNote(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#expungeNotes(String, java.util.List)
   */
  public int expungeNotes(List<String> noteGuids) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().expungeNotes(getToken(), noteGuids);
  }

  /**
   * @see NoteStore.Client#expungeInactiveNotes(String)
   */
  public int expungeInactiveNotes() throws EDAMUserException,
      EDAMSystemException, TException {
    return getClient().expungeInactiveNotes(getToken());
  }

  /**
   * @see NoteStore.Client#copyNote(String, String, String)
   */
  public Note copyNote(String noteGuid, String toNotebookGuid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().copyNote(getToken(), noteGuid, toNotebookGuid);
  }

  /**
   * @see NoteStore.Client#listNoteVersions(String, String)
   */
  public List<NoteVersionId> listNoteVersions(String noteGuid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().listNoteVersions(getToken(), noteGuid);
  }

  /**
   * @see NoteStore.Client#getNoteVersion(String, String, int, boolean, boolean,
   *      boolean)
   */
  public Note getNoteVersion(String noteGuid, int updateSequenceNum,
      boolean withResourcesData, boolean withResourcesRecognition,
      boolean withResourcesAlternateData) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient()
        .getNoteVersion(getToken(), noteGuid, updateSequenceNum,
            withResourcesData, withResourcesRecognition,
            withResourcesAlternateData);
  }

  /**
   * @see NoteStore.Client#getResource(String, String, boolean, boolean,
   *      boolean, boolean)
   */
  public Resource getResource(String guid, boolean withData,
      boolean withRecognition, boolean withAttributes, boolean withAlternateData)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getResource(getToken(), guid, withData, withRecognition,
        withAttributes, withAlternateData);
  }

  /**
   * @see NoteStore.Client#getResourceApplicationData(String, String)
   */
  public LazyMap getResourceApplicationData(String guid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getResourceApplicationData(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getResourceApplicationDataEntry(String, String,
   *      String)
   */
  public String getResourceApplicationDataEntry(String guid, String key)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getResourceApplicationDataEntry(getToken(), guid, key);
  }

  /**
   * @see NoteStore.Client#setResourceApplicationDataEntry(String, String,
   *      String, String)
   */
  public int setResourceApplicationDataEntry(String guid, String key,
      String value) throws EDAMUserException, EDAMSystemException,
      EDAMNotFoundException, TException {
    return getClient().setResourceApplicationDataEntry(getToken(), guid, key,
        value);
  }

  /**
   * @see NoteStore.Client#unsetResourceApplicationDataEntry(String, String,
   *      String)
   */
  public int unsetResourceApplicationDataEntry(String guid, String key)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().unsetResourceApplicationDataEntry(getToken(), guid, key);
  }

  /**
   * @see NoteStore.Client#updateResource(String,
   *      com.evernote.edam.type.Resource)
   */
  public int updateResource(Resource resource) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().updateResource(getToken(), resource);
  }

  /**
   * @see NoteStore.Client#getResourceData(String, String)
   */
  public byte[] getResourceData(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getResourceData(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getResourceByHash(String, String, byte[], boolean,
   *      boolean, boolean)
   */
  public Resource getResourceByHash(String noteGuid, byte[] contentHash,
      boolean withData, boolean withRecognition, boolean withAlternateData)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getResourceByHash(getToken(), noteGuid, contentHash,
        withData, withRecognition, withAlternateData);
  }

  /**
   * @see NoteStore.Client#getResourceRecognition(String, String)
   */
  public byte[] getResourceRecognition(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getResourceRecognition(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getResourceAlternateData(String, String)
   */
  public byte[] getResourceAlternateData(String guid) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getResourceAlternateData(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getResourceAttributes(String, String)
   */
  public ResourceAttributes getResourceAttributes(String guid)
      throws EDAMUserException, EDAMSystemException, EDAMNotFoundException,
      TException {
    return getClient().getResourceAttributes(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#getPublicNotebook(int, String)
   */
  public Notebook getPublicNotebook(int userId, String publicUri)
      throws EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().getPublicNotebook(userId, publicUri);
  }

  /**
   * @see NoteStore.Client#createSharedNotebook(String,
   *      com.evernote.edam.type.SharedNotebook)
   */
  public SharedNotebook createSharedNotebook(SharedNotebook sharedNotebook)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().createSharedNotebook(getToken(), sharedNotebook);
  }

  /**
   * @see NoteStore.Client#updateSharedNotebook(String,
   *      com.evernote.edam.type.SharedNotebook)
   */
  public int updateSharedNotebook(SharedNotebook sharedNotebook)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().updateSharedNotebook(getToken(), sharedNotebook);
  }

  /**
   * @see NoteStore.Client#sendMessageToSharedNotebookMembers(String, String,
   *      String, java.util.List)
   */
  public int sendMessageToSharedNotebookMembers(String notebookGuid,
      String messageText, List<String> recipients) throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().sendMessageToSharedNotebookMembers(getToken(),
        notebookGuid, messageText, recipients);
  }

  /**
   * @see NoteStore.Client#listSharedNotebooks(String)
   */
  public List<SharedNotebook> listSharedNotebooks() throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().listSharedNotebooks(getToken());
  }

  /**
   * @see NoteStore.Client#expungeSharedNotebooks(String, java.util.List)
   */
  public int expungeSharedNotebooks(List<Long> sharedNotebookIds)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().expungeSharedNotebooks(getToken(), sharedNotebookIds);
  }

  /**
   * @see NoteStore.Client#createLinkedNotebook(String,
   *      com.evernote.edam.type.LinkedNotebook)
   */
  public LinkedNotebook createLinkedNotebook(LinkedNotebook linkedNotebook)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().createLinkedNotebook(getToken(), linkedNotebook);
  }

  /**
   * @see NoteStore.Client#updateLinkedNotebook(String,
   *      com.evernote.edam.type.LinkedNotebook)
   */
  public int updateLinkedNotebook(LinkedNotebook linkedNotebook)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().updateLinkedNotebook(getToken(), linkedNotebook);
  }

  /**
   * @see NoteStore.Client#listLinkedNotebooks(String)
   */
  public List<LinkedNotebook> listLinkedNotebooks() throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().listLinkedNotebooks(getToken());
  }

  /**
   * @see NoteStore.Client#expungeLinkedNotebook(String, String)
   */
  public int expungeLinkedNotebook(String guid) throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().expungeLinkedNotebook(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#authenticateToSharedNotebook(String, String)
   */
  public AuthenticationResult authenticateToSharedNotebook(String shareKey)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    return getClient().authenticateToSharedNotebook(shareKey, getToken());
  }

  /**
   * @see NoteStore.Client#getSharedNotebookByAuth(String)
   */
  public SharedNotebook getSharedNotebookByAuth() throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().getSharedNotebookByAuth(getToken());
  }

  /**
   * @see NoteStore.Client#emailNote(String,
   *      com.evernote.edam.notestore.NoteEmailParameters)
   */
  public void emailNote(NoteEmailParameters parameters)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    getClient().emailNote(getToken(), parameters);
  }

  /**
   * @see NoteStore.Client#shareNote(String, String)
   */
  public String shareNote(String guid) throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().shareNote(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#stopSharingNote(String, String)
   */
  public void stopSharingNote(String guid) throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    getClient().stopSharingNote(getToken(), guid);
  }

  /**
   * @see NoteStore.Client#authenticateToSharedNote(String, String)
   */
  public AuthenticationResult authenticateToSharedNote(String guid,
      String noteKey, String authenticationToken) throws EDAMUserException,
      EDAMNotFoundException, EDAMSystemException, TException {
    return getClient().authenticateToSharedNote(guid, noteKey,
        authenticationToken);
  }

  /**
   * @see NoteStore.Client#findRelated(String,
   *      com.evernote.edam.notestore.RelatedQuery,
   *      com.evernote.edam.notestore.RelatedResultSpec)
   */
  public RelatedResult findRelated(RelatedQuery query,
      RelatedResultSpec resultSpec) throws EDAMUserException,
      EDAMSystemException, EDAMNotFoundException, TException {
    return getClient().findRelated(getToken(), query, resultSpec);
  }

  /**
   * @see NoteStore.Client#setSharedNotebookRecipientSettings(String, long,
   *      SharedNotebookRecipientSettings)
   */
  public void setSharedNotebookRecipientSettings(
      final String authenticationToken, final long sharedNotebookId,
      final SharedNotebookRecipientSettings recipientSettings)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    getClient().setSharedNotebookRecipientSettings(authenticationToken,
        sharedNotebookId, recipientSettings);
  }

}
