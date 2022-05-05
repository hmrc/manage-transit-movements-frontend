/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import viewModels.pagination._
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}

trait ViewModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryViewAllArrivalMovementsViewModel: Arbitrary[ViewAllArrivalMovementsViewModel] =
    Arbitrary {
      for {
        viewArrivals        <- listWithMaxLength[ViewArrival]()
        paginationViewModel <- arbitrary[PaginationViewModel]
      } yield ViewAllArrivalMovementsViewModel(viewArrivals, paginationViewModel)
    }

  implicit lazy val arbitraryPaginationViewModel: Arbitrary[PaginationViewModel] =
    Arbitrary {
      for {
        results  <- arbitrary[MetaData]
        previous <- arbitrary[Option[Previous]]
        next     <- arbitrary[Option[Next]]
        items    <- arbitrary[Items]
      } yield PaginationViewModel(results, previous, next, items)
    }

  implicit lazy val arbitraryMetaData: Arbitrary[MetaData] =
    Arbitrary {
      for {
        from        <- arbitrary[Int]
        to          <- arbitrary[Int]
        count       <- arbitrary[Int]
        currentPage <- arbitrary[Int]
        totalPages  <- arbitrary[Int]
      } yield MetaData(from, to, count, currentPage, totalPages)
    }

  implicit lazy val arbitraryPrevious: Arbitrary[Previous] =
    Arbitrary {
      for {
        href <- Gen.alphaNumStr
      } yield Previous(href)
    }

  implicit lazy val arbitraryNext: Arbitrary[Next] =
    Arbitrary {
      for {
        href <- Gen.alphaNumStr
      } yield Next(href)
    }

  implicit lazy val arbitraryItems: Arbitrary[Items] =
    Arbitrary {
      for {
        items           <- listWithMaxLength[Item]()
        firstItemDotted <- arbitrary[Boolean]
        lastItemDotted  <- arbitrary[Boolean]
      } yield Items(items, firstItemDotted, lastItemDotted)
    }

  implicit lazy val arbitraryItem: Arbitrary[Item] =
    Arbitrary {
      for {
        pageNumber <- arbitrary[Int]
        href       <- Gen.alphaNumStr
        selected   <- arbitrary[Boolean]
      } yield Item(pageNumber, href, selected)
    }
}
