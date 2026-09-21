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

package viewModels.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import config.FrontendAppConfig
import generators.Generators
import models.MessageStatus
import models.departure.*
import models.departure.BusinessRejectionType.*
import models.departure.DepartureMessageType.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers.running
import viewModels.ViewMovementAction

import java.time.LocalDateTime

class DepartureStatusViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "DepartureStatusViewModel" - {

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

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.departureNotificationSubmitted",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head is Failed DepartureDeclaration" in {

      val movementAndMessage = otherMovementAndMessage(DepartureNotification, MessageStatus.Failed)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.departureNotificationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.departure,
            "movement.status.resendDepartureNotification"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of CancellationRequested" in {

      val movementAndMessage = otherMovementAndMessage(CancellationRequested)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.cancellationSubmitted",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed CancellationRequested" in {

      val movementAndMessage = otherMovementAndMessage(CancellationRequested, MessageStatus.Failed)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.cancellationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.cancellationStart(departureIdP5, lrn.value),
            "movement.status.resendCancellation"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of AmendmentSubmitted" in {

      val movementAndMessage = otherMovementAndMessage(AmendmentSubmitted)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.amendmentSubmitted",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed AmendmentSubmitted" in {

      val movementAndMessage = otherMovementAndMessage(AmendmentSubmitted, MessageStatus.Failed)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.amendmentFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.departureFrontendTaskListUrl(lrn.value),
            "movement.status.resendAmendment"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of PrelodgedDeclarationSent" in {

      val movementAndMessage = otherMovementAndMessage(PrelodgedDeclarationSent)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.prelodgedDeclarationSent",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of Failed PrelodgedDeclarationSent" in {

      val movementAndMessage = otherMovementAndMessage(PrelodgedDeclarationSent, MessageStatus.Failed)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.prelodgedDeclarationFailed",
        Seq(
          ViewMovementAction(
            frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5),
            "movement.status.resendPrelodgedDeclaration"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of movementNotArrivedResponseSent" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrivedResponseSent)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.movementNotArrivedResponseSent",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of movementNotArrived" in {

      val movementAndMessage = otherMovementAndMessage(MovementNotArrived)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.movementNotArrived",
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

        val result = DepartureStatusViewModel(movementAndMessage)

        val expectedResult = DepartureStatusViewModel(
          "movement.status.declarationAmendmentAccepted",
          Seq(
            ViewMovementAction(
              controllers.departure.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
              "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
              "movement.status.action.declarationAmendmentAccepted.completeDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
              "movement.status.action.declarationAmendmentAccepted.cancelDeclaration"
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

        val result = DepartureStatusViewModel(movementAndMessage)

        val expectedResult = DepartureStatusViewModel(
          "movement.status.declarationAmendmentAccepted",
          Seq(
            ViewMovementAction(
              controllers.departure.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
              "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
            )
          )
        )

        result mustEqual expectedResult
      }

    }

    "when given Message with head of cancellationDecision" in {

      val movementAndMessage = otherMovementAndMessage(CancellationDecision)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.cancellationDecision",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.IsDepartureCancelledController.isDeclarationCancelled(departureIdP5, messageId).url,
            "movement.status.action.cancellationDecision.viewCancellation"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of discrepancies" in {

      val movementAndMessage = otherMovementAndMessage(Discrepancies)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.discrepancies",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of invalidMRN" in {

      val movementAndMessage = otherMovementAndMessage(InvalidMRN)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.invalidMRN",
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

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.allocatedMRN",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
            "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
            "movement.status.action.allocatedMRN.cancelDeclaration"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of releasedForTransit" in {

      val movementAndMessage = otherMovementAndMessage(ReleasedForTransit)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.releasedForTransit",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.TransitAccompanyingDocumentController.getTAD(departureIdP5, messageId).url,
            "movement.status.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of goodsNotReleased" in {

      val movementAndMessage = otherMovementAndMessage(GoodsNotReleased)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.goodsNotReleased",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.GoodsNotReleasedController.goodsNotReleased(departureIdP5, messageId).url,
            "movement.status.action.goodsNotReleased.viewDetails"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of guaranteeRejected" in {

      val movementAndMessage = otherMovementAndMessage(GuaranteeRejected)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.guaranteeRejected",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.GuaranteeRejectedController.onPageLoad(departureIdP5, messageId).url,
            "movement.status.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
            "movement.status.action.guaranteeRejected.cancelDeclaration"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.RejectionMessageController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.amendDeclaration"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.RejectionMessageController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.amendDeclaration"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.DepartureDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewDepartureErrorsController
                  .onPageLoad(None, departureIdP5, messageId)
                  .url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewError"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.RejectionMessageController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.amendDeclaration"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewDepartureErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewDepartureErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewError"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.DepartureDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewCancellationErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewCancellationErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewError"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.CancellationNotificationErrorsController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewPrelodgedDeclarationErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.ReviewPrelodgedDeclarationErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewError"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.rejectedByOfficeOfDeparture",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.PreLodgedDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.rejectedByOfficeOfDeparture.viewErrors"
              )
            )
          )
          result mustEqual expectedResult
        }
      }
    }

    "when given Message with head of invalidMRN" - {

      "and IE022 is enabled" - {
        val app = guiceApplicationBuilder()
          .configure("microservice.services.features.isIE022Enabled" -> true)
          .build()

        "and declaration is amendable" in {
          running(app) {
            val movementAndMessage = DeclarationAmendmentRejectedMovementAndMessages(
              departureIdP5,
              lrn.value,
              LocalDateTime.now(),
              DepartureMovementMessages(
                NonEmptyList.one(
                  DepartureMessage(
                    messageId,
                    LocalDateTime.now(),
                    InvalidMRN,
                    MessageStatus.Success
                  )
                ),
                "ie015MessageId"
              ),
              isRejectionAmendable = true,
              xPaths = Seq(Some("body/path"))
            )

            val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

            val expectedResult = DepartureStatusViewModel(
              "movement.status.invalidMRN",
              Seq(
                ViewMovementAction(
                  controllers.departure.routes.DeclarationAmendmentRejectionMessageController.onPageLoad(None, departureIdP5, messageId).url,
                  "movement.status.action.invalidMRN.amendDeclaration"
                )
              )
            )

            result mustEqual expectedResult
          }
        }

        "and declaration is not amendable with one FunctionalError" in {
          running(app) {
            val movementAndMessage = DeclarationAmendmentRejectedMovementAndMessages(
              departureIdP5,
              lrn.value,
              LocalDateTime.now(),
              DepartureMovementMessages(
                NonEmptyList.one(
                  DepartureMessage(
                    messageId,
                    LocalDateTime.now(),
                    InvalidMRN,
                    MessageStatus.Success
                  )
                ),
                "ie015MessageId"
              ),
              isRejectionAmendable = false,
              Seq(Some("body/path"))
            )

            val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

            val expectedResult = DepartureStatusViewModel(
              "movement.status.invalidMRN",
              Seq(
                ViewMovementAction(
                  controllers.departure.routes.ReviewDepartureAmendmentErrorsController.onPageLoad(None, departureIdP5, messageId).url,
                  "movement.status.action.invalidMRN.viewError"
                )
              )
            )

            result mustEqual expectedResult
          }
        }

        "and declaration is not amendable and no FunctionalErrors" in {
          running(app) {
            val movementAndMessage = DeclarationAmendmentRejectedMovementAndMessages(
              departureIdP5,
              lrn.value,
              LocalDateTime.now(),
              DepartureMovementMessages(
                NonEmptyList.one(
                  DepartureMessage(
                    messageId,
                    LocalDateTime.now(),
                    InvalidMRN,
                    MessageStatus.Success
                  )
                ),
                "ie015MessageId"
              ),
              isRejectionAmendable = false,
              Seq.empty
            )

            val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

            val expectedResult = DepartureStatusViewModel(
              "movement.status.invalidMRN",
              Seq(
                ViewMovementAction(
                  controllers.departure.routes.AmendDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url,
                  "movement.status.action.invalidMRN.viewErrors"
                )
              )
            )

            result mustEqual expectedResult
          }
        }
      }

      "and IE022 is disabled" in {
        val app = guiceApplicationBuilder()
          .configure("microservice.services.features.isIE022Enabled" -> false)
          .build()

        running(app) {
          val movementAndMessage = DeclarationAmendmentRejectedMovementAndMessages(
            departureIdP5,
            lrn.value,
            LocalDateTime.now(),
            DepartureMovementMessages(
              NonEmptyList.one(
                DepartureMessage(
                  messageId,
                  LocalDateTime.now(),
                  InvalidMRN,
                  MessageStatus.Success
                )
              ),
              "ie015MessageId"
            ),
            isRejectionAmendable = true,
            xPaths = Seq(Some("body/path"))
          )

          val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

          val expectedResult = DepartureStatusViewModel(
            "movement.status.invalidMRN",
            Seq.empty
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.goodsUnderControl",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.goodsUnderControl.viewDetails"
              ),
              ViewMovementAction(
                s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
                "movement.status.action.goodsUnderControl.cancelDeclaration"
              ),
              ViewMovementAction(
                s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
                "movement.status.action.goodsUnderControl.completeDeclaration"
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

          val result = DepartureStatusViewModel(movementAndMessage)

          val expectedResult = DepartureStatusViewModel(
            "movement.status.goodsUnderControl",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.goodsUnderControl.viewDetails"
              ),
              ViewMovementAction(
                s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
                "movement.status.action.goodsUnderControl.cancelDeclaration"
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

        val result = DepartureStatusViewModel(movementAndMessage)

        val expectedResult = DepartureStatusViewModel(
          "movement.status.goodsUnderControl",
          Seq(
            ViewMovementAction(
              controllers.departure.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url,
              "movement.status.action.goodsUnderControl.viewDetails"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
              "movement.status.action.goodsUnderControl.cancelDeclaration"
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

            val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

            val expectedResult = DepartureStatusViewModel(
              "movement.status.incidentDuringTransit",
              Seq(
                ViewMovementAction(
                  controllers.departure.routes.IncidentsDuringTransitController.onPageLoad(departureIdP5, messageId).url,
                  "movement.status.action.incidentDuringTransit.viewIncidents"
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

          val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

          val expectedResult = DepartureStatusViewModel(
            "movement.status.incidentDuringTransit",
            Seq(
              ViewMovementAction(
                controllers.departure.routes.IncidentsDuringTransitController.onPageLoad(departureIdP5, messageId).url,
                "movement.status.action.incidentDuringTransit.viewIncident"
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

              val result = DepartureStatusViewModel(movementAndMessage)(app.injector.instanceOf[FrontendAppConfig])

              val expectedResult = DepartureStatusViewModel(
                "movement.status.incidentDuringTransit",
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

        val result = DepartureStatusViewModel(movementAndMessage)

        val expectedResult = DepartureStatusViewModel(
          "movement.status.declarationSent",
          Seq(
            ViewMovementAction(
              controllers.departure.routes.AmendmentController.prepareForAmendment(departureIdP5).url,
              "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.cancellation}/$departureIdP5/index/$lrn",
              "movement.status.action.declarationSent.cancelDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)}",
              "movement.status.action.declarationSent.completeDeclaration"
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

        val result = DepartureStatusViewModel(movementAndMessage)

        val expectedResult = DepartureStatusViewModel(
          "movement.status.declarationSent",
          Nil
        )

        result mustEqual expectedResult
      }
    }

    "when given Message with head of goodsBeingRecovered" in {

      val movementAndMessage = otherMovementAndMessage(GoodsBeingRecovered)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.goodsBeingRecovered",
        Seq(
          ViewMovementAction(
            controllers.departure.routes.RecoveryNotificationController.onPageLoad(departureIdP5, messageId).url,
            "movement.status.action.goodsBeingRecovered.viewDetails"
          )
        )
      )

      result mustEqual expectedResult
    }

    "when given Message with head of movementEnded" in {

      val movementAndMessage = otherMovementAndMessage(MovementEnded)

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "movement.status.movementEnded",
        Nil
      )

      result mustEqual expectedResult
    }

    "when given Message with head of UnknownMessageType" in {

      val movementAndMessage = otherMovementAndMessage(UnknownMessageType("foo"))

      val result = DepartureStatusViewModel(movementAndMessage)

      val expectedResult = DepartureStatusViewModel(
        "",
        Nil
      )

      result mustEqual expectedResult
    }

    "when errors are more than one " in {

      val expectedResult = "viewErrors"

      val result = DepartureStatusViewModel.errorsActionText(Seq("body/path", "body/path", "body/path"))

      result mustEqual expectedResult

    }

    "when errors are just one " in {

      val expectedResult = "viewError"

      val result = DepartureStatusViewModel.errorsActionText(Seq("body/path"))

      result mustEqual expectedResult

    }

  }

}
