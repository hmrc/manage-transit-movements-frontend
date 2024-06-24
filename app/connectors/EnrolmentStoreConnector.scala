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
import models.GroupEnrolmentResponse
import models.GroupEnrolmentResponse._
import play.api.Logging
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {

  def checkGroupEnrolments(groupId: String, enrolmentKey: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = url"${config.enrolmentProxyUrl}/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey"
    http
      .get(url)
      .execute[GroupEnrolmentResponse]
      .map {
        case Enrolments(enrolments)         => enrolments.map(_.service).contains(enrolmentKey)
        case NoEnrolments                   => false
        case BadRequest("INVALID_GROUP_ID") => false
        case e =>
          logger.warn(s"[EnrolmentStoreProxyConnector][checkSaGroup] Enrolment Store Proxy error: $e")
          throw new Exception(s"Call to enrolment store failed: $e")
      }
  }
}
