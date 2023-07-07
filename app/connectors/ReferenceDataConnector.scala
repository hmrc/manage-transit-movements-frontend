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
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  private def version2Header = Seq(
    "Accept" -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A]): HttpReads[Option[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[Seq[A]].map(_.headOption).getOrElse {
            throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be parsed")
          }
        case NO_CONTENT =>
          None
        case NOT_FOUND =>
          logger.warn("[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned NOT_FOUND")
          throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be found")
        case other =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid downstream status $other")
          throw new IllegalStateException(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid downstream Status $other")
      }
    }

  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]] = {
    val url = s"${config.customsReferenceDataUrl}/filtered-lists/CustomsOffices"
    val queryParams: Seq[(String, String)] = Seq(
      "data.id" -> code
    )
    http.GET[Option[CustomsOffice]](url = url, headers = version2Header, queryParams = queryParams)
  }

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[ControlType]] = {
    val url = s"${config.customsReferenceDataUrl}/filtered-lists/ControlType"
    val queryParams: Seq[(String, String)] = Seq(
      "data.code" -> code
    )
    http.GET[Option[ControlType]](url = url, headers = version2Header, queryParams = queryParams)
  }

  def getFunctionalErrorDescription(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[FunctionalErrorWithDesc]] = {
    val url = s"${config.customsReferenceDataUrl}/filtered-lists/FunctionalErrorCodesIeCA"
    val queryParams: Seq[(String, String)] = Seq(
      "data.code" -> code
    )
    http.GET[Option[FunctionalErrorWithDesc]](url = url, headers = version2Header, queryParams = queryParams)
  }
}
