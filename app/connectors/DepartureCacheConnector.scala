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
import generated.FunctionalErrorType04
import models.{FunctionalError, FunctionalErrors}
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.departureP5.Rejection
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureCacheConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {

  private val baseUrl = config.departureCacheUrl

  def isRejectionAmendable[T <: Rejection](lrn: String, rejection: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[Boolean] = {
    val url = url"$baseUrl/user-answers/$lrn/amendable"
    http
      .post(url)
      .withBody(Json.toJson(rejection))
      .execute[Boolean]
  }

  def handleErrors[T <: Rejection](lrn: String, rejection: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[HttpResponse] = {
    val url = url"$baseUrl/user-answers/$lrn/errors"
    http
      .post(url)
      .withBody(Json.toJson(rejection))
      .execute[HttpResponse]
  }

  def prepareForAmendment(lrn: String, departureId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"$baseUrl/user-answers/$lrn"
    http
      .patch(url)
      .withBody(Json.toJson(departureId))
      .execute[HttpResponse]
  }

  def convertErrors(errors: Seq[FunctionalErrorType04])(implicit hc: HeaderCarrier): Future[FunctionalErrorsWithSection] = {
    import models.FunctionalError.writes

    val url = url"$baseUrl/messages/rejection"
    http
      .post(url)
      .withBody(Json.toJson(errors))
      .execute[FunctionalErrorsWithSection]
  }
}
