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
import models.ArrivalRejectionType.{ArrivalNotificationRejection, UnloadingRemarkRejection}
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
              s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}/${movementAndMessage.arrivalMovement.arrivalId}/unloading-remarks/$messageId",
              "movement.status.P5.action.unloadingPermission.unloadingRemarks"
            ),
            ViewMovementAction(
              controllers.testOnly.routes.UnloadingPermissionController
                .getUnloadingPermissionDocument(messageId, movementAndMessage.arrivalMovement.arrivalId)
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

          val messages = MessagesForArrivalMovement(
            NonEmptyList(
              ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination),
              List(ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks))
            )
          )

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
              UnloadingRemarkRejection
            )

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedZero(ArrivalMessageType.RejectionFromOfficeOfDestination))

          val href = controllers.testOnly.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5)

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
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

          def movementAndMessagesRejectedMultiple(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(messages.messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              UnloadingRemarkRejection
            )

          val movementAndMessage = movementAndMessagesRejectedMultiple(RejectionFromOfficeOfDestination)

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.testOnly.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID")

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
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
          def movementAndMessagesRejectedMultiple(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
            RejectedMovementAndMessage(
              ArrivalMovement(
                "arrivalID",
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(messages.messages.head, arrivalIdP5),
              functionalErrorCount = 3,
              ArrivalNotificationRejection
            )

          val result = ArrivalStatusP5ViewModel(movementAndMessagesRejectedMultiple(ArrivalMessageType.RejectionFromOfficeOfDestination))

          val href = controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, "arrivalID")

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
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
                "arrivalID",
                "mrn",
                LocalDateTime.now(),
                "location"
              ),
              LatestArrivalMessage(ArrivalMessage(arrivalIdP5, dateTimeNow, headMessage), arrivalIdP5),
              functionalErrorCount = 0,
              ArrivalNotificationRejection
            )
          val movementAndMessage = movementAndMessagesRejectedZero(RejectionFromOfficeOfDestination)

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.testOnly.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad("arrivalID")

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
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
