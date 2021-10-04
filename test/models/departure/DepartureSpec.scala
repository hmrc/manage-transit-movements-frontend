/*
 * Copyright 2021 HM Revenue & Customs
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

package models.departure

import base.SpecBase
import models.departure.DepartureStatus._
import models.{Departure, DepartureId, LocalReferenceNumber}

import java.time.LocalDateTime

class DepartureSpec extends SpecBase {

  "currentStatus" - {
    "when there is only the message from the user" - {

      "must return messageType" in {
        val localDateTime: LocalDateTime = LocalDateTime.now()

        val departure =
          Departure(
            DepartureId(22),
            localDateTime,
            LocalReferenceNumber("lrn"),
            Seq(DepartureMessageMetaData(DepartureSubmitted, localDateTime.minusSeconds(10)))
          )

        departure.currentStatus mustBe DepartureSubmitted
      }
    }

    "when there are responses from NCTS for the departure" - {
      "when there is a single response from NCTS" - {
        "must return the messageType for the latest NCTS message" in {

          val localDateTime: LocalDateTime = LocalDateTime.now()

          val departure =
            Departure(
              DepartureId(22),
              localDateTime,
              LocalReferenceNumber("lrn"),
              Seq(
                DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10))
              )
            )

          departure.currentStatus mustBe PositiveAcknowledgement
        }
      }

      "when there are multiple responses from NCTS" - {
        "when messages are well ordered" - {
          "must return the messageType for the latest NCTS message" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val departure =
              Departure(
                DepartureId(22),
                localDateTime,
                LocalReferenceNumber("lrn"),
                Seq(
                  DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                  DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10)),
                  DepartureMessageMetaData(MrnAllocated, localDateTime.plusSeconds(20))
                )
              )

            departure.currentStatus mustBe MrnAllocated
          }
        }

        "when messages are not well ordered" - {
          "must return the messageType for the message with the latest dateTime" - {

            "Scenario 1" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusSeconds(20)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10)),
                  )
                )

              departure.currentStatus mustBe MrnAllocated
            }

            "Scenario 2" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(GuaranteeNotValid, localDateTime.plusDays(3)),
                    DepartureMessageMetaData(NoReleaseForTransit, localDateTime.plusDays(4)),
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusDays(2)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusDays(1)),
                  )
                )

              departure.currentStatus mustBe NoReleaseForTransit
            }

            "Scenario 3" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(DeclarationCancellationRequest, localDateTime.plusWeeks(3)),
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(CancellationDecision, localDateTime.plusMonths(4)),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusDays(2)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(1)),
                  )
                )

              departure.currentStatus mustBe CancellationDecision
            }
          }
        }

        "when messages have the same latest dateTime" - {

          "must return the latest messageType" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val departure =
              Departure(
                DepartureId(22),
                localDateTime,
                LocalReferenceNumber("lrn"),
                Seq(
                  DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                  DepartureMessageMetaData(MrnAllocated, localDateTime.plusMinutes(10)),
                  DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusMinutes(10))
                )
              )

            departure.currentStatus mustBe MrnAllocated
          }
        }
      }
    }
  }

  "previousStatus" - {

    "when there is only the message from the user" - {

      "must return messageType" in {
        val localDateTime: LocalDateTime = LocalDateTime.now()

        val departure =
          Departure(
            DepartureId(22),
            localDateTime,
            LocalReferenceNumber("lrn"),
            Seq(DepartureMessageMetaData(DepartureSubmitted, localDateTime.minusSeconds(10)))
          )

        departure.previousStatus mustBe DepartureSubmitted
      }
    }

    "when there are responses from NCTS for the departure" - {
      "when there is a single response from NCTS" - {
        "must return the messageType for the second latest NCTS message" in {

          val localDateTime: LocalDateTime = LocalDateTime.now()

          val departure =
            Departure(
              DepartureId(22),
              localDateTime,
              LocalReferenceNumber("lrn"),
              Seq(
                DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10))
              )
            )

          departure.previousStatus mustBe DepartureSubmitted
        }
      }

      "when there are multiple responses from NCTS" - {
        "when messages are well ordered" - {
          "must return the messageType for the second latest NCTS message" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val departure =
              Departure(
                DepartureId(22),
                localDateTime,
                LocalReferenceNumber("lrn"),
                Seq(
                  DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                  DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10)),
                  DepartureMessageMetaData(MrnAllocated, localDateTime.plusSeconds(20))
                )
              )

            departure.previousStatus mustBe PositiveAcknowledgement
          }
        }

        "when messages are not well ordered" - {
          "must return the messageType for the message with the second latest dateTime" - {

            "Scenario 1" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusSeconds(20)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(10)),
                  )
                )

              departure.previousStatus mustBe PositiveAcknowledgement
            }

            "Scenario 2" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(GuaranteeNotValid, localDateTime.plusDays(3)),
                    DepartureMessageMetaData(NoReleaseForTransit, localDateTime.plusDays(4)),
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusDays(2)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusDays(1)),
                  )
                )

              departure.previousStatus mustBe GuaranteeNotValid
            }

            "Scenario 3" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val departure =
                Departure(
                  DepartureId(22),
                  localDateTime,
                  LocalReferenceNumber("lrn"),
                  Seq(
                    DepartureMessageMetaData(DeclarationCancellationRequest, localDateTime.plusWeeks(3)),
                    DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                    DepartureMessageMetaData(CancellationDecision, localDateTime.plusMonths(4)),
                    DepartureMessageMetaData(MrnAllocated, localDateTime.plusDays(2)),
                    DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusSeconds(1)),
                  )
                )

              departure.previousStatus mustBe DeclarationCancellationRequest
            }
          }
        }

        "when messages have the same latest dateTime" - {

          "must return the second latest messageType" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val departure =
              Departure(
                DepartureId(22),
                localDateTime,
                LocalReferenceNumber("lrn"),
                Seq(
                  DepartureMessageMetaData(DepartureSubmitted, localDateTime),
                  DepartureMessageMetaData(MrnAllocated, localDateTime),
                  DepartureMessageMetaData(PositiveAcknowledgement, localDateTime.plusMinutes(10))
                )
              )

            departure.previousStatus mustBe MrnAllocated
          }
        }
      }
    }
  }
}