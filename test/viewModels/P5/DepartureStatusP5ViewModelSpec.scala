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
import models.departureP5.DepartureMessageType._
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.DepartureStatusP5ViewModel
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class DepartureStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateTimeNow = LocalDateTime.now()

  "DepartureStatusP5ViewModel" - {

    def movementAndMessages(headMessage: DepartureMessageType): DepartureMovementAndMessage =
      DepartureMovementAndMessage(
        DepartureMovement(
          "departureId",
          Some("mrn"),
          LocalDateTime.now(),
          "location"
        ),
        MessagesForDepartureMovement(
          NonEmptyList(DepartureMessage(dateTimeNow, headMessage, "body/path", Nil), List.empty)
        ),
        "AB123",
        isMovementInCache = true
      )

    "when given Message with head of DepartureDeclaration" in {

      val movementAndMessage = movementAndMessages(DepartureNotification)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
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
          ViewMovementAction(s"", "movement.status.P5.action.cancellationDecision.viewCancellation")
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
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.invalidMRN.amendErrors")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of allocatedMRN" in {

      val movementAndMessage = movementAndMessages(AllocatedMRN)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.allocatedMRN.cancelDeclaration")
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
          ViewMovementAction(s"", "movement.status.P5.action.releasedForTransit.viewAndPrintAccompanyingPDF")
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
          ViewMovementAction(s"", "movement.status.P5.action.goodsNotReleased.viewDetails")
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
          ViewMovementAction(s"", "movement.status.P5.action.guaranteeRejected.viewErrors"),
          ViewMovementAction(s"", "movement.status.P5.action.guaranteeRejected.cancelDeclaration")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of rejectedByOfficeOfDeparture" in {

      val movementAndMessage = movementAndMessages(RejectedByOfficeOfDeparture)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.rejectedByOfficeOfDeparture",
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.rejectedByOfficeOfDeparture.amendErrors")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of goodsUnderControl" in {

      val movementAndMessage = movementAndMessages(GoodsUnderControl)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.goodsUnderControl.viewDetails"),
          ViewMovementAction(s"", "movement.status.P5.action.goodsUnderControl.cancelDeclaration")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of incidentDuringTransit" in {

      val movementAndMessage = movementAndMessages(IncidentDuringTransit)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.incidentDuringTransit.viewErrors")
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of declarationSent" in {

      val movementAndMessage = movementAndMessages(DeclarationSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        Seq(
          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.cancelDeclaration")
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
          ViewMovementAction(s"", "movement.status.P5.action.goodsBeingRecovered.viewErrors")
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

  }

}
