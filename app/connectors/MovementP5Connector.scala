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

import logging.Logging
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpReadsTry, HttpResponse}

trait MovementP5Connector extends HttpReadsTry with Logging {

  def messageModelHttpReads[T](implicit reads: Reads[T]): HttpReads[T] =
    (_: String, _: String, response: HttpResponse) =>
      response.json
        .validate[T]
        .fold(
          errors => throw new Exception(s"Failed to read message: ${errors.mkString}"),
          identity
        )

}
