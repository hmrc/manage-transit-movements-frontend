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
import generated.CC051CType
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.GoodsNotReleasedP5Helper
import viewModels.sections.Section.StaticSection

class GoodsNotReleasedP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "GoodsNotReleasedP5Helper" - {

    "must return a complete section" in {
      forAll(arbitrary[CC051CType].map {
        x =>
          x
            .copy(TransitOperation =
              x.TransitOperation.copy(
                MRN = "someMRN",
                declarationSubmissionDateAndTime = XMLCalendar("2014-06-09T16:15:04"),
                noReleaseMotivationCode = "releaseMotivationCode",
                noReleaseMotivationText = "releaseMotivationText"
              )
            )
      }) {
        message =>
          val helper = new GoodsNotReleasedP5Helper(message)

          val result = helper.buildDetailsSection

          result mustBe StaticSection(
            sectionTitle = None,
            rows = List(
              SummaryListRow(Key(Text("Movement Reference Number (MRN)")), Value(Text("someMRN"))),
              SummaryListRow(Key(Text("Date and time declaration sent")), Value(Text("09 June 2014 at 4:15pm"))),
              SummaryListRow(Key(Text("Reason")), Value(Text("releaseMotivationCode"))),
              SummaryListRow(Key(Text("Description")), Value(Text("releaseMotivationText")))
            )
          )
      }
    }
  }
}
