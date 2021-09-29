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

package controllers.actions

import base.SpecBase
import base.FakeFrontendAppConfig
import com.google.inject.Inject
import connectors.EnrolmentStoreConnector
import controllers.actions.AuthActionSpec._
import controllers.routes
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.mvc.Results
import play.api.test.Helpers._
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import uk.gov.hmrc.auth.{core => authClient}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {

    def onPageLoad() = (stubControllerComponents().actionBuilder andThen authAction) {
      _ =>
        Results.Ok
    }
  }

  val mockAuthConnector: AuthConnector                     = mock[AuthConnector]
  val mockEnrolmentStoreConnector: EnrolmentStoreConnector = mock[EnrolmentStoreConnector]
  val mockUIRender: Renderer                               = mock[Renderer]
  val frontendAppConfig                                    = FakeFrontendAppConfig()

  val LEGACY_ENROLMENT_KEY    = "HMCE-NCTS-ORG"
  val LEGACY_ENROLMENT_ID_KEY = "VATRegNoTURN"
  val NEW_ENROLMENT_KEY       = "HMRC-CTC-ORG"
  val NEW_ENROLMENT_ID_KEY    = "EORINumber"

  val legacyEnrolmentsWithoutEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = "HMCE-NCTS-ORG",
        identifiers = Seq.empty,
        state = "Activated"
      ),
      Enrolment(
        key = "IR-CT",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "456"
          )
        ),
        state = "Activated"
      )
    )
  )

  val legacyEnrolmentsWithEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = LEGACY_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            LEGACY_ENROLMENT_ID_KEY,
            "123"
          )
        ),
        state = "NotYetActivated"
      ),
      Enrolment(
        key = LEGACY_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            LEGACY_ENROLMENT_ID_KEY,
            "456"
          )
        ),
        state = "Activated"
      )
    )
  )

  val legacyEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = LEGACY_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            LEGACY_ENROLMENT_ID_KEY,
            "123"
          )
        ),
        state = "NotYetActivated"
      )
    )
  )

  val newEnrolmentsWithoutEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = NEW_ENROLMENT_KEY,
        identifiers = Seq.empty,
        state = "Activated"
      ),
      Enrolment(
        key = "IR-CT",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "456"
          )
        ),
        state = "Activated"
      )
    )
  )

  val newEnrolmentsWithEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = NEW_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            NEW_ENROLMENT_ID_KEY,
            "123"
          )
        ),
        state = "NotYetActivated"
      ),
      Enrolment(
        key = NEW_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            NEW_ENROLMENT_ID_KEY,
            "456"
          )
        ),
        state = "Activated"
      )
    )
  )

  val newEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      ),
      Enrolment(
        key = NEW_ENROLMENT_KEY,
        identifiers = Seq(
          EnrolmentIdentifier(
            NEW_ENROLMENT_ID_KEY,
            "123"
          )
        ),
        state = "NotYetActivated"
      )
    )
  )

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val authAction =
          new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "when the user's session has expired" - {

      "must redirect the user to log in " in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "when the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments),
                                                           frontendAppConfig,
                                                           mockEnrolmentStoreConnector,
                                                           mockUIRender
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
                                                           frontendAppConfig,
                                                           mockEnrolmentStoreConnector,
                                                           mockUIRender
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider),
                                                           frontendAppConfig,
                                                           mockEnrolmentStoreConnector,
                                                           mockUIRender
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
                                                           frontendAppConfig,
                                                           mockEnrolmentStoreConnector,
                                                           mockUIRender
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole),
                                                           frontendAppConfig,
                                                           mockEnrolmentStoreConnector,
                                                           mockUIRender
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "AuthAction" - {
      "must redirect to unauthorised page when given legacy enrolments without eori" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(legacyEnrolmentsWithoutEori ~ Some("testName")))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "must redirect to unauthorised page when given new enrolments without eori" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(newEnrolmentsWithoutEori ~ Some("testName")))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }

      "must redirect to unauthorised page with group access when given user has no active legacy enrolments but new group has" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(legacyEnrolmentsWithEoriButNoActivated ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockUIRender.render(any())(any())).thenReturn(Future.successful(Html("")))

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        verify(mockUIRender, times(1)).render(templateCaptor.capture())(any())
        templateCaptor.getValue mustBe "unauthorisedWithGroupAccess.njk"
      }

      "must redirect to unauthorised page with group access when given user has no active legacy enrolments but legacy group has" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(legacyEnrolmentsWithEoriButNoActivated ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
        when(mockUIRender.render(any())(any())).thenReturn(Future.successful(Html("")))

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        verify(mockUIRender, times(1)).render(templateCaptor.capture())(any())
        templateCaptor.getValue mustBe "unauthorisedWithGroupAccess.njk"
      }

      "must redirect to unauthorised page with group access when given user has no active new enrolments but new group has" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(newEnrolmentsWithEoriButNoActivated ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockUIRender.render(any())(any())).thenReturn(Future.successful(Html("")))

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        verify(mockUIRender, times(1)).render(templateCaptor.capture())(any())
        templateCaptor.getValue mustBe "unauthorisedWithGroupAccess.njk"
      }

      "must redirect to unauthorised page with group access when given user has no active new enrolments but legacy group has" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(newEnrolmentsWithEoriButNoActivated ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
        when(mockUIRender.render(any())(any())).thenReturn(Future.successful(Html("")))

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        verify(mockUIRender, times(1)).render(templateCaptor.capture())(any())
        templateCaptor.getValue mustBe "unauthorisedWithGroupAccess.njk"
      }
      "must redirect to unauthorised page with group access when given user has no enrolments but group has" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set.empty) ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockUIRender.render(any())(any())).thenReturn(Future.successful(Html("")))

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        verify(mockUIRender, times(1)).render(templateCaptor.capture())(any())

        templateCaptor.getValue mustBe "unauthorisedWithGroupAccess.njk"
      }

      "must redirect to unauthorised page without group access when given both user and group has no enrolments" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set.empty) ~ Some("testName")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockUIRender.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.eccEnrolmentSplashPage)
      }

      "must redirect to unauthorised page without group access when given user has no enrolments and there is no group" in {
        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set.empty) ~ None))

        when(mockUIRender.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
        when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.eccEnrolmentSplashPage)
      }

      "must return Ok when given legacy enrolments with eori" in {
        when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(legacyEnrolmentsWithEori ~ Some("testName")))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }

      "must return Ok when given new enrolments with eori" in {
        when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(newEnrolmentsWithEori ~ Some("testName")))

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, mockEnrolmentStoreConnector, mockUIRender)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
      }
    }
  }

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockAuthConnector)
    reset(mockEnrolmentStoreConnector)
    reset(mockUIRender)
  }
}

object AuthActionSpec {

  implicit class RetrievalsUtil[A](val retrieval: A) extends AnyVal {
    def `~`[B](anotherRetrieval: B): A ~ B = authClient.retrieve.~(retrieval, anotherRetrieval)
  }

}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
