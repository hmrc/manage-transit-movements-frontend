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

package viewModels.arrival

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import generators.Generators
import models.MessageStatus
import models.arrival.*
import models.arrival.ArrivalMessageType.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class ArrivalStatusViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "ArrivalStatusViewModel" - {

    "must return correct ArrivalStatusViewModel" - {

      def movementAndMessagesOther(headMessage: ArrivalMessageType, status: MessageStatus = MessageStatus.Success): ArrivalMovementAndMessage =
        OtherMovementAndMessage(
          ArrivalMovement(
            "arrivalID",
            "mrn",
            LocalDateTime.now()
          ),
          LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage, status), arrivalIdP5)
        )

      "when given Message with head of ArrivalNotification" in {

        val movementAndMessage = movementAndMessagesOther(ArrivalNotification)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel("movement.status.arrivalNotificationSubmitted", Nil)

        result mustEqual expectedResult
      }

      "when given Message with head of Failed ArrivalNotification" in {

        val movementAndMessage = movementAndMessagesOther(ArrivalNotification, MessageStatus.Failed)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel(
          "movement.status.arrivalNotificationFailed",
          Seq(
            ViewMovementAction(
              frontendAppConfig.arrival,
              "movement.status.resendArrivalNotification"
            )
          )
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnloadingRemarks" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingRemarks)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel("movement.status.unloadingRemarksSubmitted", Nil)

        result mustEqual expectedResult
      }

      "when given Message with head of Failed UnloadingRemarks" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingRemarks, MessageStatus.Failed)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel(
          "movement.status.unloadingRemarksFailed",
          Seq(
            ViewMovementAction(
              frontendAppConfig.unloadingStart(movementAndMessage.arrivalMovement.arrivalId, messageId),
              "movement.status.action.unloadingPermission.resendUnloadingRemarks"
            )
          )
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnloadingPermission" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingPermission)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel(
          "movement.status.unloadingPermissionReceived",
          Seq(
            ViewMovementAction(
              frontendAppConfig.unloadingStart(movementAndMessage.arrivalMovement.arrivalId, messageId),
              "movement.status.action.unloadingPermission.unloadingRemarks"
            ),
            ViewMovementAction(
              controllers.arrival.routes.UnloadingPermissionController
                .getUnloadingPermissionDocument(movementAndMessage.arrivalMovement.arrivalId, messageId)
                .url,
              "movement.status.action.unloadingPermission.pdf"
            )
          )
        )

        result mustEqual expectedResult
      }

      "when given Message with head of GoodsReleasedNotification" - {
        def movementAndMessages(goodsReleased: String): ArrivalMovementAndMessage =
          GoodsReleasedMovementAndMessage(
            ArrivalMovement(
              "arrivalID",
              "mrn",
              LocalDateTime.now()
            ),
            LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, GoodsReleasedNotification, MessageStatus.Success), arrivalIdP5),
            goodsReleased
          )
        "when goods are released" in {

          val movementAndMessage: ArrivalMovementAndMessage = movementAndMessages("3")

          val result = ArrivalStatusViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusViewModel("movement.status.goodsReleased", Nil)

          result mustEqual expectedResult
        }

        "when goods are not released" in {

          val movementAndMessage: ArrivalMovementAndMessage = movementAndMessages("4")

          val result = ArrivalStatusViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusViewModel("movement.status.arrival.goodsNotReleased", Nil)

          result mustEqual expectedResult
        }
      }

      "when given Message with head of RejectionFromOfficeOfDestination for unloading" - {
        "and there are no functional errors" in {

          def movementAndMessagesRejectedZero(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                arrivalIdP5,
                mrn,
                LocalDateTime.now()
              ),
              LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage, MessageStatus.Success), arrivalIdP5),
              functionalErrorCount = 0,
              "044"
            )

          val result = ArrivalStatusViewModel(movementAndMessagesRejectedZero(ArrivalMessageType.RejectionFromOfficeOfDestination))

          val href = controllers.arrival.routes.UnloadingRemarkWithoutFunctionalErrorsController.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusViewModel(
            "movement.status.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.action.viewErrors")
            )
          )

          result mustEqual expectedResult
        }

        "and there are functional errors" in {

          val messages = NonEmptyList(
            ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination, MessageStatus.Success),
            List(
              ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks, MessageStatus.Success)
            )
          )

          val movementAndMessagesRejectedMultiple: ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now()
              ),
              LatestArrivalMessage(messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              "044"
            )

          val result = ArrivalStatusViewModel(movementAndMessagesRejectedMultiple)

          val href = controllers.arrival.routes.UnloadingRemarkWithFunctionalErrorsController.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusViewModel(
            "movement.status.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.action.viewErrors")
            )
          )

          result mustEqual expectedResult
        }
      }

      "when given Message with head of rejectionFromOfficeOfDestinationArrival for arrival" - {

        "and there are functional errors" in {

          val messages = NonEmptyList(
            ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination, MessageStatus.Success),
            List(
              ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks, MessageStatus.Success)
            )
          )

          val movementAndMessagesRejectedMultiple: ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now()
              ),
              LatestArrivalMessage(messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              "007"
            )

          val result = ArrivalStatusViewModel(movementAndMessagesRejectedMultiple)
          val href   = controllers.arrival.routes.ArrivalNotificationWithFunctionalErrorsController.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusViewModel(
            "movement.status.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.action.viewErrors")
            )
          )

          result mustEqual expectedResult
        }

        "and there are no functional errors" in {
          def movementAndMessagesRejectedZero(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                arrivalIdP5,
                "mrn",
                LocalDateTime.now()
              ),
              LatestArrivalMessage(ArrivalMessage(messageId, dateTimeNow, headMessage, MessageStatus.Success), arrivalIdP5),
              functionalErrorCount = 0,
              "007"
            )
          val movementAndMessage = movementAndMessagesRejectedZero(RejectionFromOfficeOfDestination)

          val result = ArrivalStatusViewModel(movementAndMessage)

          val href = controllers.arrival.routes.ArrivalNotificationWithoutFunctionalErrorsController.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusViewModel(
            "movement.status.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.action.viewErrors")
            )
          )

          result mustEqual expectedResult
        }
      }

      "when given Message with head of movementEnded" in {

        val movementAndMessage = movementAndMessagesOther(MovementEnded)

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel(
          "movement.status.movementEnded",
          Nil
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnknownMessageType" in {

        val movementAndMessage = movementAndMessagesOther(UnknownMessageType("foo"))

        val result = ArrivalStatusViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusViewModel(
          "",
          Nil
        )

        result mustEqual expectedResult
      }

      "when errors are more than one " - {

        val expectedResult = "viewErrors"

        val result = ArrivalStatusViewModel.errorsActionText(2)

        result mustEqual expectedResult

      }

      "when errors are just one " - {

        val expectedResult = "viewError"

        val result = ArrivalStatusViewModel.errorsActionText(1)

        result mustEqual expectedResult

      }
    }
  }

}
