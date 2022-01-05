/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.CustomHttpReads.rawHttpResponseHttpReads
import javax.inject.Inject
import models.referenceData.CustomsOffice
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends Status {

  def getCustomsOffice(customsOfficeId: String)(implicit hc: HeaderCarrier): Future[Option[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-office/$customsOfficeId"

    /*
      TODO: Alerting - We need to clarify the logging and alerting for the failures below
     */
    http.GET[HttpResponse](serviceUrl).map {
      case response if response.status == OK        => response.json.validate[CustomsOffice].asOpt // TODO: Alerting - we cannot parse the response
      case response if response.status == NOT_FOUND => None
      case _                                        => None // TODO: Alerting - we got a response we didn't expect
    }
  }
}
