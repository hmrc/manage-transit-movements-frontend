package controllers.departure.drafts

import base.{AppWithDefaultMockFixtures, SpecBase}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.departure.drafts.DashboardView

class DashboardControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val draftDashboardRoute = routes.DashboardController.onPageLoad(lrn).url

  "DraftDashboard Controller" - {

    "must return OK and the correct view for a GET" ignore {
//
//      setExistingUserAnswers(emptyUserAnswers)
//
//      val request = FakeRequest(GET, draftDashboardRoute)
//      val result  = route(app, request).value
//
//      val view = injector.instanceOf[DashboardView]
//
//      status(result) mustEqual OK
//
//      contentAsString(result) mustEqual
//        view(lrn)(request, messages).toString
    }
  }
}
