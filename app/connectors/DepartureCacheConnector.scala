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
import models.departureP5.Rejection
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
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

  def isDeclarationAmendable(lrn: String, xPaths: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/user-answers/$lrn/is-amendable"

    http
      .post(url)
      .withBody(Json.toJson(xPaths))
      .execute[Boolean]
  }

  def doesDeclarationExist(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/user-answers/$lrn"

    http
      .get(url)
      .execute[HttpResponse]
      .map(_.status)
      .map {
        case OK        => true
        case NOT_FOUND => false
        case x         => throw new Exception(s"[DepartureCacheConnector][doesDeclarationExist] returned $x")
      }
  }

  def handleErrors(lrn: String, rejection: Rejection)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
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
}
