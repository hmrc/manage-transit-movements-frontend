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
import models.arrivalP5.{ArrivalMessage, ArrivalMessageType, ArrivalMovement, ArrivalMovementAndMessage, MessagesForArrivalMovement}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.arrival.ArrivalStatusP5ViewModel
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class ArrivalStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "ArrivalStatusP5ViewModel" - {

    "must return correct ArrivalStatusP5ViewModel" - {

      def movementAndMessages(headMessage: ArrivalMessageType): ArrivalMovementAndMessage =
        ArrivalMovementAndMessage(
          ArrivalMovement(
            "arrivalID",
            "mrn",
            LocalDateTime.now(),
            "location"
          ),
          MessagesForArrivalMovement(
            NonEmptyList(ArrivalMessage(messageId, dateTimeNow, headMessage), List.empty)
          ),
          functionalErrorCount = 0
        )

      "when given Message with head of ArrivalNotification" in {

        val movementAndMessage = movementAndMessages(ArrivalNotification)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", Nil)

        result mustBe expectedResult
      }

      "when given Message with head of UnloadingRemarks" in {

        val movementAndMessage = movementAndMessages(UnloadingRemarks)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", Nil)

        result mustBe expectedResult
      }

      "when given Message with head of UnloadingPermission" in {

        val movementAndMessage = movementAndMessages(UnloadingPermission)

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

      "when given Message with head of GoodsReleasedNotification" in {

        val movementAndMessage = movementAndMessages(GoodsReleasedNotification)

        val result = ArrivalStatusP5ViewModel(movementAndMessage)

        val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.goodsReleasedReceived", Nil)

        result mustBe expectedResult
      }

      "when given Message with head of RejectionFromOfficeOfDestination for unloading" - {
        "and there are no functional errors" in {

          val messages = MessagesForArrivalMovement(
            NonEmptyList(
              ArrivalMessage(messageId, dateTimeNow, RejectionFromOfficeOfDestination),
              List(
                ArrivalMessage(messageId, dateTimeNow, UnloadingRemarks)
              )
            )
          )

          val movementAndMessage0Errors = movementAndMessages(RejectionFromOfficeOfDestination).copy(
            messagesForMovement = messages,
            functionalErrorCount = 0
          )

          val result0Errors = ArrivalStatusP5ViewModel(movementAndMessage0Errors)

          val href = controllers.testOnly.routes.UnloadingRemarkErrorsP5Controller.onPageLoad("arrivalID")

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
                                                        Seq(
                                                          ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
                                                        )
          )

          result0Errors mustBe expectedResult
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

          val movementAndMessage = movementAndMessages(RejectionFromOfficeOfDestination).copy(
            messagesForMovement = messages,
            functionalErrorCount = 11
          )

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.testOnly.routes.ReviewUnloadingRemarkErrorsP5Controller.onPageLoad("arrivalID")

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

          val movementAndMessage =
            movementAndMessages(RejectionFromOfficeOfDestination).copy(functionalErrorCount = 11)

          val result = ArrivalStatusP5ViewModel(movementAndMessage)

          val href = controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad("arrivalID")

          val expectedResult = ArrivalStatusP5ViewModel("movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
                                                        Seq(
                                                          ViewMovementAction(s"$href", "movement.status.P5.action.viewErrors")
                                                        )
          )

          result mustBe expectedResult
        }

        "and there are no functional errors" in {

          val movementAndMessage =
            movementAndMessages(RejectionFromOfficeOfDestination).copy(functionalErrorCount = 0)

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

    }
  }
}
