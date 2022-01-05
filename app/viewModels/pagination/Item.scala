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

package viewModels.pagination

import play.api.libs.json.{Json, OFormat}

case class Item(pageNumber: Int, href: String, selected: Boolean)

object Item {

  def apply(pageNumber: Int, href: String, currentPage: Int): Item =
    Item(
      pageNumber = pageNumber,
      href = s"$href?page=$pageNumber",
      selected = pageNumber == currentPage
    )

  implicit val format: OFormat[Item] = Json.format[Item]
}
