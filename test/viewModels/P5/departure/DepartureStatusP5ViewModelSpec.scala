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

package viewModels.P5.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import config.FrontendAppConfig
import generators.Generators
import models.MessageStatus
import models.departureP5.*
import models.departureP5.BusinessRejectionType.*
import models.departureP5.DepartureMessageType.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers.running
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class DepartureStatusP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "DepartureStatusP5ViewModel" - {

    def otherMovementAndMessage(messageType: DepartureMessageType, status: MessageStatus = MessageStatus.Success): OtherMovementAndMessages =
      OtherMovementAndMessages(
        departureIdP5,
        lrn.value,
        LocalDateTime.now(),
        DepartureMovementMessages(
          NonEmptyList.one(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              messageType,
              status
            )
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

      result mustEqual expectedResult
    }

    "when given Message with head is Failed DepartureDeclaration" in {

      val movementAndMessage = otherMovementAndMessage(DepartureNotification, MessageStatus.Failed)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.p5Departure,
            "movement.status.P5.resendDepartureNotification"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of CancellationRequested" in {

      val movementAndMessage = otherMovementAndMessage(CancellationRequested)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationSubmitted",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed CancellationRequested" in {

      val movementAndMessage = otherMovementAndMessage(CancellationRequested, MessageStatus.Failed)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.p5CancellationStart(departureIdP5, lrn.value),
            "movement.status.P5.resendCancellation"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of AmendmentSubmitted" in {

      val movementAndMessage = otherMovementAndMessage(AmendmentSubmitted)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.amendmentSubmitted",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed AmendmentSubmitted" in {

      val movementAndMessage = otherMovementAndMessage(AmendmentSubmitted, MessageStatus.Failed)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.amendmentFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.departureFrontendTaskListUrl(lrn.value),
            "movement.status.P5.resendAmendment"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of PrelodgedDeclarationSent" in {

      val movementAndMessage = otherMovementAndMessage(PrelodgedDeclarationSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.prelodgedDeclarationSent",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed PrelodgedDeclarationSent" in {

      val movementAndMessage = otherMovementAndMessage(PrelodgedDeclarationSent, MessageStatus.Failed)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.prelodgedDeclarationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5),
            "movement.status.P5.resendPrelodgedDeclaration"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of movementNotArrivedResponseSent" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrivedResponseSent)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrivedResponseSent",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of movementNotArrived" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrived)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of declarationAmendmentAccepted" - {

      "when prelodged" in {

        val movementAndMessage = DepartureMovementAndMessages(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                DeclarationAmendmentAccepted,
                MessageStatus.Success
              )
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
              controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
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

        result mustEqual expectedResult
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessages(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                DeclarationAmendmentAccepted,
                MessageStatus.Success
              )
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
              controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
              "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
            )
          )
        )

        result mustEqual expectedResult
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

      result mustEqual expectedResult
    }

    "when given Message with head of discrepancies" in {

      val movementAndMessage = otherMovementAndMessage(Discrepancies)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.discrepancies",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of invalidMRN" in {

      val movementAndMessage = otherMovementAndMessage(InvalidMRN)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.invalidMRN",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of allocatedMRN" in {

      val movementAndMessage = OtherMovementAndMessages(
        departureIdP5,
        lrn.value,
        LocalDateTime.now(),
        DepartureMovementMessages(
          NonEmptyList.one(
            DepartureMessage(
              messageId,
              LocalDateTime.now(),
              AllocatedMRN,
              MessageStatus.Success
            )
          ),
          "ie015MessageId"
        )
      )

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        Seq(
          ViewMovementAction(
            controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureIdP5/index/$lrn",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )

      result mustEqual expectedResult
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

      result mustEqual expectedResult
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

      result mustEqual expectedResult
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

      result mustEqual expectedResult
    }

    "when given Message with head of rejectedByOfficeOfDeparture" - {

      "when BusinessRejectionType is AmendmentRejection" - {

        val rejectionType = AmendmentRejection

        "with one functional error and cache exists for LRN" in {

          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = true,
            xPaths = Seq("body/path")
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

          result mustEqual expectedResult
        }

        "and cache exists for LRN with no functional errors" in {

          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = true,
            xPaths = Nil
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

          result mustEqual expectedResult
        }

        "and cache does not exists for LRN and no errors" in {

          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            xPaths = Nil
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

          result mustEqual expectedResult
        }

        "and cache does not exists for LRN with errors" in {

          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            xPaths = Seq("body/path")
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

          result mustEqual expectedResult
        }
      }

      "and head of tail is IE015" - {

        val rejectionType = DeclarationRejection

        "and declaration is amendable" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = true,
            xPaths = Seq("body/path")
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

          result mustEqual expectedResult
        }

        "and declaration is not amendable with errors in range 2 to 10" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq("body/path", "abc")
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

          result mustEqual expectedResult
        }

        "and declaration is not amendable with one error" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq("body/path")
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

          result mustEqual expectedResult
        }

        "and declaration is not amendable and no FunctionalErrors" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq.empty
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

          result mustEqual expectedResult
        }
      }

      "and head of tail is IE014" - {

        val rejectionType = InvalidationRejection

        "with errors in range 2 to 10" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq("body/path", "abc")
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

          result mustEqual expectedResult
        }

        "with one error " in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq("body/path")
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

          result mustEqual expectedResult
        }

        "with no FunctionalErrors" in {
          val movementAndMessage = RejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            rejectionType = rejectionType,
            isRejectionAmendable = false,
            Seq.empty
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

          result mustEqual expectedResult
        }

      }

      "and head of tail is IE170" - {

        "with errors in range 2 to 10" in {
          val movementAndMessage = PrelodgeRejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            Seq("body/path", "abc")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewPrelodgedDeclarationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )

          result mustEqual expectedResult
        }

        "with one error " in {
          val movementAndMessage = PrelodgeRejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            Seq("body/path")
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.ReviewPrelodgedDeclarationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewError"
              )
            )
          )

          result mustEqual expectedResult
        }

        "with no FunctionalErrors" in {
          val movementAndMessage = PrelodgeRejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  RejectedByOfficeOfDeparture,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            Seq.empty
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.PreLodgedDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.P5.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )
          result mustEqual expectedResult
        }
      }
    }

    "when given Message with head of goodsUnderControl" - {

      "when prelodged" - {

        "and IE170 not yet submitted" in {

          val movementAndMessage = DepartureMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  GoodsUnderControl,
                  MessageStatus.Success
                )
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

          result mustEqual expectedResult
        }

        "and IE170 already submitted" in {

          val movementAndMessage = DepartureMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.of(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  GoodsUnderControl,
                  MessageStatus.Success
                ),
                DepartureMessage(
                  messageId,
                  LocalDateTime.now().minusDays(1),
                  PrelodgedDeclarationSent,
                  MessageStatus.Success
                )
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
              )
            )
          )

          result mustEqual expectedResult
        }
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessages(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                GoodsUnderControl,
                MessageStatus.Success
              )
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

        result mustEqual expectedResult
      }
    }

    "when given Message with head of incidentDuringTransit" - {
      "and IE182 is enabled" - {

        val app = guiceApplicationBuilder()
          .configure("microservice.services.features.isIE182Enabled" -> true)
          .build()

        "and containing multiple incidents" in {
          running(app) {
            val movementAndMessage = IncidentMovementAndMessages(
              departureIdP5,
              lrn.value,
              LocalDateTime.now(),
              DepartureMovementMessages(
                NonEmptyList.one(
                  DepartureMessage(
                    messageId,
                    LocalDateTime.now(),
                    IncidentDuringTransit,
                    MessageStatus.Success
                  )
                ),
                "messageId"
              ),
              hasMultipleIncidents = true
            )

            val result = DepartureStatusP5ViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

            val expectedResult = DepartureStatusP5ViewModel(
              "movement.status.P5.incidentDuringTransit",
              Seq(
                ViewMovementAction(
                  controllers.departureP5.routes.IncidentsDuringTransitP5Controller.onPageLoad(departureIdP5, messageId).url,
                  "movement.status.P5.action.incidentDuringTransit.viewIncidents"
                )
              )
            )

            result mustEqual expectedResult
          }
        }

        "and containing one incident" in {

          val movementAndMessage = IncidentMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  IncidentDuringTransit,
                  MessageStatus.Success
                )
              ),
              "messageId"
            ),
            hasMultipleIncidents = false
          )

          val result = DepartureStatusP5ViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

          val expectedResult = DepartureStatusP5ViewModel(
            "movement.status.P5.incidentDuringTransit",
            Seq(
              ViewMovementAction(
                controllers.departureP5.routes.IncidentsDuringTransitP5Controller.onPageLoad(departureIdP5, messageId).url,
                "movement.status.P5.action.incidentDuringTransit.viewIncident"
              )
            )
          )

          result mustEqual expectedResult
        }
      }

      "and IE182 is disabled" in {

        val app = guiceApplicationBuilder()
          .configure("microservice.services.features.isIE182Enabled" -> false)
          .build()

        running(app) {
          forAll(arbitrary[Boolean]) {
            hasMultipleIncidents =>
              val movementAndMessage = IncidentMovementAndMessages(
                departureIdP5,
                lrn.value,
                LocalDateTime.now(),
                DepartureMovementMessages(
                  NonEmptyList.one(
                    DepartureMessage(
                      messageId,
                      LocalDateTime.now(),
                      IncidentDuringTransit,
                      MessageStatus.Success
                    )
                  ),
                  "messageId"
                ),
                hasMultipleIncidents
              )

              val result = DepartureStatusP5ViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

              val expectedResult = DepartureStatusP5ViewModel(
                "movement.status.P5.incidentDuringTransit",
                Seq.empty
              )

              result mustEqual expectedResult
          }
        }
      }
    }

    "when given Message with head of declarationSent" - {

      "when prelodged" in {

        val movementAndMessage = DepartureMovementAndMessages(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                DeclarationSent,
                MessageStatus.Success
              )
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
              controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
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

        result mustEqual expectedResult
      }

      "when not prelodged" in {

        val movementAndMessage = DepartureMovementAndMessages(
          departureIdP5,
          lrn.value,
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                messageId,
                LocalDateTime.now(),
                DeclarationSent,
                MessageStatus.Success
              )
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

        result mustEqual expectedResult
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

      result mustEqual expectedResult
    }

    "when given Message with head of movementEnded" in {

      val movementAndMessage = otherMovementAndMessage(MovementEnded)

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "movement.status.P5.movementEnded",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of UnknownMessageType" in {

      val movementAndMessage = otherMovementAndMessage(UnknownMessageType("foo"))

      val result = DepartureStatusP5ViewModel(movementAndMessage)

      val expectedResult = DepartureStatusP5ViewModel(
        "",
        Nil
      )

      result mustEqual expectedResult
    }

    "when errors are more than one " in {

      val expectedResult = "viewErrors"

      val result = DepartureStatusP5ViewModel.errorsActionText(Seq("body/path", "body/path", "body/path"))

      result mustEqual expectedResult

    }

    "when errors are just one " in {

      val expectedResult = "viewError"

      val result = DepartureStatusP5ViewModel.errorsActionText(Seq("body/path"))

      result mustEqual expectedResult

    }

  }

}
