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
import models.{ArrivalId, DepartureId}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val phase5Enabled: Boolean = configuration.get[Boolean](s"microservice.services.features.isPhase5Enabled")

  val unloadingFrontendUrl            = getFrontendUrl("Unloading")
  val arrivalFrontendUrl: String      = getFrontendUrl("Arrival")
  val departureFrontendUrl: String    = getFrontendUrl("Departure")
  val cancellationFrontendUrl: String = getFrontendUrl("Cancellation")

  private lazy val contactHost: String     = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "CTCTraders"

  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  private val host: String = configuration.get[String]("host")

  lazy val customsReferenceDataUrl: String = configuration.get[Service]("microservice.services.customs-reference-data").fullServiceUrl

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  def declareUnloadingRemarksUrl(arrivalId: ArrivalId) = s"$unloadingFrontendUrl/${arrivalId.index}"

  val declareArrivalNotificationStartUrl: String =
    if (phase5Enabled) {
      arrivalFrontendUrl
    } else {
      s"$arrivalFrontendUrl/movement-reference-number"
    }

  val startDepartureDeclarationStartUrl: String =
    if (phase5Enabled) {
      departureFrontendUrl
    } else {
      s"$departureFrontendUrl/local-reference-number"
    }

  def arrivalFrontendRejectedUrl(arrivalId: ArrivalId)  = s"$arrivalFrontendUrl/${arrivalId.index}/arrival-rejection"
  def unloadingRemarksRejectedUrl(arrivalId: ArrivalId) = s"$unloadingFrontendUrl/${arrivalId.index}/unloading-rejection"

  private val guaranteeBalanceUrlBase    = configuration.get[String]("urls.guaranteeBalanceFrontend")
  def checkGuaranteeBalanceUrl           = s"$guaranteeBalanceUrlBase/start?referral=ncts"
  val isGuaranteeBalanceEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.isGuaranteeBalanceEnabled")

  lazy val loginUrl: String                          = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String                  = configuration.get[String]("urls.loginContinue")
  lazy val eccEnrolmentSplashPage: String            = configuration.get[String]("urls.eccEnrolmentSplashPage")
  lazy val departureBaseUrl: String                  = configuration.get[Service]("microservice.services.departure").baseUrl
  lazy val departureUrl: String                      = configuration.get[Service]("microservice.services.departure").fullServiceUrl
  lazy val testSupportUrl: String                    = configuration.get[Service]("microservice.services.test-support").baseUrl
  lazy val draftDeparturesUrl: String                = configuration.get[Service]("microservice.services.drafts-repository").fullServiceUrl
  lazy val destinationBaseUrl: String                = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val destinationUrl: String                    = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val routerUrl: String                         = configuration.get[Service]("microservice.services.testOnly-router").fullServiceUrl
  lazy val enrolmentProxyUrl: String                 = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl
  lazy val legacyEnrolmentKey: String                = configuration.get[String]("keys.legacy.enrolmentKey")
  lazy val legacyEnrolmentIdentifierKey: String      = configuration.get[String]("keys.legacy.enrolmentIdentifierKey")
  lazy val newEnrolmentKey: String                   = configuration.get[String]("keys.enrolmentKey")
  lazy val newEnrolmentIdentifierKey: String         = configuration.get[String]("keys.enrolmentIdentifierKey")
  lazy val manageService: String                     = configuration.get[String]("appName")
  lazy val commonTransitConventionTradersUrl: String = configuration.get[Service]("microservice.services.common-transit-convention-traders").fullServiceUrl
  lazy val transitMovementsUrl: String               = configuration.get[Service]("microservice.services.transit-movements").fullServiceUrl

  lazy val departureCacheUrl: String = configuration.get[Service]("microservice.services.manage-transit-movements-departure-cache").fullServiceUrl

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val nctsHelpdeskUrl: String  = configuration.get[String]("urls.nctsHelpdesk")
  lazy val timeoutSeconds: Int      = configuration.get[Int]("session.timeoutSeconds")
  lazy val countdownSeconds: Int    = configuration.get[Int]("session.countdownSeconds")

  val manageTransitMovementsUnloadingFrontend: String    = configuration.get[String]("urls.manageTransitMovementsUnloadingFrontend")
  val manageTransitMovementsCancellationFrontend: String = configuration.get[String]("urls.manageTransitMovementsCancellationFrontend")

  lazy val manageDocumentsUrl: String = configuration.get[Service]("microservice.services.manage-documents").fullServiceUrl

  val declareDepartureStartWithLRNUrl: String =
    if (phase5Enabled) {
      departureFrontendUrl
    } else {
      s"$departureFrontendUrl/local-reference-number"
    }

  def departureFrontendTaskListUrl(lrn: String)                     = s"$departureFrontendUrl/$lrn/declaration-summary"
  def departureNewLocalReferenceNumberUrl(lrn: String)              = s"$departureFrontendUrl/$lrn/new-local-reference-number"
  def departureFrontendRejectedUrl(departureId: DepartureId)        = s"$departureFrontendUrl/${departureId.index}/guarantee-rejection"
  def departureFrontendDeclarationFailUrl(departureId: DepartureId) = s"$departureFrontendUrl/${departureId.index}/departure-declaration-fail"

  def departureFrontendCancellationDecisionUrl(departureId: DepartureId): String =
    if (phase5Enabled) {
      s"$cancellationFrontendUrl/${departureId.index}/cancellation-decision-update"
    } else {
      s"$departureFrontendUrl/${departureId.index}/cancellation-decision-update"
    }

  def departureTadPdfUrl(departureId: DepartureId) = s"$departureFrontendUrl/${departureId.index}/tad-pdf"

  def departureFrontendConfirmCancellationUrl(departureId: DepartureId): String =
    if (phase5Enabled) {
      s"$cancellationFrontendUrl/${departureId.index}"
    } else {
      s"$cancellationFrontendUrl/${departureId.index}/confirm-cancellation"
    }

  private def getFrontendUrl(name: String): String = {
    val url = if (phase5Enabled) {
      s"manageTransitMovements${name}Frontend"
    } else {
      s"declareTransitMovement${name}Frontend"
    }
    configuration.get[String](s"urls.$url")
  }
}
