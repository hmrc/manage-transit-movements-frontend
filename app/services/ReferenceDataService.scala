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
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataServiceImpl @Inject() (connector: ReferenceDataConnector) extends ReferenceDataService {

  def getCustomsOffice(customsOfficeId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]] = {
    val queryParams: Seq[(String, String)] = Seq("data.id" -> customsOfficeId)
    connector.getCustomsOffices(queryParams).map(_.headOption)
  }

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    lazy val default                       = ControlType(code, "")
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    connector.getControlTypes(queryParams).map(_.headOption.getOrElse(default)).recover {
      case _ => default
    }
  }

  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc] = {
    lazy val default                       = FunctionalErrorWithDesc(code, "")
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    connector.getFunctionalErrors(queryParams).map(_.headOption.getOrElse(default)).recover {
      case _ => default
    }
  }

  def getFunctionalErrors()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[FunctionalErrorWithDesc]] =
    connector.getFunctionalErrors().recover {
      case _ => Seq.empty
    }
}

trait ReferenceDataService {
  def getCustomsOffice(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CustomsOffice]]
  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType]
  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc]
  def getFunctionalErrors()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[FunctionalErrorWithDesc]]
}
