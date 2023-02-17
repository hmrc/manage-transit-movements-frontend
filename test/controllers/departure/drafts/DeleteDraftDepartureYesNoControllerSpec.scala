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
import connectors.DeparturesMovementsP5Connector
import controllers.actions.{FakeLockAction, LockActionProvider}
import forms.YesNoFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DraftDepartureService
import uk.gov.hmrc.http.HttpResponse
import views.html.departure.drafts.DeleteDraftDepartureYesNoView

import scala.concurrent.Future

class DeleteDraftDepartureYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with ScalaCheckPropertyChecks {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("departure.drafts.deleteDraftDepartureYesNo")

  val lrnString: String = lrn.toString()

  private val draftDepartureService = mock[DraftDepartureService]
  private val mockConnector         = mock[DeparturesMovementsP5Connector]

  final val mockLockActionProvider: LockActionProvider = mock[LockActionProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DraftDepartureService].toInstance(draftDepartureService),
        bind[LockActionProvider].toInstance(mockLockActionProvider),
        bind[DeparturesMovementsP5Connector].toInstance(mockConnector)
      )

  private lazy val deleteDraftDepartureYesNoRoute = routes.DeleteDraftDepartureYesNoController.onPageLoad(lrnString, 1, 2, None).url

  "DeleteDraftDepartureYesNo Controller" - {

    when(mockLockActionProvider.apply(any())).thenReturn(new FakeLockAction("AB123", mockConnector))
    
    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, deleteDraftDepartureYesNoRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[DeleteDraftDepartureYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrnString, 1, 2, None)(request, messages).toString
    }

    "when yes submitted must redirect back to draft departure dashboard when on first page" in {

      val routePath = routes.DeleteDraftDepartureYesNoController.onPageLoad(lrnString, 1, 2, None).url

      val statusOK = 200

      when(draftDepartureService.deleteDraftDeparture(any())(any())).thenReturn(Future.successful(HttpResponse(statusOK, "")))

      val request = FakeRequest(POST, routePath)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.departure.drafts.routes.DashboardController.onPageLoad(Some(1), None).url
    }

    "when yes submitted must redirect back to draft departure dashboard when on page is not 1 and rows is 1" in {

      val routePath = routes.DeleteDraftDepartureYesNoController.onPageLoad(lrnString, 2, 1, None).url

      val statusOK = 200

      when(draftDepartureService.deleteDraftDeparture(any())(any())).thenReturn(Future.successful(HttpResponse(statusOK, "")))

      val request = FakeRequest(POST, routePath)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.departure.drafts.routes.DashboardController.onPageLoad(Some(1), None).url
    }

    "when yes submitted must redirect back to draft departure dashboard when on page is not 1 and rows is not 1" in {

      val routePath = routes.DeleteDraftDepartureYesNoController.onPageLoad(lrnString, 2, 2, None).url

      val statusOK = 200

      when(draftDepartureService.deleteDraftDeparture(any())(any())).thenReturn(Future.successful(HttpResponse(statusOK, "")))

      val request = FakeRequest(POST, routePath)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.departure.drafts.routes.DashboardController.onPageLoad(Some(2), None).url
    }

    "when no submitted must redirect back to draft departure dashboard" in {
      val request = FakeRequest(POST, deleteDraftDepartureYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.departure.drafts.routes.DashboardController.onPageLoad(Some(1), None).url
    }

    "when yes submitted must redirect to InternalServerError if status 500 is returned from connector" in {

      val statusError = 500

      when(draftDepartureService.deleteDraftDeparture(any())(any())).thenReturn(Future.successful(HttpResponse(statusError, "")))

      val request = FakeRequest(POST, deleteDraftDepartureYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.routes.ErrorController.internalServerError().url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val lrnString = lrn.toString

      val invalidValue = ""
      val request      = FakeRequest(POST, deleteDraftDepartureYesNoRoute).withFormUrlEncodedBody(("value", invalidValue))
      val boundForm    = form.bind(Map("value" -> invalidValue))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[DeleteDraftDepartureYesNoView]

      val content = contentAsString(result)

      content mustEqual
        view(boundForm, lrnString, 1, 2, None)(request, messages).toString
    }

  }
}
