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
import viewModels._
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination._
import viewModels.paginationP5.{DraftsPaginationViewModel, MetaData => MetaDataP5}

import java.time.{LocalDate, LocalTime}

trait ViewModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryViewAllArrivalMovementsViewModel: Arbitrary[ViewAllArrivalMovementsViewModel] =
    Arbitrary {
      for {
        viewArrivals        <- listWithMaxLength[ViewArrival]()
        paginationViewModel <- arbitrary[MovementsPaginationViewModel]
      } yield ViewAllArrivalMovementsViewModel(viewArrivals, paginationViewModel)
    }

  implicit lazy val arbitraryViewAllDepartureMovementsViewModel: Arbitrary[ViewAllDepartureMovementsViewModel] =
    Arbitrary {
      for {
        viewDepartures      <- listWithMaxLength[ViewDeparture]()
        paginationViewModel <- arbitrary[MovementsPaginationViewModel]
      } yield ViewAllDepartureMovementsViewModel(viewDepartures, paginationViewModel)
    }

  implicit lazy val arbitraryPaginationViewModel: Arbitrary[MovementsPaginationViewModel] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
        href                     <- nonEmptyString
      } yield MovementsPaginationViewModel(totalNumberOfMovements, numberOfMovementsPerPage, currentPage, href)
    }

  implicit lazy val arbitraryMetaData: Arbitrary[MetaData] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
      } yield MetaData(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)
    }

  implicit lazy val arbitraryMetaDataP5: Arbitrary[MetaDataP5] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
      } yield MetaDataP5(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)
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

  implicit lazy val arbitraryPaginationViewModelP5: Arbitrary[DraftsPaginationViewModel] =
    Arbitrary {
      for {
        totalNumberOfMovements   <- Gen.choose(0, Int.MaxValue)
        numberOfMovementsPerPage <- Gen.choose(1, Int.MaxValue)
        currentPage              <- Gen.choose(1, Int.MaxValue)
        href                     <- nonEmptyString
      } yield DraftsPaginationViewModel(totalNumberOfMovements, currentPage, numberOfMovementsPerPage, href)
    }

  implicit val arbitraryAllDraftDeparturesViewModel: Arbitrary[AllDraftDeparturesViewModel] =
    Arbitrary {
      for {
        draftDepartures <- arbitrary[DeparturesSummary]
        pageSize        <- arbitrary[Int]
        lrn             <- Gen.option(arbitrary[String])
        url             <- nonEmptyString
        pagination      <- arbitrary[DraftsPaginationViewModel]
      } yield AllDraftDeparturesViewModel(draftDepartures, pageSize, lrn, url, pagination)
    }
}
