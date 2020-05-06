/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "play26frontend"

  val analyticsToken: String         = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String          = configuration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl       = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl         = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl                = s"$contactHost/contact/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  val signOutUrl: String             = configuration.get[String]("urls.logout")

  private val declareArrivalNotificationRoute    = configuration.get[String]("declare-transit-movement-arrival-frontend.host")
  private val declareArrivalNotificationStartUrl = configuration.get[String]("declare-transit-movement-arrival-frontend.startUrl")
  val declareArrivalNotificationUrl              = s"$declareArrivalNotificationRoute/$declareArrivalNotificationStartUrl"

  private val declareUnloadingRemarksRoute         = configuration.get[String]("declare-transit-movement-unloading-frontend.host")
  private val declareUnloadingRemarksStartUrl      = configuration.get[String]("declare-transit-movement-unloading-frontend.startUrl")
  val declareUnloadingRemarksUrl: String => String = mrn => s"$declareUnloadingRemarksRoute/$declareUnloadingRemarksStartUrl/$mrn/unloading-guidance"

  lazy val authUrl: String          = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String         = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val destinationUrl: String   = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val referenceDataUrl: String = configuration.get[Service]("microservice.services.reference-data").baseUrl
  lazy val enrolmentKey: String     = configuration.get[String]("keys.enrolmentKey")

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val loginHmrcService: String = configuration.get[String]("urls.loginHmrcService")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)
}
