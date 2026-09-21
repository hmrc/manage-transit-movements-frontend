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

import models.FunctionalErrors.{FunctionalErrorsWithSection, FunctionalErrorsWithoutSection}
import models.departure.BusinessRejectionType.DepartureBusinessRejectionType
import models.{DeparturesSummary, GuaranteeReference}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Content
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.*
import viewModels.arrival.*
import viewModels.departure.*
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.MetaData
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

import java.time.LocalDateTime

trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

  lazy val arbitraryStaticSectionNoChildren: Arbitrary[StaticSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield StaticSection(sectionTitle, rows)
  }

  implicit lazy val arbitraryStaticSection: Arbitrary[StaticSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(0, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
      children     <- Gen.containerOf[Seq, AccordionSection](arbitrary[AccordionSection])
    } yield StaticSection(sectionTitle, rows, children)
  }

  implicit lazy val arbitraryAccordionSection: Arbitrary[AccordionSection] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield AccordionSection(sectionTitle, rows)
  }

  implicit lazy val arbitraryStaticSections: Arbitrary[List[StaticSection]] = Arbitrary {
    distinctListWithMaxLength[StaticSection, Option[String]]()(_.sectionTitle)
  }

  implicit lazy val arbitraryAccordionSections: Arbitrary[List[AccordionSection]] = Arbitrary {
    distinctListWithMaxLength[AccordionSection, Option[String]]()(_.sectionTitle)
  }

  implicit lazy val arbitraryMetaData: Arbitrary[MetaData] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
      } yield MetaData(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)
    }

  implicit lazy val arbitraryHtml: Arbitrary[Html] = Arbitrary {
    for {
      text <- nonEmptyString
    } yield Html(text)
  }

  implicit lazy val arbitraryFormError: Arbitrary[FormError] = Arbitrary {
    for {
      key     <- nonEmptyString
      message <- nonEmptyString
    } yield FormError(key, message)
  }

  implicit val arbitraryViewMovementAction: Arbitrary[ViewMovementAction] =
    Arbitrary {
      for {
        href <- nonEmptyString
        key  <- nonEmptyString
      } yield ViewMovementAction(href, key)
    }

  implicit val arbitraryViewArrival: Arbitrary[ViewArrival] =
    Arbitrary {
      for {
        dateTime <- arbitrary[LocalDateTime]
        mrn      <- stringsWithMaxLength(17: Int)
        status   <- nonEmptyString
        actions  <- listWithMaxLength[ViewMovementAction]()
      } yield ViewArrival(dateTime, mrn, status, actions)
    }

  implicit val arbitraryViewDeparture: Arbitrary[ViewDeparture] =
    Arbitrary {
      for {
        dateTime <- arbitrary[LocalDateTime]
        lrn      <- stringsWithMaxLength(17: Int)
        status   <- nonEmptyString
        actions  <- listWithMaxLength[ViewMovementAction]()
      } yield ViewDeparture(dateTime, lrn, status, actions)
    }

  implicit def arbitraryArrivalNotificationWithFunctionalErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[ArrivalNotificationWithFunctionalErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        mrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        arrivalId             <- nonEmptyString
        messageId             <- nonEmptyString
      } yield ArrivalNotificationWithFunctionalErrorsViewModel(
        functionalErrors = functionalErrors,
        mrn = mrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        arrivalId = arrivalId,
        messageId = messageId
      )
    }

  implicit def arbitraryUnloadingRemarkWithFunctionalErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[UnloadingRemarkWithFunctionalErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        mrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        arrivalId             <- nonEmptyString
        messageId             <- nonEmptyString
      } yield UnloadingRemarkWithFunctionalErrorsViewModel(
        functionalErrors = functionalErrors,
        mrn = mrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        arrivalId = arrivalId,
        messageId = messageId
      )
    }

  implicit def arbitraryRejectionMessageViewModel(implicit
    messages: Messages
  ): Arbitrary[RejectionMessageViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithSection]
        lrn                   <- nonEmptyString
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield RejectionMessageViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        businessRejectionType = businessRejectionType,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit def arbitraryReviewDepartureErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewDepartureErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithSection]
        lrn                   <- nonEmptyString
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield ReviewDepartureErrorsViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        businessRejectionType = businessRejectionType,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit def arbitraryReviewDepartureAmendmentErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewDepartureAmendmentErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield ReviewDepartureAmendmentErrorsViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit def arbitraryReviewCancellationErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewCancellationErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield ReviewCancellationErrorsViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit def arbitraryReviewPrelodgedDeclarationErrorsViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewPrelodgedDeclarationErrorsViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield ReviewPrelodgedDeclarationErrorsViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit def arbitraryViewAllDepartureMovementsViewModel(implicit
    messages: Messages
  ): Arbitrary[ViewAllDepartureMovementsViewModel] =
    Arbitrary {
      for {
        movementsAndMessages   <- listWithMaxLength[ViewDeparture]()
        searchParam            <- Gen.option(nonEmptyString)
        currentPage            <- positiveInts
        numberOfItemsPerPage   <- positiveInts
        totalNumberOfMovements <- Gen.choose(1, (currentPage - 1) * numberOfItemsPerPage)
      } yield ViewAllDepartureMovementsViewModel(
        movementsAndMessages = movementsAndMessages,
        searchParam = searchParam,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage,
        totalNumberOfMovements = totalNumberOfMovements
      )
    }

  implicit def arbitraryViewAllArrivalMovementsViewModel(implicit
    messages: Messages
  ): Arbitrary[ViewAllArrivalMovementsViewModel] =
    Arbitrary {
      for {
        movementsAndMessages   <- listWithMaxLength[ViewArrival]()
        searchParam            <- Gen.option(nonEmptyString)
        currentPage            <- positiveInts
        numberOfItemsPerPage   <- positiveInts
        totalNumberOfMovements <- Gen.choose(1, (currentPage - 1) * numberOfItemsPerPage)
      } yield ViewAllArrivalMovementsViewModel(
        movementsAndMessages = movementsAndMessages,
        searchParam = searchParam,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage,
        totalNumberOfMovements = totalNumberOfMovements
      )
    }

  implicit def arbitraryAllDraftDeparturesViewModel(implicit messages: Messages): Arbitrary[AllDraftDeparturesViewModel] =
    Arbitrary {
      for {
        departures           <- arbitrary[DeparturesSummary]
        searchParam          <- Gen.option(nonEmptyString)
        currentPage          <- positiveInts
        numberOfItemsPerPage <- positiveInts
      } yield AllDraftDeparturesViewModel(
        departures = departures,
        searchParam = searchParam,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage
      )
    }

  implicit val arbitraryDepartureDeclarationErrorsViewModel: Arbitrary[DepartureDeclarationErrorsViewModel] =
    Arbitrary {
      for {
        lrn                   <- nonEmptyString
        mrn                   <- Gen.option(nonEmptyString)
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
      } yield DepartureDeclarationErrorsViewModel(lrn, mrn, businessRejectionType)
    }

  implicit val arbitraryAmendDeclarationErrorsViewModel: Arbitrary[AmendDeclarationErrorsViewModel] =
    Arbitrary {
      for {
        lrn <- nonEmptyString
        mrn <- Gen.option(nonEmptyString)
      } yield AmendDeclarationErrorsViewModel(lrn, mrn)
    }

  implicit val arbitraryGuaranteeRejectedViewModel: Arbitrary[GuaranteeRejectedViewModel] =
    Arbitrary {
      for {
        guaranteeReferences       <- listWithMaxLength[GuaranteeReference]()
        lrn                       <- nonEmptyString
        mrn                       <- nonEmptyString
        declarationAcceptanceDate <- nonEmptyString
        paragraph1                <- nonEmptyString
        paragraph2                <- nonEmptyString
        link                      <- nonEmptyString
      } yield GuaranteeRejectedViewModel(
        guaranteeReferences,
        lrn,
        mrn,
        declarationAcceptanceDate,
        paragraph1,
        paragraph2,
        link
      )
    }

  implicit val arbitraryGuaranteeRejectedNotAmendableViewModel: Arbitrary[GuaranteeRejectedNotAmendableViewModel] =
    Arbitrary {
      for {
        guaranteeReferences       <- listWithMaxLength[GuaranteeReference]()
        lrn                       <- nonEmptyString
        mrn                       <- nonEmptyString
        declarationAcceptanceDate <- nonEmptyString
        paragraph1                <- nonEmptyString
        paragraph2                <- nonEmptyString
        link                      <- nonEmptyString
      } yield GuaranteeRejectedNotAmendableViewModel(
        guaranteeReferences,
        lrn,
        mrn,
        declarationAcceptanceDate,
        paragraph1,
        paragraph2,
        link
      )
    }

  implicit def arbitraryDeclarationAmendmentRejectionMessageViewModel(implicit
    messages: Messages
  ): Arbitrary[DeclarationAmendmentRejectionMessageViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        departureId           <- nonEmptyString
        messageId             <- nonEmptyString
      } yield DeclarationAmendmentRejectionMessageViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        departureId = departureId,
        messageId = messageId
      )
    }

  implicit lazy val arbitraryText: Arbitrary[Text] = Arbitrary {
    for {
      content <- nonEmptyString
    } yield content.toText
  }

  implicit lazy val arbitraryContent: Arbitrary[Content] = Arbitrary {
    arbitrary[Text]
  }

  implicit lazy val arbitraryKey: Arbitrary[Key] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Key(content, classes)
  }

  implicit lazy val arbitraryValue: Arbitrary[Value] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Value(content, classes)
  }

  implicit lazy val arbitrarySummaryListRow: Arbitrary[SummaryListRow] = Arbitrary {
    for {
      key     <- arbitrary[Key]
      value   <- arbitrary[Value]
      classes <- Gen.alphaNumStr
    } yield SummaryListRow(key, value, classes, None)
  }

  implicit lazy val arbitrarySection: Arbitrary[Section] = Arbitrary {
    for {
      sectionTitle <- nonEmptyString
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield StaticSection(sectionTitle, rows)
  }

  implicit lazy val arbitrarySections: Arbitrary[List[Section]] = Arbitrary {
    listWithMaxLength[Section]().retryUntil {
      sections =>
        val sectionTitles = sections.map(_.sectionTitle)
        sectionTitles.distinct.size == sectionTitles.size
    }
  }

  implicit lazy val arbitraryTableRow: Arbitrary[TableRow] = Arbitrary {
    for {
      content <- nonEmptyString
    } yield TableRow(Text(content))
  }

  implicit lazy val arbitraryTableRows: Arbitrary[List[TableRow]] = Arbitrary {
    listWithMaxLength[TableRow]()
  }

  implicit lazy val arbitraryHeadCell: Arbitrary[HeadCell] = Arbitrary {
    for {
      content <- nonEmptyString
    } yield HeadCell(Text(content))
  }

  implicit lazy val arbitraryHeadCells: Arbitrary[List[HeadCell]] = Arbitrary {
    listWithMaxLength[HeadCell]()
  }

}
