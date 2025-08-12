/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.*
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*

class SummaryListRowHelperSpec extends SpecBase with AppWithDefaultMockFixtures with Matchers {

  val helper = new SummaryListRowHelper

  "SummaryListRowHelper" - {

    "should build row without action" in {
      val row = helper.buildRow("prefix", "text".toText, Some("id"), None)
      row.key.value should be("prefix")
      row.value.value should be("text")
      row.actions.isDefined should be(false)
    }

    "should build row with action" in {
      val row = helper.buildRow("prefix", "text".toText, Some("id1"), Some(Call("method", "url")))
      row.key.value should be("prefix")
      row.value.value should be("text")

      val actionItem = row.actions.head.items.head
      actionItem.href should be("url")
      actionItem.attributes("id") should be("id1")
    }
  }
}
