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

package connectors.testOnly

import akka.util.Helpers.Requiring
import config.FrontendAppConfig
import models.DepartureId
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import models.departureP5._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyP5DepartureMovementsConnector @Inject() (config: FrontendAppConfig, http: HttpClient) {

  def getMessageMetaData(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Messages] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/departures/${departureId.value}/messages"

    http.GET[Messages](serviceUrl)(implicitly, headers, ec)
  }

  def getGoodsUnderControl(path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IE060Data] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}$path"

    http.GET[IE060Data](serviceUrl)(implicitly, headers, ec)
  }
}
