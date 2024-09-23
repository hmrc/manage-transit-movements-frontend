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
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, InvalidGuaranteeReason, RequestedDocumentType}
import models.{Country, IdentificationType, IncidentCode, Nationality, QualifierOfIdentification}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataServiceImpl @Inject() (connector: ReferenceDataConnector) extends ReferenceDataService {

  def getCustomsOffice(customsOfficeId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, CustomsOffice]] = {
    val queryParams: (String, String) = "data.id" -> customsOfficeId
    connector
      .getCustomsOffices(queryParams)
      .map(
        x => Right(x.head)
      )
      .recover {
        case _: NoReferenceDataFoundException => Left(customsOfficeId)
      }
  }

  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Country]] = {
    val queryParams: (String, String) = "data.code" -> code
    connector
      .getCountries(queryParams)
      .map(
        countries => Right(countries.head)
      )
      .recover {
        case _: NoReferenceDataFoundException => Left(code)
      }
  }

  def getIdentificationType(`type`: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, IdentificationType]] = {
    val queryParams: (String, String) = "data.type" -> `type`
    connector
      .getIdentificationTypes(queryParams)
      .map(
        types => Right(types.head)
      )
      .recover {
        case _: NoReferenceDataFoundException => Left(`type`)
      }
  }

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Nationality]] = {
    val queryParams: (String, String) = "data.code" -> code
    connector
      .getNationalities(queryParams)
      .map(
        nationalities => Right(nationalities.head)
      )
      .recover {
        case _: NoReferenceDataFoundException => Left(code)
      }
  }

  def getQualifierOfIdentification(qualifier: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, QualifierOfIdentification]] = {
    val queryParams: (String, String) = "data.qualifier" -> qualifier
    connector
      .getQualifierOfIdentifications(queryParams)
      .map(
        identifications => Right(identifications.head)
      )
      .recover {
        case _: NoReferenceDataFoundException => Left(qualifier)
      }
  }

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getControlTypes(queryParams).map(_.head)
  }

  def getIncidentCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentCode] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getIncidentCodes(queryParams).map(_.head)
  }

  def getRequestedDocumentType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[RequestedDocumentType] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getRequestedDocumentTypes(queryParams).map(_.head)
  }

  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getFunctionalErrors(queryParams).map(_.head)
  }

  def getFunctionalErrors()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[FunctionalErrorWithDesc]] =
    connector.getFunctionalErrors().map(_.toSeq).recover {
      case _ => Seq.empty
    }

  def getInvalidGuaranteeReason(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InvalidGuaranteeReason] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getInvalidGuaranteeReasons(queryParams).map(_.head)
  }

  def getInvalidGuaranteeReasons()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[InvalidGuaranteeReason]] =
    connector.getInvalidGuaranteeReasons().map(_.toSeq).recover {
      case _ => Seq.empty
    }

}

trait ReferenceDataService {
  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, CustomsOffice]]

  def getQualifierOfIdentification(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, QualifierOfIdentification]]
  def getCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Country]]

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, Nationality]]
  def getIdentificationType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, IdentificationType]]
  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType]
  def getIncidentCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentCode]
  def getRequestedDocumentType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[RequestedDocumentType]
  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc]
  def getFunctionalErrors()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[FunctionalErrorWithDesc]]
  def getInvalidGuaranteeReason(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InvalidGuaranteeReason]
  def getInvalidGuaranteeReasons()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[InvalidGuaranteeReason]]
}
