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

package views.behaviours

import play.twirl.api.TwirlHelperImports.*
import viewModels.ViewMovement

// scalastyle:off method.length
// scalastyle:off magic.number
trait MovementsTableViewBehaviours[T <: ViewMovement] extends ViewBehaviours {

  val viewMovements: Seq[T]

  def pageWithMovementsData(referenceNumberColumnHeader: String): Unit =
    "page with a movements data table" - {

      val dates  = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading").toList
      val tables = doc.getElementsByClass("govuk-table").toList

      viewMovements.groupByDate.zipWithIndex.foreach {
        case ((date, movements), tableIndex) =>
          s"when table $tableIndex" - {
            "must render date" in {
              dates(tableIndex).text() mustEqual date
            }

            val table = tables(tableIndex)

            "header cells" - {

              val headers = table.getElementsByClass("govuk-table__header").toList

              "must display correct value for first column" in {
                headers.head.text() mustBe "Updated"
              }

              "must display correct value for second column" in {
                headers(1).text() mustBe referenceNumberColumnHeader
              }

              "must display correct value for third column" in {
                headers(2).text() mustBe "Status"
              }

              "must display correct value for fourth column" in {
                headers(3).text() mustBe "Actions"
              }
            }

            "rows" - {
              val body = table.getElementsByClass("govuk-table__body").toList.head
              body.getElementsByClass("govuk-table__row").toList.zipWithIndex.foreach {
                case (row, rowIndex) =>
                  val movement = movements(rowIndex)
                  val cells    = row.getElementsByClass("govuk-table__cell").toList

                  "must display correct value for first column" in {
                    cells.head.text() mustBe movement.updated
                  }

                  "must display correct value for second column" in {
                    cells(1).text() mustBe movement.referenceNumber
                  }

                  "must display correct value for third column" in {
                    cells(2).text() mustBe movement.status
                  }

                  "fourth column" - {

                    val actionLinks = cells(3).getElementsByClass("govuk-link")
                    actionLinks.zipWithIndex.foreach {
                      case (link, linkIndex) =>
                        val action = movement.actions(linkIndex)

                        s"when action ${linkIndex + 1}" - {

                          "must display correct text" in {
                            link.text() mustBe s"${action.key} for ${movement.referenceNumber}"

                            link.ownText() mustBe action.key

                            val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
                            hiddenText.text() mustBe s"for ${movement.referenceNumber}"
                          }

                          "must have correct id" in {
                            link.attr("id") mustBe s"${action.key}-${movement.referenceNumber}"
                          }

                          "must have correct href" in {
                            link.attr("href") mustBe action.href
                          }
                        }
                    }
                  }
              }
            }
          }
      }
    }
}

// scalastyle:on method.length
// scalastyle:on magic.number
