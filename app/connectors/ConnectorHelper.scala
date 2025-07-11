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

import config.FrontendAppConfig
import play.api.Logging
import sttp.model.HeaderNames

trait ConnectorHelper extends Logging {

  val config: FrontendAppConfig

  final val version: String = config.phase6Enabled match {
    case _ => "2.1"
  }

  private def acceptHeader(format: String): (String, String) =
    HeaderNames.Accept -> s"application/vnd.hmrc.$version+$format"

  def authorizationHeader(authorization: String): (String, String) =
    HeaderNames.Authorization -> authorization

  private def contentTypeHeader(contentType: String): (String, String) =
    HeaderNames.ContentType -> s"application/$contentType"

  def messageTypeHeader(messageType: Option[String]): (String, String) =
    "X-Message-Type" -> messageType.getOrElse("No x-message-type")

  val jsonAcceptHeader: (String, String) = acceptHeader("json")
  val xmlAcceptHeader: (String, String)  = acceptHeader("xml")

  val xmlContentTypeHeader: (String, String)  = contentTypeHeader("xml")
  val jsonContentTypeHeader: (String, String) = contentTypeHeader("json")
}
