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
    lrn: String,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    businessRejectionType match {
      case AmendmentRejection =>
        cacheConnector.doesDeclarationExist(lrn)
      case DeclarationRejection =>
        cacheConnector.isDeclarationAmendable(lrn, xPaths)
    }

  def handleErrors(
    businessRejectionType: BusinessRejectionType,
    lrn: String,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    businessRejectionType match {
      case AmendmentRejection =>
        cacheConnector.handleAmendmentErrors(lrn, xPaths)
      case DeclarationRejection =>
        if (xPaths.nonEmpty) {
          cacheConnector.handleErrors(lrn, xPaths)
        } else {
          Future.successful(false)
        }
    }

  def nextPage(
    businessRejectionType: BusinessRejectionType,
    lrn: String,
    departureId: String,
    mrn: Option[String]
  ): String =
    businessRejectionType match {
      case AmendmentRejection =>
        config.departureAmendmentUrl(lrn, departureId)
      case DeclarationRejection =>
        if (mrn.isDefined) {
          config.departureNewLocalReferenceNumberUrl(lrn)
        } else {
          config.departureFrontendTaskListUrl(lrn)
        }
    }
}
