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

import models.{DeparturesSummary, Sort}
import models.FunctionalErrors.{FunctionalErrorsWithSection, FunctionalErrorsWithoutSection}
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import models.departureP5.GuaranteeReferenceTable
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Content
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import viewModels.*
import viewModels.P5.arrival.*
import viewModels.P5.departure.*
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.MetaData
import viewModels.sections.Section
import viewModels.sections.Section.{AccordionSection, StaticSection}

import java.time.{LocalDate, LocalTime}
import javax.xml.datatype.XMLGregorianCalendar

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

  /*implicit def arbitraryViewAllArrivalMovementsP5ViewModel(implicit messages: Messages): Arbitrary[ViewAllArrivalMovementsP5ViewModel] =
    Arbitrary {
      for {
        viewArrivals <- distinctListWithMaxLength[ViewArrivalP5, LocalDate]()(_.updatedDate)
        searchParam  <- Gen.option(nonEmptyString)
        currentPage  <- positiveBigDecimals
      } yield ViewAllArrivalMovementsP5ViewModel(viewArrivals, None)
    }*/

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

  implicit val arbitraryViewArrivalP5: Arbitrary[ViewArrivalP5] =
    Arbitrary {
      for {
        date    <- arbitrary[LocalDate]
        time    <- arbitrary[LocalTime]
        mrn     <- stringsWithMaxLength(17: Int)
        status  <- nonEmptyString
        actions <- listWithMaxLength[ViewMovementAction]()
      } yield ViewArrivalP5(date, time, mrn, status, actions)
    }

  implicit val arbitraryViewDepartureP5: Arbitrary[ViewDepartureP5] =
    Arbitrary {
      for {
        date    <- arbitrary[LocalDate]
        time    <- arbitrary[LocalTime]
        lrn     <- stringsWithMaxLength(17: Int)
        status  <- nonEmptyString
        actions <- listWithMaxLength[ViewMovementAction]()
      } yield ViewDepartureP5(date, time, lrn, status, actions)
    }

  implicit def arbitraryArrivalNotificationWithFunctionalErrorsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ArrivalNotificationWithFunctionalErrorsP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        mrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield ArrivalNotificationWithFunctionalErrorsP5ViewModel(
        functionalErrors = functionalErrors,
        mrn = mrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryUnloadingRemarkWithFunctionalErrorsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[UnloadingRemarkWithFunctionalErrorsP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        mrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield UnloadingRemarkWithFunctionalErrorsP5ViewModel(
        functionalErrors = functionalErrors,
        mrn = mrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryRejectionMessageP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[RejectionMessageP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        lrn                   <- nonEmptyString
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield RejectionMessageP5ViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        businessRejectionType = businessRejectionType,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryReviewDepartureErrorsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewDepartureErrorsP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithSection]
        lrn                   <- nonEmptyString
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield ReviewDepartureErrorsP5ViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        businessRejectionType = businessRejectionType,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryReviewCancellationErrorsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewCancellationErrorsP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield ReviewCancellationErrorsP5ViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryReviewPrelodgedDeclarationErrorsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ReviewPrelodgedDeclarationErrorsP5ViewModel] =
    Arbitrary {
      for {
        functionalErrors      <- arbitrary[FunctionalErrorsWithoutSection]
        lrn                   <- nonEmptyString
        currentPage           <- Gen.option(positiveInts)
        numberOfErrorsPerPage <- positiveInts
        href                  <- arbitrary[Call]
      } yield ReviewPrelodgedDeclarationErrorsP5ViewModel(
        functionalErrors = functionalErrors,
        lrn = lrn,
        currentPage = currentPage,
        numberOfErrorsPerPage = numberOfErrorsPerPage,
        href = href
      )
    }

  implicit def arbitraryViewAllDepartureMovementsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ViewAllDepartureMovementsP5ViewModel] =
    Arbitrary {
      for {
        movementsAndMessages <- listWithMaxLength[ViewDepartureP5]()
        searchParam          <- Gen.option(nonEmptyString)
        currentPage          <- positiveInts
        numberOfItemsPerPage <- positiveInts
      } yield ViewAllDepartureMovementsP5ViewModel(
        movementsAndMessages = movementsAndMessages,
        searchParam = searchParam,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage
      )
    }

  implicit def arbitraryViewAllArrivalMovementsP5ViewModel(implicit
    messages: Messages
  ): Arbitrary[ViewAllArrivalMovementsP5ViewModel] =
    Arbitrary {
      for {
        movementsAndMessages <- listWithMaxLength[ViewArrivalP5]()
        searchParam          <- Gen.option(nonEmptyString)
        currentPage          <- positiveInts
        numberOfItemsPerPage <- positiveInts
      } yield ViewAllArrivalMovementsP5ViewModel(
        movementsAndMessages = movementsAndMessages,
        searchParam = searchParam,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage
      )
    }

  implicit def arbitraryAllDraftDeparturesViewModel(implicit messages: Messages): Arbitrary[AllDraftDeparturesViewModel] =
    Arbitrary {
      for {
        departures           <- arbitrary[DeparturesSummary]
        lrn                  <- Gen.option(nonEmptyString)
        currentPage          <- positiveInts
        numberOfItemsPerPage <- positiveInts
        sortParams           <- Gen.option(arbitrary[Sort])
      } yield AllDraftDeparturesViewModel(
        departures = departures,
        lrn = lrn,
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage,
        sortParams = sortParams
      )
    }

  implicit val arbitraryDepartureDeclarationErrorsP5ViewModel: Arbitrary[DepartureDeclarationErrorsP5ViewModel] =
    Arbitrary {
      for {
        lrn                   <- nonEmptyString
        mrn                   <- Gen.option(nonEmptyString)
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
      } yield DepartureDeclarationErrorsP5ViewModel(lrn, mrn, businessRejectionType)
    }

  /*implicit val arbitraryRejectionMessageP5ViewModel: Arbitrary[RejectionMessageP5ViewModel] =
    Arbitrary {
      for {
        tableRows             <- listWithMaxLength()(arbitraryTableRows)
        lrn                   <- nonEmptyString
        multipleErrors        <- arbitrary[Boolean]
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType]
      } yield RejectionMessageP5ViewModel(tableRows, lrn, multipleErrors, businessRejectionType)
    }*/

  implicit val arbitraryGuaranteeRejectedP5ViewModel: Arbitrary[GuaranteeRejectedP5ViewModel] =
    Arbitrary {
      for {
        tables         <- arbitrary[Seq[GuaranteeReferenceTable]]
        lrn            <- nonEmptyString
        mrn            <- nonEmptyString
        acceptanceDate <- arbitrary[XMLGregorianCalendar]
      } yield GuaranteeRejectedP5ViewModel(tables, lrn, mrn, acceptanceDate)
    }

  implicit val arbitraryGuaranteeRejectedNotAmendableP5ViewModel: Arbitrary[GuaranteeRejectedNotAmendableP5ViewModel] =
    Arbitrary {
      for {
        tables         <- arbitrary[Seq[GuaranteeReferenceTable]]
        lrn            <- nonEmptyString
        mrn            <- nonEmptyString
        acceptanceDate <- arbitrary[XMLGregorianCalendar]
      } yield GuaranteeRejectedNotAmendableP5ViewModel(tables, lrn, mrn, acceptanceDate)
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

  implicit lazy val arbitraryTable: Arbitrary[Table] = Arbitrary {
    for {
      rows              <- arbitrary[Seq[Seq[TableRow]]]
      head              <- Gen.option(arbitrary[Seq[HeadCell]])
      caption           <- Gen.option(arbitrary[String])
      captionClasses    <- nonEmptyString
      firstCellIsHeader <- arbitrary[Boolean]
      classes           <- nonEmptyString
    } yield Table(rows, head, caption, captionClasses, firstCellIsHeader, classes, Map.empty)
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
