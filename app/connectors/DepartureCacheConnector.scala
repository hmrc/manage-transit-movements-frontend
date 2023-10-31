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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureCacheConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClient
)(implicit ec: ExecutionContext)
    extends Logging {

  private val baseUrl = s"${config.departureCacheUrl}"

  def isDeclarationAmendable(lrn: String, xPaths: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$baseUrl/x-paths/$lrn/is-declaration-amendable"

    http.POST[Seq[String], Boolean](url, xPaths)
  }

  def handleErrors(lrn: String, functionalErrors: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$baseUrl/x-paths/$lrn/handle-errors"

    http.POST[Seq[String], Boolean](url, functionalErrors)
  }

  def handleAmendmentErrors(lrn: String, functionalErrors: Seq[String])(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$baseUrl/x-paths/$lrn/handle-amendment-errors"

    http.POST[Seq[String], Boolean](url, functionalErrors)
  }

  def handleGuaranteeRejection(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$baseUrl/x-paths/$lrn/handle-guarantee-errors"

    http.GET[Boolean](url)
  }

  def doesDeclarationExist(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$baseUrl/does-cache-exists-for-lrn/$lrn"

    http.GET[Boolean](url)
  }
}
