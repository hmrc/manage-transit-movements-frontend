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

package viewModels

import base.SpecBase
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ViewMovementActionSpec extends SpecBase with ScalaCheckPropertyChecks {

  "id" - {
    "must replace spaces with hyphens and append a reference number" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (href, ref) =>
          val action = ViewMovementAction(href, "random message key")
          action.id(ref) mustBe s"random-message-key-$ref"
      }
    }
  }

}
