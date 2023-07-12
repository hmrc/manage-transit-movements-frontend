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

package viewModels.P5.departure

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Messages, messages}

import java.time.{LocalDate, LocalTime}

class ViewDepartureP5Spec extends AnyFreeSpec with Matchers {
  private val dateNow = LocalDate.now()
  private val timeNow = LocalTime.now()

  "ViewMovementSpec" - {

    "must return correct statusWithArgs when args is a Some" in {

      val departure = ViewDepartureP5(dateNow, timeNow, "abc123", "status1", Seq.empty, Some("LRN23242"))

      departure.statusWithArgs(implicit messages: Messages) mustBe "LRN23242"

    }
  }
}
