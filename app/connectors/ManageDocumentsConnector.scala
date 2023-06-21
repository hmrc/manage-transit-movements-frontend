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

package connectors

import config.FrontendAppConfig
import logging.Logging
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames => HMRCHeaderNames}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageDocumentsConnector @Inject() (config: FrontendAppConfig, wsClient: WSClient)(implicit ec: ExecutionContext) extends Logging {

  def getTAD(departureId: String, messageId: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {

    val serviceUrl: String = s"${config.manageDocumentsUrl}/$departureId/transit-accompanying-document/$messageId"
    val headers            = hc.headers(HMRCHeaderNames.explicitlyIncludedHeaders)

    wsClient.url(serviceUrl).withHttpHeaders(headers: _*).get()
  }

  def getUnloadingPermission(messageId: String, arrivalId: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {

    val serviceUrl: String = s"${config.manageDocumentsUrl}/$arrivalId/unloading-permission-document/$messageId"
    val headers            = hc.headers(HMRCHeaderNames.explicitlyIncludedHeaders)

    wsClient.url(serviceUrl).withHttpHeaders(headers: _*).get()
  }

}
