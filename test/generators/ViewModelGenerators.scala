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

import models.{DeparturesSummary, LocalReferenceNumber}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Content
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.arrival.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels._
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.{ListPaginationViewModel, MetaData}
import viewModels.sections.Section

import java.time.{LocalDate, LocalTime}

trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

  implicit lazy val arbitraryViewAllArrivalMovementsViewModel: Arbitrary[ViewAllArrivalMovementsViewModel] =
    Arbitrary {
      for {
        viewArrivals        <- listWithMaxLength[ViewArrival]()
        paginationViewModel <- arbitrary[ListPaginationViewModel]
      } yield ViewAllArrivalMovementsViewModel(viewArrivals, paginationViewModel)
    }

  implicit lazy val arbitraryViewAllArrivalMovementsP5ViewModel: Arbitrary[ViewAllArrivalMovementsP5ViewModel] =
    Arbitrary {
      for {
        viewArrivals        <- listWithMaxLength[ViewArrivalP5]()
        paginationViewModel <- arbitrary[ListPaginationViewModel]
      } yield ViewAllArrivalMovementsP5ViewModel(viewArrivals, paginationViewModel, None)
    }

  implicit lazy val arbitraryViewAllDepartureMovementsP5ViewModel: Arbitrary[ViewAllDepartureMovementsP5ViewModel] =
    Arbitrary {
      for {
        viewArrivals        <- listWithMaxLength[ViewDepartureP5]()
        paginationViewModel <- arbitrary[ListPaginationViewModel]
      } yield ViewAllDepartureMovementsP5ViewModel(viewArrivals, paginationViewModel, None)
    }

  implicit lazy val arbitraryViewAllDepartureMovementsViewModel: Arbitrary[ViewAllDepartureMovementsViewModel] =
    Arbitrary {
      for {
        viewDepartures      <- listWithMaxLength[ViewDeparture]()
        paginationViewModel <- arbitrary[ListPaginationViewModel]
      } yield ViewAllDepartureMovementsViewModel(viewDepartures, paginationViewModel)
    }

  implicit lazy val arbitraryPaginationViewModel: Arbitrary[ListPaginationViewModel] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
        href                     <- nonEmptyString
      } yield ListPaginationViewModel(totalNumberOfMovements, numberOfMovementsPerPage, currentPage, href)
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
        date    <- arbitrary[LocalDate]
        time    <- arbitrary[LocalTime]
        mrn     <- stringsWithMaxLength(17: Int)
        status  <- nonEmptyString
        actions <- listWithMaxLength[ViewMovementAction]()
      } yield ViewArrival(date, time, mrn, status, actions)
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

  implicit val arbitraryViewDeparture: Arbitrary[ViewDeparture] =
    Arbitrary {
      for {
        updatedDate          <- arbitrary[LocalDate]
        updatedTime          <- arbitrary[LocalTime]
        localReferenceNumber <- arbitrary[LocalReferenceNumber]
        status               <- nonEmptyString
        actions              <- listWithMaxLength[ViewMovementAction]()
      } yield new ViewDeparture(updatedDate, updatedTime, localReferenceNumber, status, actions)
    }

  implicit val arbitraryViewArrivalMovements: Arbitrary[ViewArrivalMovements] =
    Arbitrary {
      for {
        seqOfViewMovements <- listWithMaxLength[ViewArrival]()
      } yield ViewArrivalMovements(seqOfViewMovements)
    }

  implicit val arbitraryViewDepartureMovements: Arbitrary[ViewDepartureMovements] =
    Arbitrary {
      for {
        seqOfViewDepartureMovements <- listWithMaxLength[ViewDeparture]()
      } yield ViewDepartureMovements(seqOfViewDepartureMovements)
    }

  implicit val arbitraryAllDraftDeparturesViewModel: Arbitrary[AllDraftDeparturesViewModel] =
    Arbitrary {
      for {
        draftDepartures <- arbitrary[DeparturesSummary]
        pageSize        <- arbitrary[Int]
        lrn             <- Gen.option(nonEmptyString)
        url             <- nonEmptyString
        pagination      <- arbitrary[ListPaginationViewModel]
      } yield AllDraftDeparturesViewModel(draftDepartures, pageSize, lrn, url, pagination)
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
    } yield Section(sectionTitle, rows)
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
