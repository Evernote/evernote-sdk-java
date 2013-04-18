package controllers

import play.api._
import play.api.libs.oauth._
import play.api.mvc._
import play.api.Logger

import com.evernote.thrift.protocol.TBinaryProtocol
import com.evernote.thrift.transport.THttpClient
import com.evernote.thrift.transport.TTransportException

import com.evernote.edam.`type`._
import com.evernote.edam.userstore._
import com.evernote.edam.notestore._

import scala.collection.JavaConversions._

object Evernote extends Controller {

  val KEY = ConsumerKey("YOUR CONSUMER KEY", "YOUR CONSUMER SECRET")

  val EVERNOTE = OAuth(ServiceInfo(
    "https://sandbox.evernote.com/oauth",
    "https://sandbox.evernote.com/oauth",
    "https://sandbox.evernote.com/OAuth.action", KEY),
    false)

  val CALLBACK_URL = "http://localhost:9000/auth"
  val USER_STORE_URL = "https://sandbox.evernote.com/edam/user"

  def authenticate = Action { request =>
    request.queryString.get("oauth_verifier").flatMap(_.headOption).map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      EVERNOTE.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          val userStoreTrans: THttpClient = new THttpClient(USER_STORE_URL)
          val userStoreProt: TBinaryProtocol = new TBinaryProtocol(userStoreTrans)
          val userStore: UserStore.Client = new UserStore.Client(userStoreProt, userStoreProt)
          val noteStoreUrl: String = userStore.getNoteStoreUrl(t.token)
          val noteStoreTrans: THttpClient = new THttpClient(noteStoreUrl)
          val noteStoreProt: TBinaryProtocol = new TBinaryProtocol(noteStoreTrans)
          val noteStore: NoteStore.Client = new NoteStore.Client(noteStoreProt, noteStoreProt)
          val notebooks: String = noteStore.listNotebooks(t.token).map(_.getName).mkString(",")
          // We received the authorized tokens in the OAuth object - store it before we proceed
          Redirect(routes.Application.index).withSession(
            "token" -> t.token,
            "secret" -> t.secret,
            "noteStoreUrl" -> noteStoreUrl,
            "notebooks" -> notebooks
          )
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      EVERNOTE.retrieveRequestToken(CALLBACK_URL) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(EVERNOTE.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      })
  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }
}
