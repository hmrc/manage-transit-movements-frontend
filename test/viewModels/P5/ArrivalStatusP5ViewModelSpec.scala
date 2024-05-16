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
import models.arrivalP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.arrival.ArrivalStatusP5ViewModel
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class ArrivalStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "ArrivalStatusP5ViewModel" - {

    "must return correct ArrivalStatusP5ViewModel" - {

      def movementAndMessagesOther(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
        OtherMovementAndMessage(
          ArrivalMovement(
            "arrivalID",
            "mrn",
            LocalDateTime.now(),
            "location"
          ),
          LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage), arrivalIdP5)
        )

      "when given Message with head of ArrivalNotification" in {

        val movementAndMessage = movementAndMessagesOther(ArrivalNotification)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", Nil)

        result mustBe expectedResult
      }

      "when given Message with head of UnloadingRemarks" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingRemarks)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", Nil)

        result mustBe expectedResult
      }

      "when given Message with head of UnloadingPermission" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingPermission)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "movement.status.P5.unloadingPermissionReceived",
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.p5Unloading}/${movementAndMessage.arrivalMovement.arrivalId}/unloading-remarks/$messageId",
              "movement.status.P5.action.unloadingPermission.unloadingRemarks"
            ),
            ViewMovementAction(
              controllers.arrivalP5.routes.UnloadingPermissionController
                .getUnloadingPermissionDocument(movementAndMessage.arrivalMovement.arrivalId, messageId)
                .url,
              "movement.status.P5.action.unloadingPermission.pdf"
            )
          )
        )

        result mustBe expectedResult
      }

      "when given Message with head of GoodsReleasedNotification" - {
        def movementAndMessages(goodsReleased: String): ArrivalMovementAndMessage =
          GoodsReleasedMovementAndMessage(
            ArrivalMovement(
              "arrivalID",
              "mrn",
              LocalDateTime.now(),
              "location"
            ),
            LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, GoodsReleasedNotification), arrivalIdP5),
            goodsReleased
          )
        "when goods are released" in {

          val movementAndMessage: ArrivalMovementAndMessage = movementAndMessages("3")

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.goodsReleased", Nil)

          result mustBe expectedResult
        }

        "when goods are not released" in {

          val movementAndMessage: ArrivalMovementAndMessage = movementAndMessages("4")

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrival.goodsNotReleased", Nil)

          result mustBe expectedResult
        }
      }

      "when given Message with head of RejectionFromOfficeOfDestination for unloading" - {
        "and there are no functional errors" in {

          def movementAndMessagesRejectedZero(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                arrivalIdP5,
                mrn,
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage), arrivalIdP5),
              functionalErrorCount = 0,
              "044"
            )

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedZero(ArrivalMessageType.RejectionFromOfficeOfDestination))

          val href = controllers.arrivalP5.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
            )
          )

          result mustBe expectedResult
        }

        "and there are functional errors" in {

          val messages = MessagesForArrivalMovement(
            NonEmptyList(
              ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination),
              List(
                ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks)
              )
            )
          )

          val movementAndMessagesRejectedMultiple: ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(messages.messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              "044"
            )

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedMultiple)

          val href = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
            )
          )

          result mustBe expectedResult
        }
      }

      "when given Message with head of rejectionFromOfficeOfDestinationArrival for arrival" - {

        "and there are functional errors" in {

          val messages = MessagesForArrivalMovement(
            NonEmptyList(
              ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination),
              List(
                ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks)
              )
            )
          )
          val movementAndMessagesRejectedMultiple: ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(messages.messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              "007"
            )

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedMultiple)
          val href   = controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
            )
          )

          result mustBe expectedResult
        }

        "and there are no functional errors" in {
          def movementAndMessagesRejectedZero(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                arrivalIdP5,
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage), arrivalIdP5),
              functionalErrorCount = 0,
              "007"
            )
          val movementAndMessage = movementAndMessagesRejectedZero(RejectionFromOfficeOfDestination)

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.arrivalP5.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
            )
          )

          result mustBe expectedResult
        }
      }

      "when errors are more than one " - {

        val expectedResult = "viewErrors"

        val result = ArrivalStatusP5ViewModel.errorsActionText(2)

        result mustBe expectedResult

      }

      "when errors are just one " - {

        val expectedResult = "viewError"

        val result = ArrivalStatusP5ViewModel.errorsActionText(1)

        result mustBe expectedResult

      }
    }
  }
}
