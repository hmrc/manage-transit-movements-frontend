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

package helper

import base.SpecBase
import generators.Generators
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.Format.decisionDateTimeFormatter
import utils.GoodsNotReleasedP5Helper
import viewModels.sections.Section

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class GoodsNotReleasedP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "GoodsNotReleasedP5Helper" - {

    "must return a complete section" in {

      val now = LocalDateTime.now()

      val message: IE051Data = IE051Data(
        IE051MessageData(
          TransitOperationIE051(
            "someMRN",
            now,
            "releaseMotivationCode",
            "releaseMotivationText"
          )
        )
      )

      val helper = new GoodsNotReleasedP5Helper(message.data)

      val result = helper.buildDetailsSection

      result mustBe Section(
        None,
        List(
          SummaryListRow(Key(Text("Movement Reference Number (MRN)"), ""), Value(Text("someMRN"), ""), "", None),
          SummaryListRow(Key(Text("Date and time declaration sent"), ""), Value(Text(formatAsDecisionDateTime(now)), ""), "", None),
          SummaryListRow(Key(Text("Reason"), ""), Value(Text("releaseMotivationCode"), ""), "", None),
          SummaryListRow(Key(Text("Description"), ""), Value(Text("releaseMotivationText"), ""), "", None)
        ),
        None
      )
    }
  }

  private def formatAsDecisionDateTime(answer: LocalDateTime): String =
    answer
      .format(decisionDateTimeFormatter)
      .replace("PM", "pm")
      .replace("AM", "am")
}
