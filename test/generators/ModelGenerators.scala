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

package generators

import models.ErrorType.GenericError
import models._
import models.arrival.{ArrivalStatus, XMLSubmissionNegativeAcknowledgementMessage}
import models.arrivalP5.{ArrivalMovement, ArrivalMovements}
import models.departure._
import models.departureP5.{DepartureMovement, DepartureMovements}
import models.referenceData.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaNumStr, choose, listOfN, numChar}
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call

import java.time._

// scalastyle:off magic.number
trait ModelGenerators {
  self: Generators =>

  implicit val arbitraryControlDecision: Arbitrary[ControlDecision] =
    Arbitrary {
      for {
        mrn                 <- Gen.alphaNumStr
        dateOfControl       <- arbitrary[LocalDate]
        principleTraderName <- Gen.alphaNumStr
        principleTraderEori <- Gen.option(Gen.alphaNumStr)
      } yield ControlDecision(mrn, dateOfControl, principleTraderName, principleTraderEori)
    }

  implicit val arbitraryLocalDate: Arbitrary[LocalDate] =
    Arbitrary {
      for {
        day <- Gen.choose(1, 28)
        month <- Gen.chooseNum(
          LocalDate.MIN.getMonthValue,
          LocalDate.MAX.getMonthValue
        )
        year <- Gen.chooseNum(1111, 3000)
      } yield LocalDate.of(year, month, day)
    }

  implicit val arbitraryLocalTime: Arbitrary[LocalTime] = Arbitrary {
    for {
      hours   <- Gen.chooseNum(0, 23)
      minutes <- Gen.chooseNum(0, 59)
    } yield LocalTime.of(hours, minutes)
  }

  def dateTimesBetween(min: LocalDateTime, max: LocalDateTime): Gen[LocalDateTime] = {
    def toMillis(date: LocalDateTime): Long =
      date.atZone(ZoneOffset.UTC).toInstant.toEpochMilli
    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDateTime
    }
  }

  implicit lazy val arbitraryLocalDateTime: Arbitrary[LocalDateTime] = Arbitrary {
    dateTimesBetween(
      LocalDateTime.of(1900, 1, 1, 0, 0, 0),
      LocalDateTime.of(2100, 1, 1, 0, 0, 0)
    )
  }

  implicit lazy val arbitraryArrivalId: Arbitrary[ArrivalId] =
    Arbitrary {
      for {
        length        <- choose(1, 9)
        listOfCharNum <- listOfN(length, numChar)
      } yield ArrivalId(listOfCharNum.mkString.toInt)
    }

  implicit lazy val arbitraryDepartureId: Arbitrary[DepartureId] =
    Arbitrary {
      for {
        length        <- choose(1, 9)
        listOfCharNum <- listOfN(length, numChar)
      } yield DepartureId(listOfCharNum.mkString.toInt)
    }

  implicit val arbitraryArrival: Arbitrary[Arrival] =
    Arbitrary {
      for {
        arrivalId <- arbitrary[ArrivalId]
        date      <- arbitrary[LocalDateTime]
        time      <- arbitrary[LocalDateTime]
        mrn       <- stringsWithMaxLength(17)
        status    <- arbitrary[ArrivalStatus]
      } yield Arrival(arrivalId, date, time, mrn, status)
    }

  implicit val arbitraryDeparture: Arbitrary[Departure] =
    Arbitrary {
      for {
        departureID          <- arbitrary[DepartureId]
        updated              <- arbitrary[LocalDateTime]
        localReferenceNumber <- arbitrary[LocalReferenceNumber]
        status               <- arbitrary[DepartureStatus]
      } yield Departure(departureID, updated, localReferenceNumber, status)
    }

  implicit val arbitraryDepartureStatus: Arbitrary[DepartureStatus] =
    Arbitrary {
      Gen.oneOf(DepartureStatus.values)
    }

  implicit val arbitraryArrivalStatus: Arbitrary[ArrivalStatus] =
    Arbitrary {
      Gen.oneOf(ArrivalStatus.values)
    }

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- alphaNumericWithMaxLength(22)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryControlResult: Arbitrary[ControlResult] =
    Arbitrary {
      for {
        dateLimit <- localDateGen
        code      <- stringsWithMaxLength(2)
      } yield ControlResult(dateLimit, code)
    }

  implicit lazy val arbitraryResultsOfControl: Arbitrary[ResultsOfControl] =
    Arbitrary {
      for {
        indicator   <- stringsWithMaxLength(2)
        description <- Gen.option(alphaNumStr)
      } yield ResultsOfControl(indicator, description)
    }

  implicit lazy val arbitraryNoReleaseForTransitMessage: Arbitrary[NoReleaseForTransitMessage] = Arbitrary {
    for {
      mrn                        <- nonEmptyString
      noReleaseMotivation        <- Gen.option(nonEmptyString)
      totalNumberOfItems         <- arbitrary[Int]
      officeOfDepartureRefNumber <- Gen.alphaNumStr
      controlResult              <- arbitrary[ControlResult]
      resultsOfControl           <- Gen.option(arbitrary[Seq[ResultsOfControl]])
    } yield new NoReleaseForTransitMessage(
      mrn = mrn,
      noReleaseMotivation = noReleaseMotivation,
      totalNumberOfItems = totalNumberOfItems,
      officeOfDepartureRefNumber = officeOfDepartureRefNumber,
      controlResult = controlResult,
      resultsOfControl = resultsOfControl
    )
  }

  implicit lazy val arbitraryResultsOfControlList: Arbitrary[Seq[ResultsOfControl]] = Arbitrary {
    listWithMaxLength[ResultsOfControl](ResultsOfControl.maxResultsOfControl)
  }

  implicit lazy val genericErrorType: Arbitrary[GenericError] =
    Arbitrary {
      Gen.oneOf(ErrorType.genericValues)
    }

  implicit lazy val arbitraryErrorType: Arbitrary[ErrorType] =
    Arbitrary {
      for {
        genericError <- arbitrary[GenericError]
        errorType    <- Gen.oneOf(Seq(genericError))
      } yield errorType
    }

  implicit lazy val arbitraryRejectionError: Arbitrary[FunctionalError] =
    Arbitrary {
      for {
        errorType     <- arbitrary[ErrorType]
        pointer       <- Gen.alphaNumStr
        reason        <- Gen.option(Gen.alphaNumStr)
        originalValue <- Gen.option(Gen.alphaNumStr)
      } yield FunctionalError(errorType, ErrorPointer(pointer), reason, originalValue)
    }

  implicit lazy val arbitraryXMLSubmissionNegativeAcknowledgementMessage: Arbitrary[XMLSubmissionNegativeAcknowledgementMessage] =
    Arbitrary {
      for {
        mrn   <- Gen.option(nonEmptyString)
        lrn   <- Gen.option(nonEmptyString)
        error <- arbitrary[FunctionalError]
      } yield XMLSubmissionNegativeAcknowledgementMessage(mrn, lrn, error)
    }

  implicit lazy val arbitraryAvailability: Arbitrary[Availability] =
    Arbitrary {
      Gen.oneOf(Availability.NonEmpty, Availability.Empty, Availability.Unavailable)
    }

  implicit lazy val arbitraryDraftAvailability: Arbitrary[DraftAvailability] =
    Arbitrary {
      Gen.oneOf(DraftAvailability.NonEmpty, DraftAvailability.Empty, DraftAvailability.Unavailable)
    }

  implicit lazy val arbitraryArrivals: Arbitrary[Arrivals] =
    Arbitrary {
      for {
        retrievedArrivals <- arbitrary[Int]
        totalArrivals     <- arbitrary[Int]
        totalMatched      <- arbitrary[Option[Int]]
        arrivals          <- listWithMaxLength[Arrival]()
      } yield Arrivals(retrievedArrivals, totalArrivals, totalMatched, arrivals)
    }

  implicit lazy val arbitraryArrivalMovement: Arbitrary[ArrivalMovement] =
    Arbitrary {
      for {
        arrivalId        <- arbitrary[String]
        mrn              <- arbitrary[String]
        updated          <- arbitrary[LocalDateTime]
        messagesLocation <- arbitrary[String]
      } yield ArrivalMovement(arrivalId, mrn, updated, messagesLocation)
    }

  implicit lazy val arbitraryArrivalMovements: Arbitrary[ArrivalMovements] = Arbitrary {
    Gen.nonEmptyListOf(arbitrary[ArrivalMovement]).map(ArrivalMovements(_))
  }

  implicit lazy val arbitraryDepartureMovement: Arbitrary[DepartureMovement] =
    Arbitrary {
      for {
        departureId      <- arbitrary[String]
        mrn              <- arbitrary[String]
        updated          <- arbitrary[LocalDateTime]
        messagesLocation <- arbitrary[String]
      } yield DepartureMovement(departureId, Some(mrn), updated, messagesLocation)
    }

  implicit lazy val arbitraryDepartureMovements: Arbitrary[DepartureMovements] = Arbitrary {
    Gen.nonEmptyListOf(arbitrary[DepartureMovement]).map(DepartureMovements(_))
  }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- arbitrary[String]
        name        <- arbitrary[String]
        phoneNumber <- arbitrary[Option[String]]
      } yield CustomsOffice(id, name, phoneNumber)
    }

  implicit lazy val arbitraryDepartures: Arbitrary[Departures] =
    Arbitrary {
      for {
        retrievedDepartures <- arbitrary[Int]
        totalDepartures     <- arbitrary[Int]
        totalMatched        <- arbitrary[Option[Int]]
        departures          <- listWithMaxLength[Departure]()
      } yield Departures(retrievedDepartures, totalDepartures, totalMatched, departures)
    }

  implicit lazy val arbitraryCall: Arbitrary[Call] =
    Arbitrary {
      for {
        method <- Gen.const("POST")
        url    <- nonEmptyString
      } yield Call(method, url)
    }

  implicit lazy val arbitraryUserAnswerSummary: Arbitrary[DepartureUserAnswerSummary] =
    Arbitrary {
      for {
        lrn       <- arbitrary[LocalReferenceNumber]
        createdAt <- arbitrary[LocalDateTime]
        expires   <- Gen.chooseNum(1, 30)
      } yield DepartureUserAnswerSummary(lrn, createdAt, expires)
    }

  implicit lazy val arbitraryDraftDeparture: Arbitrary[DeparturesSummary] = Arbitrary {
    listWithMaxLength[DepartureUserAnswerSummary](9).map(DeparturesSummary(0, 0, _))
  }

  implicit lazy val arbitraryFunctionalError: Arbitrary[models.departureP5.FunctionalError] =
    Arbitrary {
      for {
        errorPointer           <- nonEmptyString
        errorCode              <- nonEmptyString
        errorReason            <- nonEmptyString
        originalAttributeValue <- Gen.option(nonEmptyString)
      } yield models.departureP5.FunctionalError(errorPointer, errorCode, errorReason, originalAttributeValue)
    }

  lazy val arbitraryAmendableFunctionalError: Arbitrary[models.departureP5.FunctionalError] =
    Arbitrary {
      for {
        errorPointer           <- nonEmptyString
        errorCode              <- nonEmptyString
        errorReason            <- nonEmptyString
        originalAttributeValue <- Gen.option(nonEmptyString)
      } yield models.departureP5.FunctionalError(s"/CC015C/$errorPointer", errorCode, errorReason, originalAttributeValue)
    }

  implicit lazy val arbitraryDepartureMessageType: Arbitrary[models.departureP5.DepartureMessageType] =
    Arbitrary {
      Gen.oneOf(models.departureP5.DepartureMessageType.values)
    }

  implicit lazy val arbitraryDepartureMessage: Arbitrary[models.departureP5.DepartureMessage] =
    Arbitrary {
      for {
        received    <- arbitrary[LocalDateTime]
        messageType <- arbitrary[models.departureP5.DepartureMessageType]
        bodyPath    <- nonEmptyString
      } yield models.departureP5.DepartureMessage(received, messageType, bodyPath)
    }

}
// scalastyle:on magic.number
