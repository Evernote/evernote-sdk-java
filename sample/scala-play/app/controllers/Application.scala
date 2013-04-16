package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._

object Application extends Controller {

  /**
   * Home page
   */
  def index = Action { implicit request =>
    Ok(html.index()).withSession(session)
  }

  /**
   * Clear session
   */
  def clear = Action { implicit request =>
    Ok(html.index()).withNewSession
  }

}
