/*
  Evernote API sample code, structured as a simple command line
  application that demonstrates several API calls.
  
  To compile (Unix):
    javac -classpath ../../target/evernote-api-*.jar EDAMDemo.java
 
  To run:
    java -classpath ../../target/evernote-api-*.jar EDAMDemo
 
  Full documentation of the Evernote API can be found at 
  http://dev.evernote.com/documentation/cloud/
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.transport.TTransportException;

public class EDAMDemo {

  /***************************************************************************
   * You must change the following values before running this sample code *
   ***************************************************************************/

  // Real applications authenticate with Evernote using OAuth, but for the
  // purpose of exploring the API, you can get a developer token that allows
  // you to access your own Evernote account. To get a developer token, visit
  // https://sandbox.evernote.com/api/DeveloperToken.action
  private static final String AUTH_TOKEN = "your developer token";

  /***************************************************************************
   * You shouldn't need to change anything below here to run sample code *
   ***************************************************************************/

  private UserStoreClient userStore;
  private NoteStoreClient noteStore;
  private String newNoteGuid;

  /**
   * Console entry point.
   */
  public static void main(String args[]) throws Exception {
    String token = System.getenv("AUTH_TOKEN");
    if (token == null) {
      token = AUTH_TOKEN;
    }
    if ("your developer token".equals(token)) {
      System.err.println("Please fill in your developer token");
      System.err
          .println("To get a developer token, go to https://sandbox.evernote.com/api/DeveloperToken.action");
      return;
    }

    EDAMDemo demo = new EDAMDemo(token);
    try {
      demo.listNotes();
      demo.createNote();
      demo.searchNotes();
      demo.updateNoteTag();
    } catch (EDAMUserException e) {
      // These are the most common error types that you'll need to
      // handle
      // EDAMUserException is thrown when an API call fails because a
      // paramter was invalid.
      if (e.getErrorCode() == EDAMErrorCode.AUTH_EXPIRED) {
        System.err.println("Your authentication token is expired!");
      } else if (e.getErrorCode() == EDAMErrorCode.INVALID_AUTH) {
        System.err.println("Your authentication token is invalid!");
      } else if (e.getErrorCode() == EDAMErrorCode.QUOTA_REACHED) {
        System.err.println("Your authentication token is invalid!");
      } else {
        System.err.println("Error: " + e.getErrorCode().toString()
            + " parameter: " + e.getParameter());
      }
    } catch (EDAMSystemException e) {
      System.err.println("System error: " + e.getErrorCode().toString());
    } catch (TTransportException t) {
      System.err.println("Networking error: " + t.getMessage());
    }
  }

  /**
   * Intialize UserStore and NoteStore clients. During this step, we
   * authenticate with the Evernote web service. All of this code is boilerplate
   * - you can copy it straight into your application.
   */
  public EDAMDemo(String token) throws Exception {
    // Set up the UserStore client and check that we can speak to the server
    EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.SANDBOX, token);
    ClientFactory factory = new ClientFactory(evernoteAuth);
    userStore = factory.createUserStoreClient();

    boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
    if (!versionOk) {
      System.err.println("Incompatible Evernote client protocol version");
      System.exit(1);
    }

    // Set up the NoteStore client
    noteStore = factory.createNoteStoreClient();
  }

  /**
   * Retrieve and display a list of the user's notes.
   */
  private void listNotes() throws Exception {
    // List the notes in the user's account
    System.out.println("Listing notes:");

    // First, get a list of all notebooks
    List<Notebook> notebooks = noteStore.listNotebooks();

    for (Notebook notebook : notebooks) {
      System.out.println("Notebook: " + notebook.getName());

      // Next, search for the first 100 notes in this notebook, ordering
      // by creation date
      NoteFilter filter = new NoteFilter();
      filter.setNotebookGuid(notebook.getGuid());
      filter.setOrder(NoteSortOrder.CREATED.getValue());
      filter.setAscending(true);

      NoteList noteList = noteStore.findNotes(filter, 0, 100);
      List<Note> notes = noteList.getNotes();
      for (Note note : notes) {
        System.out.println(" * " + note.getTitle());
      }
    }
    System.out.println();
  }

  /**
   * Create a new note containing a little text and the Evernote icon.
   */
  private void createNote() throws Exception {
    // To create a new note, simply create a new Note object and fill in
    // attributes such as the note's title.
    Note note = new Note();
    note.setTitle("Test note from EDAMDemo.java");

    String fileName = "enlogo.png";
    String mimeType = "image/png";

    // To include an attachment such as an image in a note, first create a
    // Resource
    // for the attachment. At a minimum, the Resource contains the binary
    // attachment
    // data, an MD5 hash of the binary data, and the attachment MIME type.
    // It can also
    // include attributes such as filename and location.
    Resource resource = new Resource();
    resource.setData(readFileAsData(fileName));
    resource.setMime(mimeType);
    ResourceAttributes attributes = new ResourceAttributes();
    attributes.setFileName(fileName);
    resource.setAttributes(attributes);

    // Now, add the new Resource to the note's list of resources
    note.addToResources(resource);

    // To display the Resource as part of the note's content, include an
    // <en-media>
    // tag in the note's ENML content. The en-media tag identifies the
    // corresponding
    // Resource using the MD5 hash.
    String hashHex = bytesToHex(resource.getData().getBodyHash());

    // The content of an Evernote note is represented using Evernote Markup
    // Language
    // (ENML). The full ENML specification can be found in the Evernote API
    // Overview
    // at http://dev.evernote.com/documentation/cloud/chapters/ENML.php
    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
        + "<en-note>"
        + "<span style=\"color:green;\">Here's the Evernote logo:</span><br/>"
        + "<en-media type=\"image/png\" hash=\"" + hashHex + "\"/>"
        + "</en-note>";
    note.setContent(content);

    // Finally, send the new note to Evernote using the createNote method
    // The new Note object that is returned will contain server-generated
    // attributes such as the new note's unique GUID.
    Note createdNote = noteStore.createNote(note);
    newNoteGuid = createdNote.getGuid();

    System.out.println("Successfully created a new note with GUID: "
        + newNoteGuid);
    System.out.println();
  }

  /**
   * Search a user's notes and display the results.
   */
  private void searchNotes() throws Exception {
    // Searches are formatted according to the Evernote search grammar.
    // Learn more at
    // http://dev.evernote.com/documentation/cloud/chapters/Searching_notes.php

    // In this example, we search for notes that have the term "EDAMDemo" in
    // the title.
    // This should return the sample note that we created in this demo app.
    String query = "intitle:EDAMDemo";

    // To search for notes with a specific tag, we could do something like
    // this:
    // String query = "tag:tagname";

    // To search for all notes with the word "elephant" anywhere in them:
    // String query = "elephant";

    NoteFilter filter = new NoteFilter();
    filter.setWords(query);
    filter.setOrder(NoteSortOrder.UPDATED.getValue());
    filter.setAscending(false);

    // Find the first 50 notes matching the search
    System.out.println("Searching for notes matching query: " + query);
    NoteList notes = noteStore.findNotes(filter, 0, 50);
    System.out.println("Found " + notes.getTotalNotes() + " matching notes");

    Iterator<Note> iter = notes.getNotesIterator();
    while (iter.hasNext()) {
      Note note = iter.next();
      System.out.println("Note: " + note.getTitle());

      // Note objects returned by findNotes() only contain note attributes
      // such as title, GUID, creation date, update date, etc. The note
      // content
      // and binary resource data are omitted, although resource metadata
      // is included.
      // To get the note content and/or binary resources, call getNote()
      // using the note's GUID.
      Note fullNote = noteStore.getNote(note.getGuid(), true, true, false,
          false);
      System.out.println("Note contains " + fullNote.getResourcesSize()
          + " resources");
      System.out.println();
    }
  }

  /**
   * Update the tags assigned to a note. This method demonstrates how only
   * modified fields need to be sent in calls to updateNote.
   */
  private void updateNoteTag() throws Exception {
    // When updating a note, it is only necessary to send Evernote the
    // fields that have changed. For example, if the Note that you
    // send via updateNote does not have the resources field set, the
    // Evernote server will not change the note's existing resources.
    // If you wanted to remove all resources from a note, you would
    // set the resources field to a new List<Resource> that is empty.

    // If you are only changing attributes such as the note's title or tags,
    // you can save time and bandwidth by omitting the note content and
    // resources.

    // In this sample code, we fetch the note that we created earlier,
    // including
    // the full note content and all resources. A real application might
    // do something with the note, then update a note attribute such as a
    // tag.
    Note note = noteStore.getNote(newNoteGuid, true, true, false, false);

    // Do something with the note contents or resources...

    // Now, update the note. Because we're not changing them, we unset
    // the content and resources. All we want to change is the tags.
    note.unsetContent();
    note.unsetResources();

    // We want to apply the tag "TestTag"
    note.addToTagNames("TestTag");

    // Now update the note. Because we haven't set the content or resources,
    // they won't be changed.
    noteStore.updateNote(note);
    System.out.println("Successfully added tag to existing note");

    // To prove that we didn't destroy the note, let's fetch it again and
    // verify that it still has 1 resource.
    note = noteStore.getNote(newNoteGuid, false, false, false, false);
    System.out.println("After update, note has " + note.getResourcesSize()
        + " resource(s)");
    System.out.println("After update, note tags are: ");
    for (String tagGuid : note.getTagGuids()) {
      Tag tag = noteStore.getTag(tagGuid);
      System.out.println("* " + tag.getName());
    }

    System.out.println();
  }

  /**
   * Helper method to read the contents of a file on disk and create a new Data
   * object.
   */
  private static Data readFileAsData(String fileName) throws Exception {
    String filePath = new File(EDAMDemo.class.getResource(
        EDAMDemo.class.getCanonicalName() + ".class").getPath()).getParent()
        + File.separator + fileName;
    // Read the full binary contents of the file
    FileInputStream in = new FileInputStream(filePath);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byte[] block = new byte[10240];
    int len;
    while ((len = in.read(block)) >= 0) {
      byteOut.write(block, 0, len);
    }
    in.close();
    byte[] body = byteOut.toByteArray();

    // Create a new Data object to contain the file contents
    Data data = new Data();
    data.setSize(body.length);
    data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
    data.setBody(body);

    return data;
  }

  /**
   * Helper method to convert a byte array to a hexadecimal string.
   */
  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte hashByte : bytes) {
      int intVal = 0xff & hashByte;
      if (intVal < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(intVal));
    }
    return sb.toString();
  }
}
