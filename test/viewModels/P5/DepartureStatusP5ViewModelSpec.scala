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
import models.RejectionType
import models.departureP5.DepartureMessageType._
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.DepartureStatusP5ViewModel
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class DepartureStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow  = LocalDateTime.now()
  private val dateTimePast = dateTimeNow.minusHours(1)

  "DepartureStatusP5ViewModel" - {

    val departureMovement = DepartureMovement(
      departureIdP5,
      Some("mrn"),
      lrn,
      LocalDateTime.now(),
      "location"
    )

    def movementAndMessages(headMessage: DepartureMessageType): DepartureMovementAndMessage =
      DepartureMovementAndMessage(
        departureMovement,
        MessagesForDepartureMovement(
          NonEmptyList(DepartureMessage("messageId", dateTimeNow, headMessage, "body/path"), List.empty)
        ),
        lrn,
        None,
        isDeclarationAmendable = true,
        Seq.empty
      )

    "when given Message with head is DepartureDeclaration" in {

      val movementAndMessage = movementAndMessages(DepartureNotification)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.departureNotification.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of CancellationRequested" in {

      val movementAndMessage = movementAndMessages(CancellationRequested)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel("movement.status.P5.cancellationSubmitted", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of AmendmentSubmitted" in {

      val movementAndMessage = movementAndMessages(AmendmentSubmitted)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel("movement.status.P5.amendmentSubmitted", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of prelodgedDeclarationSent" in {

      val movementAndMessage = movementAndMessages(PrelodgedDeclarationSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel("movement.status.P5.prelodgedDeclarationSent", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of movementNotArrivedResponseSent" in {

      val movementAndMessage = movementAndMessages(MovementNotArrivedResponseSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrivedResponseSent",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.movementNotArrivedResponseSent.viewErrors"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of movementNotArrived" in {

      val movementAndMessage = movementAndMessages(MovementNotArrived)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        Seq(ViewMovementAction(s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}", "movement.status.P5.action.movementNotArrived.respond"))
      )

      result mustBe expectedResult
    }

    "when given Message with head of declarationAmendmentAccepted" in {

      val movementAndMessage = movementAndMessages(DeclarationAmendmentAccepted)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.declarationAmendmentAccepted",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of cancellationDecision" in {

      val movementAndMessage = movementAndMessages(CancellationDecision)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        Seq(
          ViewMovementAction(
            controllers.testOnly.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, lrn).url,
            "movement.status.P5.action.cancellationDecision.viewCancellation"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of discrepancies" in {

      val movementAndMessage = movementAndMessages(Discrepancies)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel("movement.status.P5.discrepancies", Nil)

      result mustBe expectedResult
    }

    "when given Message with head of invalidMRN" in {

      val movementAndMessage = movementAndMessages(InvalidMRN)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.invalidMRN",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of allocatedMRN" in {

      val movementAndMessage = movementAndMessages(AllocatedMRN)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of releasedForTransit" in {

      val movementAndMessage = movementAndMessages(ReleasedForTransit)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.releasedForTransit",
        Seq(
          ViewMovementAction(
            controllers.testOnly.routes.TransitAccompanyingDocumentController.getTAD(departureIdP5, "messageId").url,
            "movement.status.P5.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of goodsNotReleased" in {

      val movementAndMessage = movementAndMessages(GoodsNotReleased)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsNotReleased",
        Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureIdP5, lrn, "messageId").url,
            "movement.status.P5.action.goodsNotReleased.viewDetails"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of guaranteeRejected" in {

      val movementAndMessage = movementAndMessages(GuaranteeRejected)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GuaranteeRejectedP5Controller.onPageLoad(departureIdP5, "messageId", lrn).url,
            "movement.status.P5.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.guaranteeRejected.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of rejectedByOfficeOfDeparture" - {

      "and head of tail is IE015" - {

        val rejectionType: Option[RejectionType] = Some(RejectionType.DeclarationRejection)

        "and declaration is amendable" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, DepartureNotification, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = true,
            Seq("body/path")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.amendDeclaration"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable with errors in range 2 to 10" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, DepartureNotification, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path", "abc")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable with one error" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, DepartureNotification, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable and no FunctionalErrors" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, DepartureNotification, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq.empty
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }
      }

      "and head of tail is IE014" - {

        val rejectionType: Option[RejectionType] = Some(RejectionType.InvalidationRejection)

        "with errors in range 2 to 10" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, CancellationRequested, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path", "abc")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

        "with one error " in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, CancellationRequested, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustBe expectedResult
        }

        "with no FunctionalErrors" in {
          val movementAndMessage = DepartureMovementAndMessage(
            departureMovement,
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage("messageId1", dateTimeNow, RejectedByOfficeOfDeparture, "body/path"),
                List(
                  DepartureMessage("messageId2", dateTimePast, CancellationRequested, "body/path")
                )
              )
            ),
            lrn,
            rejectionType,
            isDeclarationAmendable = false,
            Seq.empty
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.testOnly.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureIdP5, lrn).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

      }

    }

    "when given Message with head of goodsUnderControl" in {

      val movementAndMessage = movementAndMessages(GoodsUnderControl)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5).url,
            "movement.status.P5.action.goodsUnderControl.viewDetails"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of incidentDuringTransit" in {

      val movementAndMessage = movementAndMessages(IncidentDuringTransit)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of declarationSent" in {

      val movementAndMessage = movementAndMessages(DeclarationSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        Seq(
//          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.declarationSent.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of goodsBeingRecovered" in {

      val movementAndMessage = movementAndMessages(GoodsBeingRecovered)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        Seq(
          ViewMovementAction(
            //todo update once CTCP-4085 is completed
            controllers.testOnly.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, lrn).url,
            "movement.status.P5.action.goodsBeingRecovered.viewErrors"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of guaranteeWrittenOff" in {

      val movementAndMessage = movementAndMessages(GuaranteeWrittenOff)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel("movement.status.P5.guaranteeWrittenOff", Nil)

      result mustBe expectedResult
    }

    "when errors are more than one " in {

      val expectedResult = "viewErrors"

      val result = DepartureStatusP5ViewModel.errorsActionText(Seq("body/path", "body/path", "body/path"))

      result mustBe expectedResult

    }

    "when errors are just one " in {

      val expectedResult = "viewError"

      val result = DepartureStatusP5ViewModel.errorsActionText(Seq("body/path"))

      result mustBe expectedResult

    }

  }

}
