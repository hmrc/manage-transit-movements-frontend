/*
 * Copyright 2021 HM Revenue & Customs
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

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val contactHost: String        = configuration.get[Service]("microservice.services.contact-frontend").baseUrl
  lazy val contactFrontendUrl: String = configuration.get[Service]("microservice.services.contact-frontend").fullServiceUrl
  val contactFormServiceIdentifier    = "CTCTraders"

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val showPhaseBanner: Boolean        = configuration.get[Boolean]("banners.showPhase")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  val analyticsToken: String         = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String          = configuration.get[String](s"google-analytics.host")
  val betaFeedbackUrl                = s"$contactFrontendUrl/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactFrontendUrl/beta-feedback-unauthenticated"
  val signOutUrl: String             = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  private val declareUnloadingRemarksUrlBase            = configuration.get[String]("urls.declareTransitMovementUnloadingFrontend")
  def declareUnloadingRemarksUrl(arrivalId: ArrivalId)  = s"$declareUnloadingRemarksUrlBase/${arrivalId.index}"
  private val declareArrivalNotificationUrlBase: String = configuration.get[String]("urls.declareTransitMovementArrivalFrontend")
  val declareArrivalNotificationStartUrl: String        = s"$declareArrivalNotificationUrlBase/movement-reference-number"

  def arrivalFrontendRejectedUrl(arrivalId: ArrivalId)  = s"$declareArrivalNotificationUrlBase/${arrivalId.index}/arrival-rejection"
  def unloadingRemarksRejectedUrl(arrivalId: ArrivalId) = s"$declareUnloadingRemarksUrlBase/${arrivalId.index}/unloading-rejection"

  private val guaranteeBalanceUrlBase    = configuration.get[String]("urls.guaranteeBalanceFrontend")
  def checkGuaranteeBalanceUrl           = s"$guaranteeBalanceUrlBase/start"
  val isGuaranteeBalanceEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.isGuaranteeBalanceEnabled")

  lazy val authUrl: String                      = configuration.get[Service]("auth").fullServiceUrl
  lazy val loginUrl: String                     = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String             = configuration.get[String]("urls.loginContinue")
  lazy val eccEnrolmentSplashPage: String       = configuration.get[String]("urls.eccEnrolmentSplashPage")
  lazy val departureBaseUrl: String             = configuration.get[Service]("microservice.services.departure").baseUrl
  lazy val departureUrl: String                 = configuration.get[Service]("microservice.services.departure").fullServiceUrl
  lazy val destinationBaseUrl: String           = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val destinationUrl: String               = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val referenceDataUrl: String             = configuration.get[Service]("microservice.services.reference-data").fullServiceUrl
  lazy val routerUrl: String                    = configuration.get[Service]("microservice.services.testOnly-router").fullServiceUrl
  lazy val enrolmentProxyUrl: String            = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl
  lazy val legacyEnrolmentKey: String           = configuration.get[String]("keys.legacy.enrolmentKey")
  lazy val legacyEnrolmentIdentifierKey: String = configuration.get[String]("keys.legacy.enrolmentIdentifierKey")
  lazy val newEnrolmentKey: String              = configuration.get[String]("keys.enrolmentKey")
  lazy val newEnrolmentIdentifierKey: String    = configuration.get[String]("keys.enrolmentIdentifierKey")
  lazy val manageService: String                = configuration.get[String]("appName")

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val loginHmrcService: String = configuration.get[String]("urls.loginHmrcService")
  lazy val timeoutSeconds: String   = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String = configuration.get[String]("session.countdownSeconds")

  private val departureFrontendUrl: String    = configuration.get[String]("urls.declareTransitMovementDepartureFrontend")
  private val cancellationFrontendUrl: String = configuration.get[String]("urls.declareTransitMovementCancellationFrontend")

  val declareDepartureStartWithLRNUrl: String                            = s"$departureFrontendUrl/local-reference-number"
  def departureFrontendRejectedUrl(departureId: DepartureId)             = s"$departureFrontendUrl/${departureId.index}/guarantee-rejection"
  def departureFrontendDeclarationFailUrl(departureId: DepartureId)      = s"$departureFrontendUrl/${departureId.index}/departure-declaration-fail"
  def departureFrontendCancellationDecisionUrl(departureId: DepartureId) = s"$departureFrontendUrl/${departureId.index}/cancellation-decision-update"
  def departureTadPdfUrl(departureId: DepartureId)                       = s"$departureFrontendUrl/${departureId.index}/tad-pdf"
  def departureFrontendConfirmCancellationUrl(departureId: DepartureId)  = s"$cancellationFrontendUrl/${departureId.index}/confirm-cancellation"

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")
}
