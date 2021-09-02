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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.json._
import viewModels.ViewMovement

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.convert.ImplicitConversions._

trait MovementsTableViewBehaviours extends ViewBehaviours {

  implicit val frontendAppConfig: FrontendAppConfig = FakeFrontendAppConfig()

  val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  val day1: LocalDateTime = LocalDateTime.parse("2020-08-16 06:06:06", dateTimeFormat)
  val day2: LocalDateTime = LocalDateTime.parse("2020-08-15 05:05:05", dateTimeFormat)
  val day3: LocalDateTime = LocalDateTime.parse("2020-08-14 04:04:04", dateTimeFormat)
  val day4: LocalDateTime = LocalDateTime.parse("2020-08-13 03:03:03", dateTimeFormat)
  val day5: LocalDateTime = LocalDateTime.parse("2020-08-12 02:02:02", dateTimeFormat)
  val day6_1: LocalDateTime
  val day6_2: LocalDateTime

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

    "generate a row for each movement" - {
      val rows: Elements = doc.getElementsByAttributeValue("role", "row")
      rows.size() mustEqual 7
      rows.toList.zipWithIndex.forEach {
        x =>
          s"row ${x._2 + 1}" - {

            "display correct time" in {
              val updated = x._1.selectFirst("[data-testrole*=updated]")
              val time    = Json.toJson(viewMovements(x._2)).transform((JsPath \ "updated").json.pick[JsString]).get.value
              updated.ownText() mustBe time
              updated.text() mustBe s"$messageKeyPrefix.table.updated $time"
            }

            "display correct reference number" in {
              val ref = x._1.selectFirst("[data-testrole*=ref]")
              ref.ownText() mustBe viewMovements(x._2).referenceNumber
              ref.text() mustBe s"$messageKeyPrefix.table.$refType ${viewMovements(x._2).referenceNumber}"
            }

            "display correct status" in {
              val status = x._1.selectFirst("[data-testrole*=status]")
              status.ownText() mustBe viewMovements(x._2).status
              status.text() mustBe s"$messageKeyPrefix.table.status ${viewMovements(x._2).status}"
            }

            "display actions" - {
              val actions = x._1.selectFirst("[data-testrole*=actions]")

              "include hidden content" in {
                actions.text() must include(s"$messageKeyPrefix.table.action")
              }

              val actionLinks = actions.getElementsByTag("a")
              actionLinks.zipWithIndex.forEach {
                y =>
                  s"action ${y._2 + 1}" - {

                    "display correct text" in {
                      y._1.text() mustBe s"${viewMovements(x._2).actions(y._2).key} ${s"${viewMovements(x._2).actions(y._2).key}.hidden"}"
                    }

                    "have correct id" in {
                      y._1.attr("id") mustBe s"${viewMovements(x._2).actions(y._2).key}-${viewMovements(x._2).referenceNumber}"
                    }

                    "have correct href" in {
                      y._1.attr("href") mustBe viewMovements(x._2).actions(y._2).href
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
