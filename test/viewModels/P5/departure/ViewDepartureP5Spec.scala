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

package viewModels.P5.departure

import base.SpecBase

import java.time.LocalDateTime

class ViewDepartureP5Spec extends SpecBase {

  "groupByDate" - {

    "must group departures by date and sort in reversed date/time order" in {
      val departure1 = ViewDepartureP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 11, 30),
        referenceNumber = "Reference number 1",
        status = "Status 1",
        actions = Nil
      )

      val departure2 = ViewDepartureP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 12, 30),
        referenceNumber = "Reference number 2",
        status = "Status 2",
        actions = Nil
      )

      val departure3 = ViewDepartureP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 13, 13, 30),
        referenceNumber = "Reference number 3",
        status = "Status 3",
        actions = Nil
      )

      val departure4 = ViewDepartureP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 15, 14, 30),
        referenceNumber = "Reference number 4",
        status = "Status 4",
        actions = Nil
      )

      val departure5 = ViewDepartureP5(
        updatedDateTime = LocalDateTime.of(2025, 3, 14, 10, 30),
        referenceNumber = "Reference number 5",
        status = "Status 5",
        actions = Nil
      )

      val departures = Seq(
        departure1,
        departure2,
        departure3,
        departure4,
        departure5
      )

      val result = departures.groupByDate

      result mustEqual Seq(
        "15 March 2025" -> Seq(departure4),
        "14 March 2025" -> Seq(departure2, departure1, departure5),
        "13 March 2025" -> Seq(departure3)
      )
    }
  }
}
