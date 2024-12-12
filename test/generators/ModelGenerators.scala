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

import models.*
import models.FunctionalError.{FunctionalErrorWithSection, FunctionalErrorWithoutSection}
import models.FunctionalErrors.{FunctionalErrorsWithSection, FunctionalErrorsWithoutSection}
import models.arrivalP5.{ArrivalMovement, ArrivalMovements}
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import models.departureP5.{BusinessRejectionType, DepartureMovement, DepartureMovements}
import models.referenceData.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN, numChar, posNum}
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call

import java.time.*

// scalastyle:off magic.number
trait ModelGenerators {
  self: Generators =>

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

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- alphaNumericWithMaxLength(22)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryAvailability: Arbitrary[Availability] =
    Arbitrary {
      Gen.oneOf(Availability.NonEmpty, Availability.Empty, Availability.Unavailable)
    }

  implicit lazy val arbitraryFeature: Arbitrary[Feature] =
    Arbitrary {
      for {
        availability <- arbitrary[Availability]
        href         <- nonEmptyString
      } yield Feature(availability, href)
    }

  implicit lazy val arbitraryArrivalMovement: Arbitrary[ArrivalMovement] =
    Arbitrary {
      for {
        arrivalId <- arbitrary[String]
        mrn       <- arbitrary[String]
        updated   <- arbitrary[LocalDateTime]
      } yield ArrivalMovement(arrivalId, mrn, updated)
    }

  implicit lazy val arbitraryArrivalMovements: Arbitrary[ArrivalMovements] =
    Arbitrary {
      for {
        arrivalMovements <- Gen.nonEmptyListOf(arbitrary[ArrivalMovement])
        totalCount       <- posNum[Int]
      } yield ArrivalMovements(arrivalMovements, totalCount)
    }

  implicit lazy val arbitraryDepartureMovement: Arbitrary[DepartureMovement] =
    Arbitrary {
      for {
        departureId <- arbitrary[String]
        mrn         <- arbitrary[String]
        lrn         <- arbitrary[LocalReferenceNumber]
        updated     <- arbitrary[LocalDateTime]
      } yield DepartureMovement(departureId, Some(mrn), lrn.value, updated)
    }

  implicit lazy val arbitraryDepartureMovements: Arbitrary[DepartureMovements] =
    Arbitrary {
      for {
        departureMovements <- Gen.nonEmptyListOf(arbitrary[DepartureMovement])
        totalCount         <- posNum[Int]
      } yield DepartureMovements(departureMovements, totalCount)
    }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- arbitrary[String]
        name        <- arbitrary[String]
        phoneNumber <- arbitrary[Option[String]]
      } yield CustomsOffice(id, name, phoneNumber)
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
    for {
      totalMovements         <- positiveInts
      totalMatchingMovements <- positiveInts
      userAnswers            <- listWithMaxLength[DepartureUserAnswerSummary]()
    } yield DeparturesSummary(
      totalMovements = totalMovements,
      totalMatchingMovements = totalMatchingMovements,
      userAnswers = userAnswers
    )
  }

  implicit lazy val arbitraryInvalidDataItem: Arbitrary[InvalidDataItem] =
    Arbitrary {
      for {
        value <- nonEmptyString
      } yield models.InvalidDataItem(value)
    }

  implicit lazy val arbitraryFunctionalErrorWithSection: Arbitrary[FunctionalErrorWithSection] =
    Arbitrary {
      for {
        error           <- nonEmptyString
        businessRuleId  <- nonEmptyString
        section         <- Gen.option(nonEmptyString)
        invalidDataItem <- arbitrary[InvalidDataItem]
        invalidAnswer   <- Gen.option(nonEmptyString)
      } yield FunctionalErrorWithSection(error, businessRuleId, section, invalidDataItem, invalidAnswer)
    }

  implicit lazy val arbitraryFunctionalErrorWithoutSection: Arbitrary[FunctionalErrorWithoutSection] =
    Arbitrary {
      for {
        error           <- nonEmptyString
        businessRuleId  <- nonEmptyString
        invalidDataItem <- arbitrary[InvalidDataItem]
        invalidAnswer   <- Gen.option(nonEmptyString)
      } yield FunctionalErrorWithoutSection(error, businessRuleId, invalidDataItem, invalidAnswer)
    }

  implicit lazy val arbitraryFunctionalErrorsWithSection: Arbitrary[FunctionalErrorsWithSection] =
    Arbitrary {
      for {
        value <- listWithMaxLength[FunctionalErrorWithSection]()
      } yield FunctionalErrorsWithSection(value)
    }

  implicit lazy val arbitraryFunctionalErrorsWithoutSection: Arbitrary[FunctionalErrorsWithoutSection] =
    Arbitrary {
      for {
        value <- listWithMaxLength[FunctionalErrorWithoutSection]()
      } yield FunctionalErrorsWithoutSection(value)
    }

  implicit lazy val arbitraryDepartureMessageType: Arbitrary[models.departureP5.DepartureMessageType] =
    Arbitrary {
      Gen.oneOf(models.departureP5.DepartureMessageType.values)
    }

  implicit lazy val arbitraryDepartureMessage: Arbitrary[models.departureP5.DepartureMessage] =
    Arbitrary {
      for {
        messageId   <- nonEmptyString
        received    <- arbitrary[LocalDateTime]
        messageType <- arbitrary[models.departureP5.DepartureMessageType]
        status      <- arbitrary[MessageStatus]
      } yield models.departureP5.DepartureMessage(messageId, received, messageType, status)
    }

  implicit lazy val arbitraryMessageStatus: Arbitrary[MessageStatus] =
    Arbitrary {
      Gen.oneOf(
        MessageStatus.Received,
        MessageStatus.Pending,
        MessageStatus.Processing,
        MessageStatus.Success,
        MessageStatus.Failed
      )
    }

  implicit lazy val arbitraryBusinessRejectionType: Arbitrary[BusinessRejectionType] = {
    import models.departureP5.BusinessRejectionType.*
    Arbitrary {
      for {
        value <- nonEmptyString
        businessRejectionType <- Gen.oneOf(
          AmendmentRejection,
          InvalidationRejection,
          DeclarationRejection,
          OtherBusinessRejectionType(value)
        )
      } yield businessRejectionType
    }
  }

  implicit lazy val arbitraryDepartureBusinessRejectionType: Arbitrary[DepartureBusinessRejectionType] = {
    import models.departureP5.BusinessRejectionType.*
    Arbitrary {
      Gen.oneOf(
        AmendmentRejection,
        DeclarationRejection
      )
    }
  }

  implicit lazy val arbitrarySort: Arbitrary[Sort] =
    Arbitrary {
      Gen.oneOf(
        Sort.SortByLRNAsc,
        Sort.SortByLRNDesc,
        Sort.SortByCreatedAtAsc,
        Sort.SortByCreatedAtDesc
      )
    }

  implicit lazy val arbitraryGuaranteeReference: Arbitrary[models.GuaranteeReference] =
    Arbitrary {
      for {
        grn               <- nonEmptyString
        invalidGuarantees <- listWithMaxLength[InvalidGuaranteeReason]()
      } yield models.GuaranteeReference(grn, invalidGuarantees)
    }

  implicit lazy val arbitraryInvalidGuaranteeReason: Arbitrary[models.InvalidGuaranteeReason] =
    Arbitrary {
      for {
        error              <- nonEmptyString
        furtherInformation <- Gen.option(nonEmptyString)
      } yield models.InvalidGuaranteeReason(error, furtherInformation)
    }
}

// scalastyle:on magic.number
