/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import models.Enrolment.{LegacyEnrolment, NewEnrolment}
import models.{ArrivalId, DepartureId}
import play.api.Configuration
import play.api.i18n.Messages
import play.api.mvc.Request

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val phase4Enabled: Boolean = configuration.get[Boolean]("microservice.services.features.isPhase4Enabled")
  val phase5Enabled: Boolean = configuration.get[Boolean]("microservice.services.features.isPhase5Enabled")

  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  lazy val customsReferenceDataUrl: String = configuration.get[Service]("microservice.services.customs-reference-data").fullServiceUrl

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  def declareUnloadingRemarksUrl(arrivalId: ArrivalId) = s"$p4Unloading/${arrivalId.index}"

  val p4Arrival: String      = configuration.get[String]("urls.declareTransitMovementArrivalFrontend")
  val p4ArrivalStart: String = s"$p4Arrival/movement-reference-number"

  val p5Arrival: String = configuration.get[String]("urls.manageTransitMovementsArrivalFrontend")

  val p4Departure: String      = configuration.get[String]("urls.declareTransitMovementDepartureFrontend")
  val p4DepartureStart: String = s"$p4Departure/local-reference-number"

  val p5Departure: String = configuration.get[String]("urls.manageTransitMovementsDepartureFrontend")

  val p4Cancellation: String = configuration.get[String]("urls.declareTransitMovementCancellationFrontend")

  val p5Cancellation: String = configuration.get[String]("urls.manageTransitMovementsCancellationFrontend")

  val p4Unloading: String = configuration.get[String]("urls.declareTransitMovementUnloadingFrontend")

  val p5Unloading: String = configuration.get[String]("urls.manageTransitMovementsUnloadingFrontend")

  def arrivalFrontendRejectedUrl(arrivalId: ArrivalId)  = s"$p4Arrival/${arrivalId.index}/arrival-rejection"
  def unloadingRemarksRejectedUrl(arrivalId: ArrivalId) = s"$p4Unloading/${arrivalId.index}/unloading-rejection"

  private val guaranteeBalanceUrlBase    = configuration.get[String]("urls.guaranteeBalanceFrontend")
  def checkGuaranteeBalanceUrl           = s"$guaranteeBalanceUrlBase/start?referral=ncts"
  val isGuaranteeBalanceEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.isGuaranteeBalanceEnabled")

  lazy val loginUrl: String                          = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String                  = configuration.get[String]("urls.loginContinue")
  lazy val eccEnrolmentSplashPage: String            = configuration.get[String]("urls.eccEnrolmentSplashPage")
  lazy val enrolmentGuidancePage: String             = configuration.get[String]("urls.enrolnentGuidance")
  lazy val departureBaseUrl: String                  = configuration.get[Service]("microservice.services.departure").baseUrl
  lazy val departureUrl: String                      = configuration.get[Service]("microservice.services.departure").fullServiceUrl
  lazy val testSupportUrl: String                    = configuration.get[Service]("microservice.services.test-support").baseUrl
  lazy val destinationBaseUrl: String                = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val destinationUrl: String                    = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val routerUrl: String                         = configuration.get[Service]("microservice.services.testOnly-router").fullServiceUrl
  lazy val enrolmentProxyUrl: String                 = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl
  lazy val newEnrolment: NewEnrolment                = configuration.get[NewEnrolment]("enrolments.new")
  lazy val legacyEnrolment: LegacyEnrolment          = configuration.get[LegacyEnrolment]("enrolments.legacy")
  lazy val manageService: String                     = configuration.get[String]("appName")
  lazy val commonTransitConventionTradersUrl: String = configuration.get[Service]("microservice.services.common-transit-convention-traders").fullServiceUrl
  lazy val transitMovementsUrl: String               = configuration.get[Service]("microservice.services.transit-movements").fullServiceUrl

  lazy val departureCacheUrl: String = configuration.get[Service]("microservice.services.manage-transit-movements-departure-cache").fullServiceUrl

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val nctsHelpdeskUrl: String  = configuration.get[String]("urls.nctsHelpdesk")
  lazy val timeoutSeconds: Int      = configuration.get[Int]("session.timeoutSeconds")
  lazy val countdownSeconds: Int    = configuration.get[Int]("session.countdownSeconds")

  val presentationNotificationFrontend: String = configuration.get[String]("urls.presentationNotificationFrontend")

  lazy val manageDocumentsUrl: String = configuration.get[Service]("microservice.services.transit-movements-trader-manage-documents").fullServiceUrl

  def departureFrontendTaskListUrl(lrn: String)                          = s"$p5Departure/$lrn/declaration-summary"
  def departureNewLocalReferenceNumberUrl(lrn: String)                   = s"$p5Departure/$lrn/new-local-reference-number"
  def departureAmendErrorsUrl(lrn: String, departureId: String)          = s"$p5Departure/$lrn/amend-errors/$departureId"
  def departureAmendGuaranteeErrorsUrl(lrn: String, departureId: String) = s"$p5Departure/$lrn/amend-guarantee-errors/$departureId"
  def departureFrontendRejectedUrl(departureId: DepartureId)             = s"$p4Departure/${departureId.index}/guarantee-rejection"
  def departureFrontendDeclarationFailUrl(departureId: DepartureId)      = s"$p5Departure/${departureId.index}/departure-declaration-fail"
  def presentationNotificationFrontendUrl(departureId: String)           = s"$presentationNotificationFrontend/$departureId"

  def departureFrontendCancellationDecisionUrl(departureId: DepartureId): String =
    s"$p4Departure/${departureId.index}/cancellation-decision-update"

  def departureFrontendConfirmCancellationUrl(departureId: DepartureId): String =
    s"$p4Cancellation/${departureId.index}/confirm-cancellation"

  val isTraderTest: Boolean = configuration.get[Boolean]("trader-test.enabled")
  val feedbackEmail: String = configuration.get[String]("trader-test.feedback.email")
  val feedbackForm: String  = configuration.get[String]("trader-test.feedback.link")

  def mailto(implicit request: Request[_], messages: Messages): String = {
    val subject = messages("site.email.subject")
    val body = {
      val newLine      = "%0D%0A"
      val newParagraph = s"$newLine$newLine"
      s"""
         |URL: ${request.uri}$newParagraph
         |Tell us how we can help you here.$newParagraph
         |Give us a brief description of the issue or question, including details like…$newLine
         | - The screens where you experienced the issue$newLine
         | - What you were trying to do at the time$newLine
         | - The information you entered$newParagraph
         |Please include your name and phone number and we’ll get in touch.
         |""".stripMargin
    }

    s"mailto:$feedbackEmail?subject=$subject&body=$body"
  }
}
