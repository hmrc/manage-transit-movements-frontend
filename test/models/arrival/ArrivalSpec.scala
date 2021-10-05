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

package models.arrival

import base.SpecBase
import models.arrival.ArrivalStatus.{
  ArrivalNotificationSubmitted,
  ArrivalRejection,
  GoodsReleased,
  UnloadingPermission,
  UnloadingRemarksRejection,
  UnloadingRemarksSubmitted,
  XMLSubmissionNegativeAcknowledgement
}
import models.{Arrival, ArrivalId, Departure, DepartureId, LocalReferenceNumber}

import java.time.LocalDateTime

class ArrivalSpec extends SpecBase {

  "currentStatus" - {
    "when there is only the message from the user" - {

      "must return messageType" in {
        val localDateTime: LocalDateTime = LocalDateTime.now()

        val arrival =
          Arrival(
            ArrivalId(22),
            localDateTime,
            localDateTime,
            Seq(ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10))),
            "mrn123"
          )

        arrival.currentStatus mustBe ArrivalNotificationSubmitted
      }
    }

    "when there are responses from NCTS for the arrival" - {
      "when there is a single response from NCTS" - {
        "must return the messageType for the latest NCTS message" in {

          val localDateTime: LocalDateTime = LocalDateTime.now()

          val arrival =
            Arrival(
              ArrivalId(22),
              localDateTime,
              localDateTime,
              Seq(
                ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10)),
                ArrivalMessageMetaData(UnloadingPermission, localDateTime)
              ),
              "mrn123"
            )

          arrival.currentStatus mustBe UnloadingPermission
        }
      }

      "when there are multiple responses from NCTS" - {
        "when messages are well ordered" - {
          "must return the messageType for the latest NCTS message" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val arrival =
              Arrival(
                ArrivalId(22),
                localDateTime,
                localDateTime,
                Seq(
                  ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
                  ArrivalMessageMetaData(GoodsReleased, localDateTime.minusSeconds(10)),
                  ArrivalMessageMetaData(ArrivalRejection, localDateTime)
                ),
                "mrn123"
              )

            arrival.currentStatus mustBe ArrivalRejection
          }
        }

        "when messages are not well ordered" - {
          "must return the messageType for the message with the latest dateTime" - {

            "Scenario 1" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
                    ArrivalMessageMetaData(ArrivalRejection, localDateTime.minusSeconds(15)),
                    ArrivalMessageMetaData(GoodsReleased, localDateTime),
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10))
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe GoodsReleased
            }

            "Scenario 2" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusDays(3)),
                    ArrivalMessageMetaData(UnloadingPermission, localDateTime.minusDays(2)),
                    ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime.minusDays(4))
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe UnloadingPermission
            }

            "Scenario 3" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(GoodsReleased, localDateTime),
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusWeeks(2))
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe GoodsReleased
            }
          }
        }

        "when messages have the same latest dateTime" - {

          "and the current weighted message is ArrivalRejection" - {

            "must return ArrivalRejection when there are equal ArrivalNotificationSubmitted" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime),
                    ArrivalMessageMetaData(ArrivalRejection, localDateTime)
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe ArrivalRejection
            }

            "must return ArrivalNotificationSubmitted when there are less ArrivalRejections" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime),
                    ArrivalMessageMetaData(ArrivalRejection, localDateTime.plusSeconds(10)),
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.plusSeconds(10))
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe ArrivalNotificationSubmitted
            }
          }

          "and the current weighted message is UnloadingRemarksRejection" - {

            "must return UnloadingRemarksRejection when there are equal UnloadingRemarksSubmitted" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(UnloadingRemarksRejection, localDateTime),
                    ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime)
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe UnloadingRemarksRejection
            }

            "must return UnloadingRemarksSubmitted when there are less UnloadingRemarksRejection" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime),
                    ArrivalMessageMetaData(UnloadingRemarksRejection, localDateTime.plusSeconds(10)),
                    ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime.plusSeconds(10))
                  ),
                  "mrn123"
                )

              arrival.currentStatus mustBe UnloadingRemarksSubmitted
            }

          }

          "and the current weighted message is XMLSubmissionNegativeAcknowledgement" - {

            "and there are previous unloading remarks" - {

              "must return XMLSubmissionNegativeAcknowledgement when there are equal UnloadingRemarksSubmitted" in {

                val localDateTime: LocalDateTime = LocalDateTime.now()

                val arrival =
                  Arrival(
                    ArrivalId(22),
                    localDateTime,
                    localDateTime,
                    Seq(
                      ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime),
                      ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime)
                    ),
                    "mrn123"
                  )

                arrival.currentStatus mustBe XMLSubmissionNegativeAcknowledgement
              }

              "must return UnloadingRemarksSubmitted when there are less XMLSubmissionNegativeAcknowledgement" in {

                val localDateTime: LocalDateTime = LocalDateTime.now()

                val arrival =
                  Arrival(
                    ArrivalId(22),
                    localDateTime,
                    localDateTime,
                    Seq(
                      ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime),
                      ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime.plusSeconds(10)),
                      ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime.plusSeconds(10))
                    ),
                    "mrn123"
                  )

                arrival.currentStatus mustBe UnloadingRemarksSubmitted
              }
            }

            "and there are previous arrivals ArrivalNotificationSubmitted" - {

              "must return XMLSubmissionNegativeAcknowledgement when there are equal ArrivalNotificationSubmitted" in {

                val localDateTime: LocalDateTime = LocalDateTime.now()

                val arrival =
                  Arrival(
                    ArrivalId(22),
                    localDateTime,
                    localDateTime,
                    Seq(
                      ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime),
                      ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime)
                    ),
                    "mrn123"
                  )

                arrival.currentStatus mustBe XMLSubmissionNegativeAcknowledgement
              }

              "must return ArrivalNotificationSubmitted when there are less XMLSubmissionNegativeAcknowledgement" in {

                val localDateTime: LocalDateTime = LocalDateTime.now()

                val arrival =
                  Arrival(
                    ArrivalId(22),
                    localDateTime,
                    localDateTime,
                    Seq(
                      ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime),
                      ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime.plusSeconds(10)),
                      ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.plusSeconds(10))
                    ),
                    "mrn123"
                  )

                arrival.currentStatus mustBe ArrivalNotificationSubmitted
              }

            }
          }

          "must return the latest messageType" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val arrival =
              Arrival(
                ArrivalId(22),
                localDateTime,
                localDateTime,
                Seq(
                  ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
                  ArrivalMessageMetaData(GoodsReleased, localDateTime.minusSeconds(10)),
                  ArrivalMessageMetaData(ArrivalRejection, localDateTime.minusSeconds(10))
                ),
                "mrn123"
              )

            arrival.currentStatus mustBe GoodsReleased
          }
        }
      }
    }
  }

  "previous" - {
    "when there is only the message from the user" - {

      "must return messageType" in {
        val localDateTime: LocalDateTime = LocalDateTime.now()

        val arrival =
          Arrival(
            ArrivalId(22),
            localDateTime,
            localDateTime,
            Seq(ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10))),
            "mrn123"
          )

        arrival.previousStatus mustBe ArrivalNotificationSubmitted
      }
    }

    "when there are responses from NCTS for the arrival" - {
      "when there is a single response from NCTS" - {
        "must return the messageType for the second latest NCTS message" in {

          val localDateTime: LocalDateTime = LocalDateTime.now()

          val arrival =
            Arrival(
              ArrivalId(22),
              localDateTime,
              localDateTime,
              Seq(
                ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10)),
                ArrivalMessageMetaData(UnloadingPermission, localDateTime)
              ),
              "mrn123"
            )

          arrival.previousStatus mustBe ArrivalNotificationSubmitted
        }
      }

      "when there are multiple responses from NCTS" - {
        "when messages are well ordered" - {
          "must return the messageType for the second latest NCTS message" in {

            val localDateTime: LocalDateTime = LocalDateTime.now()

            val arrival =
              Arrival(
                ArrivalId(22),
                localDateTime,
                localDateTime,
                Seq(
                  ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
                  ArrivalMessageMetaData(GoodsReleased, localDateTime.minusSeconds(10)),
                  ArrivalMessageMetaData(ArrivalRejection, localDateTime)
                ),
                "mrn123"
              )

            arrival.previousStatus mustBe GoodsReleased
          }
        }

        "when messages are not well ordered" - {
          "must return the messageType for the message with the second latest dateTime" - {

            "Scenario 1" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
                    ArrivalMessageMetaData(ArrivalRejection, localDateTime.minusSeconds(15)),
                    ArrivalMessageMetaData(GoodsReleased, localDateTime),
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(10))
                  ),
                  "mrn123"
                )

              arrival.previousStatus mustBe ArrivalNotificationSubmitted
            }

            "Scenario 2" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusDays(3)),
                    ArrivalMessageMetaData(UnloadingPermission, localDateTime.minusDays(2)),
                    ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime.minusDays(4))
                  ),
                  "mrn123"
                )

              arrival.previousStatus mustBe ArrivalNotificationSubmitted
            }

            "Scenario 3" in {

              val localDateTime: LocalDateTime = LocalDateTime.now()

              val arrival =
                Arrival(
                  ArrivalId(22),
                  localDateTime,
                  localDateTime,
                  Seq(
                    ArrivalMessageMetaData(GoodsReleased, localDateTime),
                    ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusWeeks(2))
                  ),
                  "mrn123"
                )

              arrival.previousStatus mustBe ArrivalNotificationSubmitted
            }
          }
        }
      }
    }

    "when currentStatus is XMLSubmissionNegativeAcknowledgement" - {

      "must not weight previous messages when messageType is ArrivalNotificationSubmitted" in {

        val localDateTime: LocalDateTime = LocalDateTime.now()

        val arrival =
          Arrival(
            ArrivalId(22),
            localDateTime,
            localDateTime,
            Seq(
              ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime.minusSeconds(20)),
              ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime.minusSeconds(20))
            ),
            "mrn123"
          )

        arrival.currentStatus mustBe XMLSubmissionNegativeAcknowledgement
        arrival.previousStatus mustBe ArrivalNotificationSubmitted
      }

      "must not weight previous messages when messageType is UnloadingRemarksSubmitted" in {

        val localDateTime: LocalDateTime = LocalDateTime.now()

        val arrival =
          Arrival(
            ArrivalId(22),
            localDateTime,
            localDateTime,
            Seq(
              ArrivalMessageMetaData(UnloadingRemarksSubmitted, localDateTime.minusSeconds(20)),
              ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime.minusSeconds(20))
            ),
            "mrn123"
          )

        arrival.currentStatus mustBe XMLSubmissionNegativeAcknowledgement
        arrival.previousStatus mustBe UnloadingRemarksSubmitted
      }
    }
  }
}
