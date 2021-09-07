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

package viewModels.pagination

import play.api.libs.json.{Json, OFormat}

case class Items(items: Seq[Item], firstItemDotted: Boolean, lastItemDotted: Boolean)

object Items {

  def apply(metaData: MetaData, href: String): Items =
    metaData match {
      case MetaData(_, _, _, currentPage, totalNumberOfPages) =>
        val itemList: Seq[Item] =
          if (totalNumberOfPages < 6) {

            val head = (1 to totalNumberOfPages).take(5)

            head.map(Item(_, href, currentPage))
          } else if (currentPage < 4) {
            val head  = (1 to totalNumberOfPages).take(4)
            val tail  = if (totalNumberOfPages >= 6) Seq(totalNumberOfPages) else Seq.empty
            val range = head ++ tail
            range.map(Item(_, href, currentPage))
          } else if (currentPage == totalNumberOfPages | currentPage == totalNumberOfPages - 1) {

            val range = Seq(1, totalNumberOfPages - 3, totalNumberOfPages - 2, totalNumberOfPages - 1, totalNumberOfPages)

            range.map(Item(_, href, currentPage))

          } else {

            val range = Seq(1, currentPage - 1, currentPage, currentPage + 1, totalNumberOfPages)

            range.map(Item(_, href, currentPage))
          }

        val firstItemDotted = currentPage > 3 && totalNumberOfPages > 5

        val lastItemDotted = currentPage < totalNumberOfPages - 2 && totalNumberOfPages > 5

        Items(itemList, firstItemDotted, lastItemDotted)
    }

  implicit val writes: OFormat[Items] = Json.format[Items]
}
