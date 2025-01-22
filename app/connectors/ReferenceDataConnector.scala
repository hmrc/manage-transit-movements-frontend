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
import play.api.cache.AsyncCacheApi
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2, cache: AsyncCacheApi) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[NonEmptySet[T]]): Future[NonEmptySet[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> "application/vnd.hmrc.2.0+json")
      .execute[NonEmptySet[T]]

  // https://www.playframework.com/documentation/2.6.x/ScalaCache#Accessing-the-Cache-API
  private def getOrElseUpdate[T: ClassTag](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[NonEmptySet[T]]): Future[T] =
    cache.getOrElseUpdate[T](url.toString, config.asyncCacheApiExpiration.seconds) {
      get[T](url).map(_.head)
    }

  private type QueryParams = (String, String)

  def getCustomsOffices(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/CustomsOffices?$queryParams"
    get[CustomsOffice](url)
  }

  def getCountries(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/CountryCodesFullList?$queryParams"
    get[Country](url)
  }

  def getQualifierOfIdentifications(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[QualifierOfIdentification]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/QualifierOfTheIdentification?$queryParams"
    get[QualifierOfIdentification](url)
  }

  def getIdentificationTypes(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[IdentificationType]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport?$queryParams"
    get[IdentificationType](url)
  }

  def getNationalities(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Nationality]] = {
    val url = url"${config.customsReferenceDataUrl}/lists/Nationality?$queryParams"
    get[Nationality](url)
  }

  def getIncidentCode(
    queryParams: QueryParams*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentCode] = {
    val url = url"${config.customsReferenceDataUrl}/lists/IncidentCode?$queryParams"
    getOrElseUpdate[IncidentCode](url)
  }

  def getControlType(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ControlType] = {
    val url = url"${config.customsReferenceDataUrl}/lists/ControlType?$queryParams"
    getOrElseUpdate[ControlType](url)
  }

  def getRequestedDocumentType(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[RequestedDocumentType] = {
    val url = url"${config.customsReferenceDataUrl}/lists/RequestedDocumentType?$queryParams"
    getOrElseUpdate[RequestedDocumentType](url)
  }

  def getFunctionalError(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FunctionalErrorWithDesc] = {
    val url = url"${config.customsReferenceDataUrl}/lists/FunctionalErrorCodesIeCA?$queryParams"
    getOrElseUpdate[FunctionalErrorWithDesc](url)
  }

  def getInvalidGuaranteeReason(queryParams: QueryParams*)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InvalidGuaranteeReason] = {
    val url = url"${config.customsReferenceDataUrl}/lists/InvalidGuaranteeReason?$queryParams"
    getOrElseUpdate[InvalidGuaranteeReason](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail*)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }

}

object ReferenceDataConnector {

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
