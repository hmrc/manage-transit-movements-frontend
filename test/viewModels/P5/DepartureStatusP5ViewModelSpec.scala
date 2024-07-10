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
import generators.Generators
import models.departureP5.DepartureMessageType._
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.DepartureStatusP5ViewModel
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class DepartureStatusP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "DepartureStatusP5ViewModel" - {

    def otherMovementAndMessage(messageType: DepartureMessageType): OtherMovementAndMessage =
      OtherMovementAndMessage(
        departureIdP5,
        lrn.value,
        LocalDateTime.now(),
        LatestDepartureMessage(
          DepartureMessage(
            messageId,
            LocalDateTime.now(),
            messageType
          ),
          "ie015MessageId"
        )
      )

    "when given Message with head is DepartureDeclaration" in {

      val movementAndMessage = otherMovementAndMessage(DepartureNotification)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of CancellationRequested" in {

      val movementAndMessage = otherMovementAndMessage(CancellationRequested)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationSubmitted",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of AmendmentSubmitted" in {

      val movementAndMessage = otherMovementAndMessage(AmendmentSubmitted)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.amendmentSubmitted",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of prelodgedDeclarationSent" in {

      val movementAndMessage = otherMovementAndMessage(PrelodgedDeclarationSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.prelodgedDeclarationSent",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of movementNotArrivedResponseSent" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrivedResponseSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrivedResponseSent",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.p5Unloading}",
            "movement.status.P5.action.movementNotArrivedResponseSent.viewErrors"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of movementNotArrived" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrived)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.p5Unloading}",
            "movement.status.P5.action.movementNotArrived.respond"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of declarationAmendmentAccepted" - {

      "when prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              DeclarationAmendmentAccepted
            ),
            "ie015MessageId"
          ),
          isPrelodged = true
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.declarationAmendmentAccepted",
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.departureAmendmentUrl(lrn.value, departureIdP5)}",
              "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
              "movement.status.P5.action.declarationAmendmentAccepted.completeDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
              "movement.status.P5.action.declarationAmendmentAccepted.cancelDeclaration"
            )
          )
        )

        result mustBe expectedResult
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              DeclarationAmendmentAccepted
            ),
            "ie015MessageId"
          ),
          isPrelodged = false
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.declarationAmendmentAccepted",
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.departureAmendmentUrl(lrn.value, departureIdP5)}",
              "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
            )
          )
        )

        result mustBe expectedResult
      }

    }

    "when given Message with head of cancellationDecision" in {

      val movementAndMessage = otherMovementAndMessage(CancellationDecision)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.IsDepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, messageId).url,
            "movement.status.P5.action.cancellationDecision.viewCancellation"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of discrepancies" in {

      val movementAndMessage = otherMovementAndMessage(Discrepancies)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.discrepancies",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of invalidMRN" in {

      val movementAndMessage = otherMovementAndMessage(InvalidMRN)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.invalidMRN",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of allocatedMRN" in {

      val movementAndMessage = OtherMovementAndMessage(
        departureIdP5,
        lrn.value,
        LocalDateTime.now(),
        LatestDepartureMessage(
          DepartureMessage(
            messageId,
            LocalDateTime.now(),
            AllocatedMRN
          ),
          "ie015MessageId"
        )
      )

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.departureAmendmentUrl(lrn.value, departureIdP5)}",
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of releasedForTransit" in {

      val movementAndMessage = otherMovementAndMessage(ReleasedForTransit)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.releasedForTransit",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.TransitAccompanyingDocumentController.getTAD(departureIdP5, messageId).url,
            "movement.status.P5.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of goodsNotReleased" in {

      val movementAndMessage = otherMovementAndMessage(GoodsNotReleased)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsNotReleased",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureIdP5, messageId).url,
            "movement.status.P5.action.goodsNotReleased.viewDetails"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of guaranteeRejected" in {

      val movementAndMessage = otherMovementAndMessage(GuaranteeRejected)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.GuaranteeRejectedP5Controller.onPageLoad(departureIdP5, messageId).url,
            "movement.status.P5.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.guaranteeRejected.cancelDeclaration"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of rejectedByOfficeOfDeparture" - {

      "when BusinessRejectionType is AmendmentRejection" - {

        "with one functional error and cache exists for LRN" in {

          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = "013",
            isDeclarationAmendable = true,
            xPaths = Seq("body/path"),
            doesCacheExistForLrn = true
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.amendDeclaration"
              )
            )
          )

          result mustBe expectedResult
        }

        "and cache exists for LRN with no functional errors" in {

          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = "013",
            isDeclarationAmendable = false,
            xPaths = Nil,
            doesCacheExistForLrn = true
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.amendDeclaration"
              )
            )
          )

          result mustBe expectedResult
        }

        "and cache does not exists for LRN and no errors" in {

          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = "013",
            isDeclarationAmendable = false,
            xPaths = Nil,
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

        "and cache does not exists for LRN with errors" in {

          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = "013",
            isDeclarationAmendable = false,
            xPaths = Seq("body/path"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewDepartureErrorsP5Controller
                  .onPageLoad(None, departureIdP5, messageId)
                  .url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustBe expectedResult
        }
      }

      "and head of tail is IE015" - {

        val rejectionType = "015"

        "and declaration is amendable" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = true,
            xPaths = Seq("body/path"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.amendDeclaration"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable with errors in range 2 to 10" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path", "abc"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable with one error" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustBe expectedResult
        }

        "and declaration is not amendable and no FunctionalErrors" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq.empty,
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }
      }

      "and head of tail is IE014" - {

        val rejectionType = "014"

        "with errors in range 2 to 10" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path", "abc"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

        "with one error " in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq("body/path"),
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustBe expectedResult
        }

        "with no FunctionalErrors" in {
          val movementAndMessage = RejectedMovementAndMessage(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            LatestDepartureMessage(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                RejectedByOfficeOfDeparture
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isDeclarationAmendable = false,
            Seq.empty,
            doesCacheExistForLrn = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustBe expectedResult
        }

      }

    }

    "when given Message with head of goodsUnderControl" - {

      "when prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              GoodsUnderControl
            ),
            "ie015MessageId"
          ),
          isPrelodged = true
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.goodsUnderControl",
          Seq(
            ViewMovementAction(
              controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url,
              "movement.status.P5.action.goodsUnderControl.viewDetails"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
              "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
              "movement.status.P5.action.goodsUnderControl.completeDeclaration"
            )
          )
        )

        result mustBe expectedResult
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              GoodsUnderControl
            ),
            "ie015MessageId"
          ),
          isPrelodged = false
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.goodsUnderControl",
          Seq(
            ViewMovementAction(
              controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url,
              "movement.status.P5.action.goodsUnderControl.viewDetails"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
              "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
            )
          )
        )

        result mustBe expectedResult
      }
    }

    "when given Message with head of incidentDuringTransit" in {

      val movementAndMessage = otherMovementAndMessage(IncidentDuringTransit)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        Nil
      )

      result mustBe expectedResult
    }

    "when given Message with head of declarationSent" - {

      "when prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              DeclarationSent
            ),
            "ie015MessageId"
          ),
          isPrelodged = true
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.declarationSent",
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.departureAmendmentUrl(lrn.value, departureIdP5)}",
              "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
              "movement.status.P5.action.declarationSent.cancelDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
              "movement.status.P5.action.declarationSent.completeDeclaration"
            )
          )
        )

        result mustBe expectedResult
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessage(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          LatestDepartureMessage(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              DeclarationSent
            ),
            "ie015MessageId"
          ),
          isPrelodged = false
        )

        val result = DepartureStatusP5ViewModel(movementAndMessage)

        val expectedResult = DepartureStatusP5ViewModel(
          "movement.status.P5.declarationSent",
          Nil
        )

        result mustBe expectedResult
      }
    }

    "when given Message with head of goodsBeingRecovered" in {

      val movementAndMessage = otherMovementAndMessage(GoodsBeingRecovered)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.RecoveryNotificationController.onPageLoad(departureIdP5, messageId).url,
            "movement.status.P5.action.goodsBeingRecovered.viewDetails"
          )
        )
      )

      result mustBe expectedResult
    }

    "when given Message with head of movementEnded" in {

      val movementAndMessage = otherMovementAndMessage(MovementEnded)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementEnded",
        Nil
      )

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
