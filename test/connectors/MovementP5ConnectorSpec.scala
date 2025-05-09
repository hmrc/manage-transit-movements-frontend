/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import sttp.model.HeaderNames

class MovementP5ConnectorSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  class TestMovementP5Connector extends MovementP5Connector
  val connector: MovementP5Connector = TestMovementP5Connector()

  "MovementP5Connector" - {

    "generate the correct acceptHeader for JSON" in {
      connector.jsonAcceptHeader mustEqual (HeaderNames.Accept -> "application/vnd.hmrc.2.1+json")
    }

    "generate the correct acceptHeader for XML" in {
      connector.xmlAcceptHeader mustEqual (HeaderNames.Accept -> "application/vnd.hmrc.2.1+xml")
    }

    "generate the correct authorization header" in {
      val token = "Bearer some-token"
      connector.authorizationHeader(token) mustEqual (HeaderNames.Authorization -> token)
    }

    "generate the correct contentTypeHeader for XML" in {
      connector.xmlContentTypeHeader mustEqual (HeaderNames.ContentType -> "application/xml")
    }

    "generate the correct contentTypeHeader for JSON" in {
      connector.jsonContentTypeHeader mustEqual (HeaderNames.ContentType -> "application/json")
    }

    "generate the correct messageTypeHeader when message type is present" in {
      val messageType = Some("IE015")
      connector.messageTypeHeader(messageType) mustEqual ("X-Message-Type" -> "IE015")
    }

    "generate the correct messageTypeHeader when message type is absent" in {
      val messageType = None
      connector.messageTypeHeader(messageType) mustEqual ("X-Message-Type" -> "No x-message-type")
    }
  }
}
