/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers
import play.api.test.Helpers._
import controllers.actions._

class KeepAliveControllerSpec extends SpecBase {

  "KeepAliveController" - {

    "must return NOContent and the correct view for a GET" in {

      val request = FakeRequest(GET, routes.KeepAliveController.keepAlive().url)

      val controller = new KeepAliveController(FakeIdentifierAction(), Helpers.stubMessagesControllerComponents())

      val result = controller.keepAlive.apply(request)

      status(result) mustEqual NO_CONTENT

    }
  }
}
