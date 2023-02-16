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

package models

sealed trait Sort {
  val field: String
  val orderBy: String
  val convertParams: String

  override def toString: String = s"$field.$orderBy"
}

object Sort {

  case object SortByLRNAsc extends Sort {
    val field: String   = "lrn"
    val orderBy: String = "ascending"
    val convertParams   = s"$field.$orderBy"
  }

  case object SortByLRNDesc extends Sort {
    val field: String   = "lrn"
    val orderBy: String = "descending"
    val convertParams   = s"$field.$orderBy"
  }

  case object SortByCreatedAtAsc extends Sort {
    val field: String   = "createdAt"
    val orderBy: String = "ascending"
    val convertParams   = s"$field.$orderBy"
  }

  case object SortByCreatedAtDesc extends Sort {
    val field: String   = "createdAt"
    val orderBy: String = "descending"
    val convertParams   = s"$field.$orderBy"
  }

  def apply(sortParams: Option[String]): Option[Sort] = sortParams map {
    case SortByLRNAsc.convertParams       => SortByLRNAsc
    case SortByLRNDesc.convertParams      => SortByLRNDesc
    case SortByCreatedAtAsc.convertParams => SortByCreatedAtAsc
    case _                                => SortByCreatedAtDesc
  }

}
