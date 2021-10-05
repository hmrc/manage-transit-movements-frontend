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

package views


import base.SingleViewSpec
import org.jsoup.nodes.Document
import play.api.libs.json.Json

class SessionExpiredViewSpec extends SingleViewSpec("session-expired.njk") {

  "SessionExpiredView" - {

    "must have the sign in link if the Document isLoggedIn=false" in {
      val doc: Document = renderDocument(
        Json.obj("isLoggedIn" -> false)
      ).futureValue

      assertPageHasLink(doc, "nav-sign-in", "Sign in", "urls.login?continue=urls.loginContinue")
      assertPageHasNoLink(doc, "nav-sign-out")
    }

    "must have the sign out link if the Document isLoggedIn=true" in {
      val doc: Document = renderDocument(
        Json.obj("isLoggedIn" -> true)
      ).futureValue

      assertPageHasLink(doc, "nav-sign-out", "Sign out", "urls.logoutContinueurls.feedback")
      assertPageHasNoLink(doc, "nav-sign-in")
    }

    "must have the sign out link if the Document isLoggedIn is not populated" in {
      val doc: Document = renderDocument().futureValue

      assertPageHasLink(doc, "nav-sign-out", "Sign out", "urls.logoutContinueurls.feedback")
      assertPageHasNoLink(doc, "nav-sign-in")
    }
  }

}
