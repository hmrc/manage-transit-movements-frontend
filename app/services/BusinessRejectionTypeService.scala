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
import models.LocalReferenceNumber
import models.departureP5.BusinessRejectionType
import models.departureP5.BusinessRejectionType._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class BusinessRejectionTypeService @Inject() (
  cacheConnector: DepartureCacheConnector,
  config: FrontendAppConfig
) {

  def canProceedWithAmendment(
    businessRejectionType: BusinessRejectionType,
    lrn: LocalReferenceNumber,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    businessRejectionType match {
      case AmendmentRejection =>
        cacheConnector.doesDeclarationExist(lrn.value)
      case DeclarationRejection =>
        cacheConnector.isDeclarationAmendable(lrn.value, xPaths)
    }

  def handleErrors(
    businessRejectionType: BusinessRejectionType,
    lrn: LocalReferenceNumber,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    businessRejectionType match {
      case AmendmentRejection =>
        cacheConnector.handleAmendmentErrors(lrn.value, xPaths)
      case DeclarationRejection =>
        if (xPaths.nonEmpty) {
          cacheConnector.handleErrors(lrn.value, xPaths)
        } else {
          Future.successful(false)
        }
    }

  def nextPage(
    businessRejectionType: BusinessRejectionType,
    lrn: LocalReferenceNumber,
    departureId: String,
    mrn: Option[String]
  ): String =
    businessRejectionType match {
      case AmendmentRejection =>
        config.departureAmendmentUrl(lrn.value, departureId)
      case DeclarationRejection =>
        if (mrn.isDefined) {
          config.departureNewLocalReferenceNumberUrl(lrn.value)
        } else {
          config.departureFrontendTaskListUrl(lrn.value)
        }
    }
}
