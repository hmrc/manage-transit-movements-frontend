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

package services

import com.google.inject.Inject
import connectors.ReferenceDataConnector
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, InvalidGuaranteeReason, RequestedDocumentType}
import models.{Country, IdentificationType, IncidentCode, Nationality, QualifierOfIdentification}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataService @Inject() (connector: ReferenceDataConnector) {

  def getCustomsOffice(customsOfficeId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice] =
    connector
      .getCustomsOffice(customsOfficeId)
      .map(_.resolve())

  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] =
    connector
      .getCountry(code)
      .map(_.resolve())

  def getIdentificationType(`type`: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IdentificationType] =
    connector
      .getIdentificationType(`type`)
      .map(_.resolve())

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Nationality] =
    connector
      .getNationality(code)
      .map(_.resolve())

  def getQualifierOfIdentification(qualifier: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[QualifierOfIdentification] =
    connector
      .getQualifierOfIdentification(qualifier)
      .map(_.resolve())

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getControlType(queryParams).map(_.resolve())
  }

  def getIncidentCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentCode] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getIncidentCode(queryParams).map(_.resolve())
  }

  def getRequestedDocumentType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[RequestedDocumentType] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getRequestedDocumentType(queryParams).map(_.resolve())
  }

  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getFunctionalError(queryParams).map(_.resolve())
  }

  def getInvalidGuaranteeReason(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InvalidGuaranteeReason] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getInvalidGuaranteeReason(queryParams).map(_.resolve())
  }
}
