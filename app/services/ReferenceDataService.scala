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
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, RequestedDocumentType}
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

  def getCustomsOfficeByCode(customsOfficeCode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice] = {
    val queryParams: (String, String) = "data.id" -> customsOfficeCode
    connector.getCustomsOffices(queryParams).map(_.head)
  }

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    val queryParams: (String, String) = "data.code" -> code
    connector.getControlTypes(queryParams).map(_.head)
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
}

trait ReferenceDataService {
  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[String, CustomsOffice]]
  def getCustomsOfficeByCode(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CustomsOffice]
  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType]
  def getRequestedDocumentType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[RequestedDocumentType]
  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc]
  def getFunctionalErrors()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[FunctionalErrorWithDesc]]
}
