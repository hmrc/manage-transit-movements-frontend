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
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureCacheConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {

  private val baseUrl = s"${config.departureCacheUrl}"

  // TODO - should all of these methods return Booleans?

  def isDeclarationAmendable(lrn: String, xPaths: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/x-paths/$lrn/is-declaration-amendable"

    http
      .post(url)
      .withBody(Json.toJson(xPaths))
      .execute[Boolean]
  }

  def handleErrors(lrn: String, functionalErrors: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/x-paths/$lrn/handle-errors"

    http
      .post(url)
      .withBody(Json.toJson(functionalErrors))
      .execute[Boolean]
  }

  def handleAmendmentErrors(lrn: String, functionalErrors: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/x-paths/$lrn/handle-amendment-errors"

    http
      .post(url)
      .withBody(Json.toJson(functionalErrors))
      .execute[Boolean]
  }

  def handleGuaranteeRejection(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/x-paths/$lrn/handle-guarantee-errors"

    http
      .get(url)
      .execute[Boolean]
  }

  def doesDeclarationExist(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"$baseUrl/does-cache-exists-for-lrn/$lrn"

    http
      .get(url)
      .execute[Boolean]
  }
}
