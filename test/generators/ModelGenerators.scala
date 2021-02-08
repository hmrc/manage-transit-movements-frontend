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

package generators

import java.time._

import models._
import models.departure.{ControlResult, NoReleaseForTransitMessage, ResultsOfControl}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN, numChar}
import org.scalacheck.{Arbitrary, Gen}
import models.ErrorType
import models.ErrorType.GenericError
import models.arrival.XMLSubmissionNegativeAcknowledgementMessage
import viewModels.{ViewArrivalMovements, ViewDeparture, ViewDepartureMovements, ViewMovement}

trait ModelGenerators {
  self: Generators =>

  implicit val arbitrarylocalDate: Arbitrary[LocalDate] = {
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

  implicit lazy val arbitraryArrivalId: Arbitrary[ArrivalId] = {
    Arbitrary {
      for {
        length        <- choose(1, 9)
        listOfCharNum <- listOfN(length, numChar)
      } yield ArrivalId(listOfCharNum.mkString.toInt)
    }
  }

  implicit lazy val arbitraryDepartureId: Arbitrary[DepartureId] = {
    Arbitrary {
      for {
        length        <- choose(1, 9)
        listOfCharNum <- listOfN(length, numChar)
      } yield DepartureId(listOfCharNum.mkString.toInt)
    }
  }

  implicit val arbitraryArrival: Arbitrary[Arrival] = {
    Arbitrary {
      for {
        arrivalId <- arbitrary[ArrivalId]
        date      <- arbitrary[LocalDateTime]
        time      <- arbitrary[LocalDateTime]
        status    <- Gen.oneOf(Seq("GoodsReleased", "UnloadingPermission", "ArrivalSubmitted", "Rejection"))
        mrn       <- stringsWithMaxLength(17)
      } yield Arrival(arrivalId, date, time, status, mrn)
    }
  }

  implicit val arbitraryDeparture: Arbitrary[Departure] = {
    Arbitrary {
      for {
        departureID          <- arbitrary[DepartureId]
        updated              <- arbitrary[LocalDateTime]
        localReferenceNumber <- arbitrary[LocalReferenceNumber]
        status               <- arbitrary[String]
      } yield Departure(departureID, updated, localReferenceNumber, status)
    }
  }

  implicit val arbitraryViewMovementAction: Arbitrary[ViewMovementAction] = {
    Arbitrary {
      for {
        href <- arbitrary[String]
        key  <- arbitrary[String]
      } yield ViewMovementAction(href, key)
    }
  }

  implicit val arbitraryViewMovement: Arbitrary[ViewMovement] = {
    Arbitrary {
      for {
        date    <- arbitrary[LocalDate]
        time    <- arbitrary[LocalTime]
        status  <- arbitrary[String]
        mrn     <- stringsWithMaxLength(17)
        actions <- listOfN(4, arbitrary[ViewMovementAction])
      } yield ViewMovement(date, time, status, mrn, actions)
    }
  }

  implicit val arbitraryViewDeparture: Arbitrary[ViewDeparture] = {
    Arbitrary {
      for {
        updatedDate          <- arbitrary[LocalDate]
        updatedTime          <- arbitrary[LocalTime]
        localReferenceNumber <- arbitrary[LocalReferenceNumber]
        status               <- arbitrary[String]
        actions              <- listOfN(4, arbitrary[ViewMovementAction])
      } yield new ViewDeparture(updatedDate, updatedTime, localReferenceNumber, status, actions)
    }
  }

  implicit val arbitraryViewArrivalMovements: Arbitrary[ViewArrivalMovements] =
    Arbitrary {
      for {
        seqOfViewMovements <- listOfN(10, arbitrary[ViewMovement])
      } yield ViewArrivalMovements(seqOfViewMovements)
    }

  implicit val arbitraryViewDepartureMovements: Arbitrary[ViewDepartureMovements] =
    Arbitrary {
      for {
        seqOfViewDepartureMovements <- listOfN(10, arbitrary[ViewDeparture])
      } yield ViewDepartureMovements(seqOfViewDepartureMovements)
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
        description <- Gen.option(nonEmptyString)
      } yield ResultsOfControl(indicator, description)
    }

  implicit val arbitraryNoReleaseForTransitMessage: Arbitrary[NoReleaseForTransitMessage] = Arbitrary {
    for {
      mrn                        <- nonEmptyString
      noReleaseMotivation        <- Gen.option(nonEmptyString)
      totalNumberOfItems         <- arbitrary[Int]
      officeOfDepartureRefNumber <- arbitrary[String]
      controlResult              <- arbitrary[ControlResult]
      resultsOfControl           <- Gen.option(listWithMaxLength(ResultsOfControl.maxResultsOfControl, arbitrary[ResultsOfControl]))
    } yield
      new NoReleaseForTransitMessage(
        mrn                        = mrn,
        noReleaseMotivation        = noReleaseMotivation,
        totalNumberOfItems         = totalNumberOfItems,
        officeOfDepartureRefNumber = officeOfDepartureRefNumber,
        controlResult              = controlResult,
        resultsOfControl           = resultsOfControl
      )
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
        pointer       <- arbitrary[String]
        reason        <- arbitrary[Option[String]]
        originalValue <- arbitrary[Option[String]]
      } yield FunctionalError(errorType, ErrorPointer(pointer), reason, originalValue)
    }

  implicit lazy val arbitraryXMLSubmissionNegativeAcknowledgementMessage: Arbitrary[XMLSubmissionNegativeAcknowledgementMessage] =
    Arbitrary {

      for {
        mrn   <- nonEmptyString
        error <- arbitrary[FunctionalError]
      } yield XMLSubmissionNegativeAcknowledgementMessage(mrn, error)
    }
}
