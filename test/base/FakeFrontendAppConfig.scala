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

package base

import play.api.Configuration
import config.FrontendAppConfig

object FakeFrontendAppConfig {

  def apply(configMapping: (String, Any)*) = {
    val default = Map(
      "microservice.services.tracking-consent-frontend.url"           -> "tracking-consent-frontend.url",
      "microservice.services.tracking-consent-frontend.gtm.container" -> "tracking-consent-frontend.gtm.container",
      "google-analytics.token"                                        -> "google-analytics.token",
      "google-analytics.host"                                         -> "google-analytics.host",
      "microservice.services.contact-frontend" -> Map(
        "host"     -> "microservice.services.contact-frontend.host",
        "port"     -> "microservice.services.contact-frontend.port",
        "protocol" -> "microservice.services.contact-frontend.protocol",
        "startUrl" -> "microservice.services.contact-frontend.startUrl"
      ),
      "urls" -> Map(
        "logoutContinue"                             -> "urls.logoutContinue",
        "feedback"                                   -> "urls.feedback",
        "declareTransitMovementUnloadingFrontend"    -> "http://localhost:9488/manage-transit-movements-unloading-remarks",
        "declareTransitMovementArrivalFrontend"      -> "http://localhost:9483/manage-transit-movements-arrivals",
        "declareTransitMovementDepartureFrontend"    -> "http://localhost:9489/manage-transit-movements-departures",
        "declareTransitMovementCancellationFrontend" -> "urls.declareTransitMovementCancellationFrontend",
        "nctsEnquiries"                              -> "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries",
        "eccEnrolmentSplashPage"                     -> "https://www.gov.uk/placeholder-for-ecc-splash-page",
        "login"                                      -> "urls.login",
        "loginContinue"                              -> "urls.loginContinue"
      ),
      "session" -> Map("timeoutSeconds" -> "1", "countdownSeconds" -> "2"),
      "keys" -> Map(
        "enrolmentKey"           -> "HMRC-CTC-ORG",
        "enrolmentIdentifierKey" -> "EORINumber",
        "legacy" -> Map(
          "enrolmentKey"           -> "HMCE-NCTS-ORG",
          "enrolmentIdentifierKey" -> "VATRegNoTURN"
        )
      )
    )

    new FrontendAppConfig(Configuration.from(default ++ configMapping.toMap))
  }

}
