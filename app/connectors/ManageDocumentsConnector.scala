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

import config.{FrontendAppConfig, PhaseConfig}
import logging.Logging
import play.api.http.HeaderNames.ACCEPT
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageDocumentsConnector @Inject() (
  appConfig: FrontendAppConfig,
  phaseConfig: PhaseConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {

  def getTAD(departureId: String, messageId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url: URL = url"${appConfig.manageDocumentsUrl}/$departureId/transit-accompanying-document/$messageId"
    http
      .get(url)
      .setHeader(ACCEPT -> phaseConfig.manageDocumentsAcceptHeader)
      .stream
  }

  def getUnloadingPermission(arrivalId: String, messageId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url: URL = url"${appConfig.manageDocumentsUrl}/$arrivalId/unloading-permission-document/$messageId"
    http.get(url).stream
  }
}
