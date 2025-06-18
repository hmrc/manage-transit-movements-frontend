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
import connectors.ReferenceDataConnector.*
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

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Responses[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> "application/vnd.hmrc.1.0+json")
      .execute[Responses[T]]

  // https://www.playframework.com/documentation/2.6.x/ScalaCache#Accessing-the-Cache-API
  private def getOrElseUpdate[T: ClassTag](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Response[T]] =
    cache.getOrElseUpdate[Response[T]](url.toString, config.asyncCacheApiExpiration.seconds) {
      get[T](url).map(_.map(_.head))
    }

  def getCustomsOffice(
    customsOfficeId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[CustomsOffice]] = {
    val queryParams = Seq("data.id" -> customsOfficeId)
    val url         = url"${config.customsReferenceDataUrl}/lists/CustomsOffices?$queryParams"
    getOrElseUpdate[CustomsOffice](url)
  }

  def getCountry(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Country]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/CountryCodesFullList?$queryParams"
    getOrElseUpdate[Country](url)
  }

  def getQualifierOfIdentification(
    qualifier: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[QualifierOfIdentification]] = {
    val queryParams = Seq("data.qualifier" -> qualifier)
    val url         = url"${config.customsReferenceDataUrl}/lists/QualifierOfTheIdentification?$queryParams"
    getOrElseUpdate[QualifierOfIdentification](url)
  }

  def getIdentificationType(
    `type`: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[IdentificationType]] = {
    val queryParams = Seq("data.type" -> `type`)
    val url         = url"${config.customsReferenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport?$queryParams"
    getOrElseUpdate[IdentificationType](url)
  }

  def getNationality(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Nationality]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/Nationality?$queryParams"
    getOrElseUpdate[Nationality](url)
  }

  def getIncidentCode(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[IncidentCode]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/IncidentCode?$queryParams"
    getOrElseUpdate[IncidentCode](url)
  }

  def getControlType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[ControlType]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/ControlType?$queryParams"
    getOrElseUpdate[ControlType](url)
  }

  def getRequestedDocumentType(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[RequestedDocumentType]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/RequestedDocumentType?$queryParams"
    getOrElseUpdate[RequestedDocumentType](url)
  }

  def getFunctionalError(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[FunctionalErrorWithDesc]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/FunctionalErrorCodesIeCA?$queryParams"
    getOrElseUpdate[FunctionalErrorWithDesc](url)
  }

  def getInvalidGuaranteeReason(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[InvalidGuaranteeReason]] = {
    val queryParams = Seq("data.code" -> code)
    val url         = url"${config.customsReferenceDataUrl}/lists/InvalidGuaranteeReason?$queryParams"
    getOrElseUpdate[InvalidGuaranteeReason](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[Either[Exception, NonEmptySet[A]]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              Left(NoReferenceDataFoundException(url))
            case JsSuccess(head :: tail, _) =>
              Right(NonEmptySet.of(head, tail*))
            case JsError(errors) =>
              Left(JsResultException(errors))
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          Left(Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}"))
      }

}

object ReferenceDataConnector {

  type Responses[T] = Either[Exception, NonEmptySet[T]]
  type Response[T]  = Either[Exception, T]

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
