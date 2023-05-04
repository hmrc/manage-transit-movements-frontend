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
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._

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

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    def onFailControlType(code: String): ControlType = ControlType(code, "")
    val serviceUrl                                   = s"${config.referenceDataUrl}/control-type/$code"

    http
      .GET[Option[ControlType]](serviceUrl)
      .map(_.getOrElse(onFailControlType(code)))
      .recover {
        case _ =>
          logger.error(s"Get Control Types  request failed to return data")
          onFailControlType(code)
      }
  }

  def getFunctionalErrorDescription(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc] = {
    def onFailFunctionalError(code: String): FunctionalErrorWithDesc = FunctionalErrorWithDesc(code, "")
    val serviceUrl                                                   = s"${config.referenceDataUrl}/functional-error-type/$code"

    http
      .GET[Option[FunctionalErrorWithDesc]](serviceUrl)
      .map(_.getOrElse(onFailFunctionalError(code)))
      .recover {
        case _ =>
          logger.error(s"Get Functional Error Type  request failed to return data")
          onFailFunctionalError(code)
      }
  }
}
