/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.DepartureCacheConnector
import models.departureP5.BusinessRejectionType.*
import models.departureP5.Rejection
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.Future

class AmendmentService @Inject() (
  cacheConnector: DepartureCacheConnector,
  config: FrontendAppConfig
) {

  def isRejectionAmendable[T <: Rejection](
    lrn: String,
    rejection: T
  )(implicit hc: HeaderCarrier, writes: Writes[T]): Future[Boolean] =
    cacheConnector.isRejectionAmendable(lrn, rejection)

  def handleErrors[T <: Rejection](lrn: String, rejection: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[HttpResponse] =
    cacheConnector.handleErrors(lrn, rejection)

  def prepareForAmendment(lrn: String, departureId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    cacheConnector.prepareForAmendment(lrn, departureId)

  def nextPage(
    businessRejectionType: DepartureBusinessRejectionType,
    lrn: String,
    mrn: Option[String]
  ): String =
    businessRejectionType match {
      case DeclarationRejection if mrn.isDefined =>
        config.departureNewLocalReferenceNumberUrl(lrn)
      case _ =>
        config.departureFrontendTaskListUrl(lrn)
    }

}
