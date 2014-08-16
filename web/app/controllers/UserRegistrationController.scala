package controllers

import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.user.Registration
import scaldi.{Injector, Injectable}
import backend.CustomerBackend
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class UserRegistrationController(implicit inj: Injector) extends Controller with Authentication with Injectable {
  private val customerBackend = inject[CustomerBackend]

  def showForm = UnauthenticatedAction {
    implicit request =>
      Ok(views.html.user.registrationForm(registrationForm))
  }

  def submitForm = UnauthenticatedAction.async {
    implicit require =>
      registrationForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.user.registrationForm(formWithErrors)))
        },
        registration => {
          customerBackend.registerCustomer(registration).map {
            result =>
              Ok(result.toString)
          }
        }
      )
  }

  val registrationForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "passwordRepeat" -> nonEmptyText
    )(Registration.apply)(Registration.unapply).
      verifying("Passwords do not match", registration => registration.password == registration.passwordRepeat)
  )
}
