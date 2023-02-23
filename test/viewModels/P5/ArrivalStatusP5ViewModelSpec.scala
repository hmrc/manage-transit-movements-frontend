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

package viewModels.P5

import base.SpecBase
import cats.data.NonEmptyList
import generators.Generators
import models.arrivalP5.ArrivalMessageType._
import models.arrivalP5.Message
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class ArrivalStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "ArrivalStatusP5ViewModel" - {

    "when given Message with head of ArrivalNotification" in {

      val messages = NonEmptyList(Message(dateTimeNow, ArrivalNotification), List.empty)

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of UnloadingRemarks" in {

      val messages = NonEmptyList(Message(dateTimeNow, UnloadingRemarks), List.empty)

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of UnloadingPermission" in {

      val messages = NonEmptyList(Message(dateTimeNow, UnloadingPermission), List.empty)

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel(
        "movement.status.P5.unloadingPermissionReceived",
        Seq(
          ViewMovementAction("#", "movement.status.P5.action.unloadingPermission.unloadingRemarks"),
          ViewMovementAction("#", "movement.status.P5.action.unloadingPermission.pdf")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of GoodsReleasedNotification" in {

      val messages = NonEmptyList(Message(dateTimeNow, GoodsReleasedNotification), List.empty)

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.goodsReleasedReceived", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of RejectionFromOfficeOfDestination for unloading" in {

      val messages = NonEmptyList(Message(dateTimeNow, RejectionFromOfficeOfDestination),
                                  List(
                                    Message(dateTimeNow, UnloadingRemarks)
                                  )
      )

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
                                                    Seq(
                                                      ViewMovementAction("#", "movement.status.P5.action.viewError")
                                                    )
      )

      result mustBe expectedResult
    }

    "when given Message with head of rejectionFromOfficeOfDestinationArrival for arrival" in {

      val messages = NonEmptyList(Message(dateTimeNow, RejectionFromOfficeOfDestination), List.empty)

      val result = ArrivalStatusP5ViewModel(messages)

      val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
                                                    Seq(
                                                      ViewMovementAction("#", "movement.status.P5.action.viewError")
                                                    )
      )

      result mustBe expectedResult
    }

  }

}
