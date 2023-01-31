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

package controllers.departure.drafts

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import org.scalatestplus.mockito.MockitoSugar

class DeleteDraftDepartureYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("departure.drafts.deleteDraftDepartureYesNo")
  private lazy val deleteDraftDepartureYesNoRoute = routes.DeleteDraftDepartureYesNoController.onPageLoad(lrn.toString()).url

  "DeleteDraftDepartureYesNo Controller" ignore {}
}
