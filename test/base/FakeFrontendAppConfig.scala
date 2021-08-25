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
      "microservice.services.contact-frontend"                        -> "contact-frontend"
    )

    new FrontendAppConfig(Configuration.from(configMapping.toMap))
  }

}
