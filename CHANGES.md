Evernote SDK for Java Changelog
-------------------------------

* 1.23 - December 7, 2012
    * Organizational changes
        * Moved to a Maven build process
        * Published a public artifact, com.evernote.evernote-api, to the Maven Central Repository
        * Removed libthrift.jar dependency
        * Repackaged Thrift runtime code from org.apache.thrift to com.evernote.thrift and included it in the SDK JAR file
        * Modified the Thrift Java code generator to significantly reduce the number of classes and lines of code in generated API wrappers.
    * API changes
        * Added support for xAuth authentication via [UserStore.authenticateLongSession](http://dev.evernote.com/documentation/reference/UserStore.html#Fn_UserStore_authenticateLongSession)
        * Added support for Evernote Business
            * Added [UserStore.authenticateToBusiness](http://dev.evernote.com/documentation/reference/UserStore.html#Fn_UserStore_authenticateToBusiness)
            * Added BusinessNotebook and contact to [Notebook](http://dev.evernote.com/documentation/reference/Types.html#Struct_Notebook)
            * Added businessId, businessName and businessRole to [User.accounting](http://dev.evernote.com/documentation/reference/Types.html#Struct_Accounting)
        * Added NoteFilter to [RelatedQuery](http://dev.evernote.com/documentation/reference/NoteStore.html#Struct_RelatedQuery) to allow relatedness searches to be filtered
        * Changed the way that sharing permissions are represented on a [SharedNotebook](http://dev.evernote.com/documentation/reference/Types.html#Struct_SharedNotebook)
            * Deprecated notebookModifiable and requireLogin
            * Added privilege and allowPreview
        * Added NotebookRestrictions to [Notebook](http://dev.evernote.com/documentation/reference/Types.html#Struct_Notebook) to allow clients to more easily determined the operations that they can perform in a given shared notebook.
        * Moved [PremiumInfo](http://dev.evernote.com/documentation/reference/Types.html#Struct_PremiumInfo) and [SponsoredGroupRole](http://dev.evernote.com/documentation/reference/Types.html#Enum_SponsoredGroupRole) from the userstore package to the types package.
        * Removed all advertising functions and structures
        * Removed the previously deprecated NoteStore.getAccountSize function