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

package services

import connectors.ManageDocumentsConnector
import play.api.http.HeaderNames._
import play.api.http.HttpEntity
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageDocumentsService @Inject() (connector: ManageDocumentsConnector)(implicit ec: ExecutionContext) {

  def getTAD(departureId: String, messageId: String)(implicit hc: HeaderCarrier): Future[Option[HttpEntity.Streamed]] =
    connector.getTAD(departureId, messageId).map(stream)

  def getUnloadingPermission(arrivalId: String, messageId: String)(implicit hc: HeaderCarrier): Future[Option[HttpEntity.Streamed]] =
    connector.getUnloadingPermission(arrivalId, messageId).map(stream)

  private def stream(response: HttpResponse): Option[HttpEntity.Streamed] =
    response.status match {
      case OK =>
        def header(key: String): Option[String] =
          response.headers.get(key).flatMap(_.headOption)

        val contentLength = header(CONTENT_LENGTH).flatMap(_.toLongOption)
        val contentType   = header(CONTENT_TYPE)

        Some(HttpEntity.Streamed(response.bodyAsSource, contentLength, contentType))
      case _ =>
        None
    }

}
