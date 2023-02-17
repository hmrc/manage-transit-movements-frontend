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

import controllers.departure.drafts.routes
import models.Sort.Field.{CreatedAt, LRN}
import models.Sort.{Field, Order}
import models.Sort.Order.{Ascending, Descending}
import play.api.mvc.Call

sealed trait Sort {
  val field: Field
  val order: Order
  def ariaSort(that: Field): String = if (this.field == that) this.order.ariaSort else "none"

  lazy val convertParams: String = this.toString
  override def toString: String  = s"$field.$order"
}

object Sort {

  sealed trait Order {
    def ariaSort: String
  }

  object Order {

    case object Ascending extends Order {
      override def toString: String = "asc"
      override def ariaSort: String = "ascending"
    }

    case object Descending extends Order {
      override def toString: String = "dsc"
      override def ariaSort: String = "descending"
    }
  }

  sealed trait Field {

    def sortHyperlink(currentSort: String, lrn: Option[String]): Call =
      routes.DashboardController.onPageLoad(
        None,
        lrn = lrn,
        currentSort match {
          case "descending" => Some(s"$this.$Ascending")
          case _            => Some(s"$this.$Descending")
        }
      )
  }

  object Field {

    case object LRN extends Field {
      override def toString: String = "lrn"
    }

    case object CreatedAt extends Field {
      override def toString: String = "createdAt"
    }
  }

  case object SortByLRNAsc extends Sort {
    override val field: Field = LRN
    override val order: Order = Ascending
  }

  case object SortByLRNDesc extends Sort {
    override val field: Field = LRN
    override val order: Order = Descending
  }

  case object SortByCreatedAtAsc extends Sort {
    override val field: Field = CreatedAt
    override val order: Order = Ascending
  }

  case object SortByCreatedAtDesc extends Sort {
    override val field: Field = CreatedAt
    override val order: Order = Descending
  }

  def apply(sortParams: Option[String]): Option[Sort] = sortParams map {
    case SortByLRNAsc.convertParams       => SortByLRNAsc
    case SortByLRNDesc.convertParams      => SortByLRNDesc
    case SortByCreatedAtAsc.convertParams => SortByCreatedAtAsc
    case _                                => SortByCreatedAtDesc
  }

}
