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
import models.referenceData.{ControlType, CustomsOffice}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-office/$code"
    http
      .GET[CustomsOffice](serviceUrl)
      .map(Some(_))
      .recover {
        case _ =>
          logger.error(s"Get Customs Office request failed to return data")
          None
      }
  }

  def getControlTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Seq[ControlType]]] = {
    val serviceUrl = s"${config.referenceDataUrl}/control-types"
    http
      .GET[Seq[ControlType]](serviceUrl)
      .map(Some(_))
      .recover {
        case _ =>
          println(s"$serviceUrl")
          logger.error(s"Get Control Types  request failed to return data")
          None
      }
  }

}
