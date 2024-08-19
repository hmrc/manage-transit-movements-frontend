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

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, InvalidGuaranteeReason, RequestedDocumentType}
import models.{Country, IdentificationType, IncidentCode, Nationality, QualifierOfIdentification}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  private type QueryParams = (String, String)

  def getCustomsOffices(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/CustomsOffices"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[CustomsOffice]]
  }

  def getCountries(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/CountryCodesFullList"
    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .setHeader(version2Header)
      .execute[NonEmptySet[Country]]
  }

  def getQualifierOfIdentifications(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[QualifierOfIdentification]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/QualifierOfTheIdentification"
    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .setHeader(version2Header)
      .execute[NonEmptySet[QualifierOfIdentification]]
  }

  def getIdentificationTypes(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[IdentificationType]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .setHeader(version2Header)
      .execute[NonEmptySet[IdentificationType]]
  }

  def getNationalities(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Nationality]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/Nationality"
    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .setHeader(version2Header)
      .execute[NonEmptySet[Nationality]]
  }

  def getIncidentCodes(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[IncidentCode]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/IncidentCode"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[IncidentCode]]
  }

  def getControlTypes(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[ControlType]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/ControlType"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[ControlType]]
  }

  def getRequestedDocumentTypes(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[RequestedDocumentType]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/RequestedDocumentType"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[RequestedDocumentType]]
  }

  def getFunctionalErrors(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[FunctionalErrorWithDesc]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/FunctionalErrorCodesIeCA"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[FunctionalErrorWithDesc]]
  }

  def getInvalidGuaranteeReasons(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[InvalidGuaranteeReason]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/InvalidGuaranteeReason"
    http
      .get(url)
      .setHeader(version2Header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[NonEmptySet[InvalidGuaranteeReason]]
  }

  private def version2Header: (String, String) =
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail: _*)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
    }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
