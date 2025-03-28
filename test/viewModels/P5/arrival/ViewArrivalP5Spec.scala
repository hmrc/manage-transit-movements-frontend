/*
 * Copyright 2025 HM Revenue & Customs
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

package viewModels.P5.arrival

import base.SpecBase

import java.time.LocalDateTime

class ViewArrivalP5Spec extends SpecBase {

  "groupByDate" - {

    "must group arrivals by date and sort in reversed date/time order" in {
      val arrival1 = ViewArrivalP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 11, 30),
        movementReferenceNumber = "Reference number 1",
        status = "Status 1",
        actions = Nil
      )

      val arrival2 = ViewArrivalP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 12, 30),
        movementReferenceNumber = "Reference number 2",
        status = "Status 2",
        actions = Nil
      )

      val arrival3 = ViewArrivalP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 13, 13, 30),
        movementReferenceNumber = "Reference number 3",
        status = "Status 3",
        actions = Nil
      )

      val arrival4 = ViewArrivalP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 15, 14, 30),
        movementReferenceNumber = "Reference number 4",
        status = "Status 4",
        actions = Nil
      )

      val arrival5 = ViewArrivalP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 10, 30),
        movementReferenceNumber = "Reference number 5",
        status = "Status 5",
        actions = Nil
      )

      val arrivals = Seq(
        arrival1,
        arrival2,
        arrival3,
        arrival4,
        arrival5
      )

      val result = arrivals.groupByDate

      result mustEqual Seq(
        "15 March 2025" -> Seq(arrival4),
        "14 March 2025" -> Seq(arrival2, arrival1, arrival5),
        "13 March 2025" -> Seq(arrival3)
      )
    }
  }
}
