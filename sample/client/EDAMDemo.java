/*
  Evernote API sample code, structured as a simple command line
  application that demonstrates several API calls.
  
  Before running this sample, you must change the API consumer key
  and consumer secret to the values that you received from Evernote.
  
  To compile (Unix):
    javac -classpath .:../../lib/libthrift.jar:../../lib/log4j-1.2.14.jar:../../lib/evernote-api-*.jar EDAMDemo.java
 
  To run:
     java -classpath .:../../lib/libthrift.jar:../../lib/log4j-1.2.14.jar:../../lib/evernote-api-*.jar EDAMDemo myuser mypass
 
  Full documentation of the Evernote API can be found at 
  http://dev.evernote.com/documentation/cloud/
 */

import java.util.*;
import java.io.*;
import java.security.MessageDigest;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;

import com.evernote.edam.type.*;
import com.evernote.edam.userstore.*;
import com.evernote.edam.error.*;
import com.evernote.edam.userstore.Constants;
import com.evernote.edam.notestore.*;

public class EDAMDemo {
  
  // NOTE: Provide the consumer key and consumer secret that you received from Evernote
  private static final String consumerKey = "";
  private static final String consumerSecret = "";
  
  // Once you have completed your development on our sandbox server, we will 
  // activate your API key on our production servers. To use the production servers, 
  // simply change "sandbox.evernote.com" to "www.evernote.com".
  private static final String evernoteHost = "sandbox.evernote.com";
  private static final String userStoreUrl = "https://" + evernoteHost + "/edam/user";

  // Change the User Agent to a string that describes your application, using 
  // the form company name/app name and version. Using a unique user agent string 
  // allows us to identify applications in our logs and provide you with better support. 
  private static final String userAgent = "Evernote/EDAMDemo (Java) " + 
                                          Constants.EDAM_VERSION_MAJOR + "." + 
                                          Constants.EDAM_VERSION_MINOR;

  private UserStore.Client userStore;
  private NoteStore.Client noteStore;
  private String authToken;
  private String newNoteGuid;

  /**
   * Console entry point.
   */
  public static void main(String args[]) 
    throws Exception 
  {
    if (args.length < 2) {
      System.err.println("Arguments:  <username> <password>");
      return;
    }
    if (consumerKey.length() == 0 || consumerSecret.length() == 0) {
      System.err.println("Please set your API consumer key and secret");
      System.err.println("To get an API key, visit http://dev.evernote.com/documentation/cloud/");
      return;
    }
    
    EDAMDemo demo = new EDAMDemo();
    
    // Connect and authenticate to Evernote
    if (demo.intitialize(args[0], args[1])) {
      
      // Test some functionality
      demo.listNotes();
      demo.createNote();
      demo.searchNotes();
      demo.updateNoteTag();
    }
  }
  
  /**
   * Intialize UserStore and NoteStore clients. During this step, we authenticate
   * with the Evernote web service.
   */
  private boolean intitialize(String username, String password) 
    throws Exception
  {
    // Set up the UserStore client. The Evernote UserStore allows you to
    // authenticate a user and access some information about their account.
    THttpClient userStoreTrans = new THttpClient(userStoreUrl);
    userStoreTrans.setCustomHeader("User-Agent", userAgent);
    TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
    userStore = new UserStore.Client(userStoreProt, userStoreProt);
    
    // Check that we can talk to the server
    boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
    if (!versionOk) {
      System.err.println("Incomatible EDAM client protocol version");
      return false;
    }

    // Authenticate using username & password
    AuthenticationResult authResult = null;
    try {
      authResult = userStore.authenticate(username, password, consumerKey, consumerSecret);
    } catch (EDAMUserException ex) {
      // Note that the error handling here is far more detailed than you would 
      // provide to a real user. It is intended to give you an idea of why the 
      // sample application isn't able to authenticate to our servers.
      
      // Any time that you contact us about a problem with an Evernote API, 
      // please provide us with the exception parameter and errorcode. 
      String parameter = ex.getParameter();
      EDAMErrorCode errorCode = ex.getErrorCode();
      
      System.err.println("Authentication failed (parameter: " + parameter + " errorCode: " + errorCode + ")");
      
      if (errorCode == EDAMErrorCode.INVALID_AUTH) {
        if (parameter.equals("consumerKey")) {
          if (consumerKey.equals("en-edamtest")) {
            System.err.println("You must replace the variables consumerKey and consumerSecret with the values you received from Evernote.");
          } else {
            System.err.println("Your consumer key was not accepted by " + evernoteHost);
            System.err.println("This sample client application requires a client API key. If you requested a web service API key, you must authenticate using OAuth as shown in sample/java/oauth");
          }
          System.err.println("If you do not have an API Key from Evernote, you can request one from http://dev.evernote.com/documentation/cloud/");
        } else if (parameter.equals("username")) {
          System.err.println("You must authenticate using a username and password from " + evernoteHost);
          if (evernoteHost.equals("www.evernote.com") == false) {
            System.err.println("Note that your production Evernote account will not work on " + evernoteHost + ",");
            System.err.println("you must register for a separate test account at https://" + evernoteHost + "/Registration.action");
          }
        } else if (parameter.equals("password")) {
          System.err.println("The password that you entered is incorrect");
        }
      }

      return false;
    }
    
    // The result of a succesful authentication is an opaque authentication token
    // that you will use in all subsequent API calls. If you are developing a
    // web application that authenticates using OAuth, the OAuth access token
    // that you receive would be used as the authToken in subsquent calls.
    authToken = authResult.getAuthenticationToken();

    // The Evernote NoteStore allows you to accessa user's notes.    
    // In order to access the NoteStore for a given user, you need to know the 
    // logical "shard" that their notes are stored on. The shard ID is included 
    // in the URL used to access the NoteStore.
    User user = authResult.getUser();
    String shardId = user.getShardId();
    
    System.out.println("Successfully authenticated as " + user.getUsername());
    
    // Set up the NoteStore client 
    THttpClient noteStoreTrans = new THttpClient(authResult.getNoteStoreUrl());
    noteStoreTrans.setCustomHeader("User-Agent", userAgent);
    TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
    noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
    
    return true;
  }
  
  /**
   * Retrieve and display a list of the user's notes.
   */
  private void listNotes() 
    throws Exception 
  {    
    // List all of the notes in the user's account
    System.out.println("Listing all notes:");

    // First, get a list of all notebooks
    List<Notebook> notebooks = noteStore.listNotebooks(authToken);
    
    for (Notebook notebook : notebooks) {
      System.out.println("Notebook: " + notebook.getName());
      
      // Next, search for the first 100 notes in this notebook, ordering by creation date
      NoteFilter filter = new NoteFilter();
      filter.setNotebookGuid(notebook.getGuid());
      filter.setOrder(NoteSortOrder.CREATED.getValue());
      filter.setAscending(true);
      
      NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
      List<Note> notes = noteList.getNotes();
      for (Note note : notes) {
        System.out.println(" * " + note.getTitle());
      }
    }
    System.out.println();
  }
  
  /**
   * Create a new note containing a little text and the Evernote app icon.
   */
  private void createNote()
    throws Exception
  {
    // To create a new note, simply create a new Note object and fill in 
    // attributes such as the note's title.
    Note note = new Note();
    note.setTitle("Test note from EDAMDemo.java");

    String fileName = "enlogo.png";
    String mimeType = "image/png"; 
    
    // To include an attachment such as an image in a note, first create a Resource
    // for the attachment. At a minimum, the Resource contains the binary attachment 
    // data, an MD5 hash of the binary data, and the attachment MIME type. It can also 
    // include attributes such as filename and location.
    Resource resource = new Resource();
    resource.setData(readFileAsData(fileName));
    resource.setMime(mimeType);
    ResourceAttributes attributes = new ResourceAttributes();
    attributes.setFileName(fileName);
    resource.setAttributes(attributes);

    // Now, add the new Resource to the note's list of resources
    note.addToResources(resource);

    // To display the Resource as part of the note's content, include an <en-media>
    // tag in the note's ENML content. The en-media tag identifies the corresponding
    // Resource using the MD5 hash.
    String hashHex = bytesToHex(resource.getData().getBodyHash());
    
    // The content of an Evernote note is represented using Evernote Markup Language
    // (ENML). The full ENML specification can be found in the Evernote API Overview
    // at http://www.evernote.com/about/developer/api/evernote-api.htm.
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
    Note createdNote = noteStore.createNote(authToken, note);
    newNoteGuid = createdNote.getGuid();
    
    System.out.println("Successfully created a new note with GUID: " + newNoteGuid);
    System.out.println();
  }

  /**
   * Search a user's notes and display the results.
   */
  private void searchNotes() 
    throws Exception
  {
    // Searches against the Evernote API are formatted according to the Evernote 
    // search grammar which is defined in Appendix C of the Evernote API Overview at
    // http://www.evernote.com/about/developer/api/evernote-api.htm
    
    // In this example, we search for notes that have the term "EDAMDemo" in the title.
    // This should return the sample note that we created in this demo app.
    String query = "intitle:EDAMDemo";
    
    // To search for notes with a specific tag, we could do something like this:
    // String query = "tag:tagname";
    
    NoteFilter filter = new NoteFilter();
    filter.setWords(query);
    filter.setOrder(NoteSortOrder.UPDATED.getValue());
    filter.setAscending(false);
        
    // Find the first 100 notes matching the search
    System.out.println("Searching for notes matching query: " + query);
    NoteList notes = noteStore.findNotes(authToken, filter, 0, 100);
    System.out.println("Found " + notes.getTotalNotes() + " matching notes");
    
    Iterator<Note> iter = notes.getNotesIterator();
    while (iter.hasNext()) {
      Note note = iter.next();
      System.out.println("Note: " + note.getTitle());
      
      // Note objects returned by findNotes() only contain note attributes
      // such as title, GUID, creation date, update date, etc. The note content 
      // and binary resource data are omitted, although resource metadata is included. 
      // To get the note content and/or binary resources, call getNote() using the note's GUID.
      Note fullNote = noteStore.getNote(authToken, note.getGuid(), true, true, false, false);
      System.out.println("Note contains " + fullNote.getResourcesSize() + " resources");
      System.out.println();
    }
  }

  /**
   * Update the tags assigned to a note. This method demonstrates 
   * how only modified fields need to be sent in calls to updateNote.
   */
  private void updateNoteTag() 
    throws Exception 
  {
    // When updating a note, it is only necessary to send Evernote the
    // fields that have changed. For example, if the Note that you 
    // send via updateNote does not have the resources field set, the
    // Evernote server will not change the note's existing resources.
    // If you wanted to remove all resources from a note, you would
    // set the resources field to a new List<Resource> that is empty.
    
    // If you are only changing attributes such as the note's title or tags, 
    // you can save time and bandwidth by omitting the note content and resources.
    
    // In this sample code, we fetch the note that we created earlier, including
    // the full note content and all resources. A real application might
    // do something with the notes, then update a note attribute such as 
    // a tag.
    Note note = noteStore.getNote(authToken, newNoteGuid, true, true, false, false);
    
    // Do something with the note contents or resources...
 
    // Now, update the note. Because we're not changing them, we unset 
    // the content and resources. All we want to change is the tags.
    note.unsetContent();
    note.unsetResources();
    
    // We want to apply the tag "TestTag"
    note.addToTagNames("TestTag");
    
    // Now update the note. Because we haven't set the content or resources,
    // they won't be changed.
    noteStore.updateNote(authToken, note);
    System.out.println("Successfully added tag to existing note");
    
    // To prove that we didn't destory the note, let's fetch it again and
    // verify that it still has 1 resource.
    note = noteStore.getNote(authToken, newNoteGuid, false, false, false, false);
    System.out.println("After update, note has " + note.getResourcesSize() + " resource(s)");
    System.out.println("After update, note tags are: ");
    for (String tagGuid : note.getTagGuids()) {
      Tag tag = noteStore.getTag(authToken, tagGuid);
      System.out.println("* " + tag.getName());
    }
    
    System.out.println();
  }

  /**
   * Helper method to read the contents of a file on disk and create a new Data object.
   */
  private static Data readFileAsData(String fileName) throws Exception {

    // Read the full binary contents of the file
    FileInputStream in = new FileInputStream(fileName);
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
