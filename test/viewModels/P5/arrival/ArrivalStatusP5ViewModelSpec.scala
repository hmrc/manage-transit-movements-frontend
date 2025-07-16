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

package viewModels.P5.arrival

import base.SpecBase
import cats.data.NonEmptyList
import generators.Generators
import models.MessageStatus
import models.arrivalP5.*
import models.arrivalP5.ArrivalMessageType.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class ArrivalStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "ArrivalStatusP5ViewModel" - {

    "must return correct ArrivalStatusP5ViewModel" - {

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

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", Nil)

        result mustEqual expectedResult
      }

      "when given Message with head of Failed ArrivalNotification" in {

        val movementAndMessage = movementAndMessagesOther(ArrivalNotification, MessageStatus.Failed)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "movement.status.P5.arrivalNotificationFailed",
          Seq(
            ViewMovementAction(
              frontendAppConfig.p5Arrival,
              "movement.status.P5.resendArrivalNotification"
            )
          )
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnloadingRemarks" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingRemarks)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", Nil)

        result mustEqual expectedResult
      }

      "when given Message with head of Failed UnloadingRemarks" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingRemarks, MessageStatus.Failed)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "movement.status.P5.unloadingRemarksFailed",
          Seq(
            ViewMovementAction(
              frontendAppConfig.p5UnloadingStart(movementAndMessage.arrivalMovement.arrivalId, messageId),
              "movement.status.P5.action.unloadingPermission.resendUnloadingRemarks"
            )
          )
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnloadingPermission" in {

        val movementAndMessage = movementAndMessagesOther(UnloadingPermission)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "movement.status.P5.unloadingPermissionReceived",
          Seq(
            ViewMovementAction(
              frontendAppConfig.p5UnloadingStart(movementAndMessage.arrivalMovement.arrivalId, messageId),
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

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.goodsReleased", Nil)

          result mustEqual expectedResult
        }

        "when goods are not released" in {

          val movementAndMessage: ArrivalMovementAndMessage = movementAndMessages("4")

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrival.goodsNotReleased", Nil)

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

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedZero(ArrivalMessageType.RejectionFromOfficeOfDestination))

          val href = controllers.arrivalP5.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
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

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedMultiple)

          val href = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
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

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedMultiple)
          val href   = controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID", messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
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

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.arrivalP5.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5, messageId)

          val expectedResult = ArrivalStatusP5ViewModel(
            "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
            Seq(
              ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
            )
          )

          result mustEqual expectedResult
        }
      }

      "when given Message with head of movementEnded" in {

        val movementAndMessage = movementAndMessagesOther(MovementEnded)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "movement.status.P5.movementEnded",
          Nil
        )

        result mustEqual expectedResult
      }

      "when given Message with head of UnknownMessageType" in {

        val movementAndMessage = movementAndMessagesOther(UnknownMessageType("foo"))

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel(
          "",
          Nil
        )

        result mustEqual expectedResult
      }

      "when errors are more than one " - {

        val expectedResult = "viewErrors"

        val result = ArrivalStatusP5ViewModel.errorsActionText(2)

        result mustEqual expectedResult

      }

      "when errors are just one " - {

        val expectedResult = "viewError"

        val result = ArrivalStatusP5ViewModel.errorsActionText(1)

        result mustEqual expectedResult

      }
    }
  }

}
