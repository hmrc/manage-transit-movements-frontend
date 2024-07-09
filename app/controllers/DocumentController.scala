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

package controllers

import play.api.http.HeaderNames._
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.mvc.Results.{Ok, Redirect}
import uk.gov.hmrc.http.HttpResponse

trait DocumentController {

  def stream(response: HttpResponse): Result =
    response.status match {
      case OK =>
        def header(key: String): Option[(String, String)] =
          response.header(key).map(key -> _)

        val contentLength = header(CONTENT_LENGTH).map(_._2).flatMap(_.toLongOption)
        val contentType   = header(CONTENT_TYPE).map(_._2)
        val headers       = header(CONTENT_DISPOSITION).toSeq

        Ok.sendEntity(HttpEntity.Streamed(response.bodyAsSource, contentLength, contentType))
          .withHeaders(headers: _*)
      case _ =>
        Redirect(routes.ErrorController.technicalDifficulties())
    }
}
