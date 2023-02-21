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
import models.Sort.Order.{Ascending, Descending}
import models.Sort.{buildParam, Field, Order}
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait Sort {
  val field: Field
  val order: Order

  lazy val convertParams: String = this.toString

  def ariaSort(field: Field): String = if (this.field == field) this.order.ariaSort else "none"

  def href(field: Field, lrn: Option[String]): Call = {
    val order = if (this.field == field) this.order.toggle else field.defaultOrder
    routes.DashboardController.onPageLoad(None, lrn, Some(buildParam(field, order)))
  }

  val prefix = "departure.drafts.dashboard.table.sort.hidden"

  def hiddenText(field: Field)(implicit messages: Messages): String = {
    val sortBy = if (this.field == field) this.order.toggle.ariaSort else field.defaultOrder.ariaSort
    messages(prefix, field.sortHeader, sortBy)
  }

  override def toString: String = buildParam(field, order)
}

object Sort {

  def buildParam(field: Field, order: Order): String = s"$field.$order"

  sealed trait Order {
    val ariaSort: String

    def toggle: Order = this match {
      case Ascending  => Descending
      case Descending => Ascending
    }
  }

  object Order {

    case object Ascending extends Order {
      override val ariaSort: String = "ascending"
      override def toString: String = "asc"
    }

    case object Descending extends Order {
      override val ariaSort: String = "descending"
      override def toString: String = "dsc"
    }
  }

  sealed trait Field {
    val defaultOrder: Order
    def sortHeader(implicit messages: Messages): String
  }

  object Field {

    case object LRN extends Field {
      override val defaultOrder: Order                             = Ascending
      override def toString: String                                = "lrn"
      override def sortHeader(implicit messages: Messages): String = messages("departure.drafts.dashboard.table.lrn")
    }

    case object CreatedAt extends Field {
      override val defaultOrder: Order                             = Descending
      override def toString: String                                = "createdAt"
      override def sortHeader(implicit messages: Messages): String = messages("departure.drafts.dashboard.table.daysToComplete")
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
