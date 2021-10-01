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

package views.behaviours

import base.FakeFrontendAppConfig
import config.FrontendAppConfig
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.libs.json._
import viewModels.ViewMovement

import java.time.format.DateTimeFormatter
import scala.collection.convert.ImplicitConversions._

abstract class MovementsTableViewBehaviours(override protected val viewUnderTest: String) extends ViewBehaviours(viewUnderTest) {

  implicit val frontendAppConfig: FrontendAppConfig = FakeFrontendAppConfig()

  val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  // scalastyle:off method.length
  // scalastyle:off magic.number
  def pageWithMovementsData[T <: ViewMovement](doc: Document, viewMovements: Seq[T], messageKeyPrefix: String, refType: String)(implicit
    wts: Writes[T]
  ): Unit = {

    "generate a heading for each unique day" in {
      val ls: Elements = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading")
      ls.size() mustEqual 6
      ls.eq(0).text() mustBe "16 August 2020"
      ls.eq(1).text() mustBe "15 August 2020"
      ls.eq(2).text() mustBe "14 August 2020"
      ls.eq(3).text() mustBe "13 August 2020"
      ls.eq(4).text() mustBe "12 August 2020"
      ls.eq(5).text() mustBe "11 August 2020"
    }

    val rows: Elements = doc.select("tr[data-testrole^=movements-list_row]")

    "generate a row for each movement" in {
      rows.size() mustEqual 7
    }

    "generate correct data in each row" - {
      rows.toList.zipWithIndex.forEach {
        case (row, rowIndex) =>
          s"row ${rowIndex + 1}" - {

            def elementWithVisibleText(element: Element, text: String): Unit =
              element.ownText() mustBe text.trim

            def elementWithHiddenText(element: Element, text: String): Unit = {
              val hiddenText = element.getElementsByClass("govuk-visually-hidden").head
              hiddenText.text() mustBe text
            }

            "display correct time" in {
              val updated = row.selectFirst("td[data-testrole*=-updated]")
              val time    = Json.toJson(viewMovements(rowIndex)).transform((JsPath \ "updated").json.pick[JsString]).get.value

              behave like elementWithVisibleText(updated, time)
              behave like elementWithHiddenText(updated, s"$messageKeyPrefix.table.updated")
            }

            "display correct reference number" in {
              val ref = row.selectFirst("td[data-testrole*=-ref]")

              behave like elementWithVisibleText(ref, viewMovements(rowIndex).referenceNumber)
              behave like elementWithHiddenText(ref, s"$messageKeyPrefix.table.$refType")
            }

            "display correct status" in {
              val status = row.selectFirst("td[data-testrole*=-status]")

              behave like elementWithVisibleText(status, viewMovements(rowIndex).status)
              behave like elementWithHiddenText(status, s"$messageKeyPrefix.table.status")
            }

            "display actions" - {
              val actions = row.selectFirst("td[data-testrole*=-actions]")

              "include hidden content" in {
                behave like elementWithHiddenText(actions, s"$messageKeyPrefix.table.action")
              }

              val links = actions.getElementsByClass("action-link")
              links.zipWithIndex.forEach {
                case (link, linkIndex) =>
                  s"action ${linkIndex + 1}" - {

                    "display correct text" in {
                      behave like elementWithVisibleText(link, s"${viewMovements(rowIndex).actions(linkIndex).key}")
                      behave like elementWithHiddenText(link, "viewArrivalNotifications.table.action.hidden")
                    }

                    "have correct id" in {
                      link.attr("id") mustBe s"${viewMovements(rowIndex).actions(linkIndex).key}-${viewMovements(rowIndex).referenceNumber}"
                    }

                    "have correct href" in {
                      link.attr("href") mustBe viewMovements(rowIndex).actions(linkIndex).href
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

}
