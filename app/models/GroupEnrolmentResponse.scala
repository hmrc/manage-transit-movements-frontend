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

package models

import play.api.http.Status._
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed trait GroupEnrolmentResponse

object GroupEnrolmentResponse {

  case class Enrolments(enrolments: Seq[Enrolment]) extends GroupEnrolmentResponse

  object Enrolments {

    implicit val reads: Reads[Enrolments] = Json.reads[Enrolments]
  }

  case class Enrolment(service: String)

  object Enrolment {
    implicit val reads: Reads[Enrolment] = Json.reads[Enrolment]
  }

  case object NoEnrolments extends GroupEnrolmentResponse

  case class BadRequest(code: String) extends GroupEnrolmentResponse

  object BadRequest {

    implicit val reads: Reads[BadRequest] = Json.reads[BadRequest]
  }

  case class Other(code: Int, message: String) extends GroupEnrolmentResponse

  implicit val httpReads: HttpReads[GroupEnrolmentResponse] =
    (_: String, _: String, response: HttpResponse) => {
      def validate[T <: GroupEnrolmentResponse](implicit rds: Reads[T]): GroupEnrolmentResponse =
        response.json
          .validate[T]
          .fold(
            errors => Other(response.status, s"Failed to validate json: $errors"),
            identity
          )

      response.status match {
        case OK                     => validate[Enrolments]
        case NO_CONTENT | NOT_FOUND => NoEnrolments
        case BAD_REQUEST            => validate[BadRequest]
        case _                      => Other(response.status, response.body)
      }
    }
}
