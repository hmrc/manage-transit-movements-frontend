/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import models.QueryGroupsEnrolmentsResponseModel
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(http: HttpClient, servicesConfig: ServicesConfig, implicit val ec: ExecutionContext) extends Logging {

  private def host: String = servicesConfig.baseUrl("enrolment-store-proxy")

  def checkGroupEnrolments(groupId: String, enrolmentKey: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val serviceUrl = s"$host/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey"
    http.GET[HttpResponse](serviceUrl).map {
      response =>
        response.status match {
          case OK         => response.json.as[QueryGroupsEnrolmentsResponseModel].enrolments.exists(_.service.contains(enrolmentKey))
          case NO_CONTENT => false
          case other =>
            logger.info(s"[EnrolmentStoreProxyConnector][checkSaGroup] Enrolment Store Proxy error with status $other")
            false
        }
    } recover {
      case exception =>
        logger.info("[EnrolmentStoreProxyConnector][checkSaGroup] Enrolment Store Proxy error", exception)
        false
    }
  }
}
