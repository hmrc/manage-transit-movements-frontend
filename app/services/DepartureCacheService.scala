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

import connectors.DepartureCacheConnector
import generated.CC056CType
import models.departureP5.BusinessRejectionType
import models.departureP5.BusinessRejectionType._
import models.{LocalReferenceNumber, RichCC056CType}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class DepartureCacheService @Inject() (cacheConnector: DepartureCacheConnector) {

  def canProceedWithAmendment(
    ie056: CC056CType,
    lrn: LocalReferenceNumber
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    BusinessRejectionType(ie056) match {
      case AmendmentRejection =>
        cacheConnector.doesDeclarationExist(lrn.value)
      case DeclarationRejection =>
        isDeclarationAmendable(lrn, ie056.xPaths)
    }

  def isDeclarationAmendable(
    lrn: LocalReferenceNumber,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    cacheConnector.isDeclarationAmendable(lrn.value, xPaths)

  def handleErrors(
    lrn: LocalReferenceNumber,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    cacheConnector.handleErrors(lrn.value, xPaths)

  def handleAmendmentErrors(
    lrn: LocalReferenceNumber,
    xPaths: Seq[String]
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    cacheConnector.handleAmendmentErrors(lrn.value, xPaths)
}
