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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.google.inject.Inject
import connectors.EnrolmentStoreConnector
import controllers.actions.AuthActionSpec._
import controllers.routes
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Results
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import uk.gov.hmrc.auth.{core => authClient}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  class Harness(authAction: IdentifierAction) {

    def onPageLoad() = (stubControllerComponents().actionBuilder andThen authAction) {
      _ =>
        Results.Ok
    }
  }

  val mockAuthConnector: AuthConnector                     = mock[AuthConnector]
  val mockEnrolmentStoreConnector: EnrolmentStoreConnector = mock[EnrolmentStoreConnector]

  val LEGACY_ENROLMENT_KEY    = "HMCE-NCTS-ORG"
  val LEGACY_ENROLMENT_ID_KEY = "VATRegNoTURN"
  val NEW_ENROLMENT_KEY       = "HMRC-CTC-ORG"
  val NEW_ENROLMENT_ID_KEY    = "EORINumber"

  private def createEnrolment(key: String, identifierKey: Option[String], id: String, state: String) =
    Enrolment(
      key = key,
      identifiers = identifierKey match {
        case Some(idKey) => Seq(EnrolmentIdentifier(idKey, id))
        case None        => Seq.empty
      },
      state = state
    )

  "Auth Action" - {

    "when P4" - {

      def applicationBuilderWithFake(authorisationException: AuthorisationException): Application = guiceApplicationBuilder()
        .overrides(
          bind[EnrolmentStoreConnector].toInstance(mockEnrolmentStoreConnector),
          bind[AuthConnector].toInstance(new FakeFailingAuthConnector(authorisationException))
        )
        .build()

      val applicationBuilderWithMock: Application = guiceApplicationBuilder()
        .overrides(
          bind[EnrolmentStoreConnector].toInstance(mockEnrolmentStoreConnector),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      "when the user hasn't logged in" - {
        "must redirect the user to log in " in {
          val app        = applicationBuilderWithFake(new MissingBearerToken)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        }
      }

      "when the user's session has expired" - {
        "must redirect the user to log in " in {
          val app        = applicationBuilderWithFake(new BearerTokenExpired)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        }
      }

      "when the user doesn't have sufficient enrolments" - {
        "must redirect the user to the unauthorised page" in {
          val app        = applicationBuilderWithFake(new InsufficientEnrolments)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when the user doesn't have sufficient confidence level" - {
        "must redirect the user to the unauthorised page" in {
          val app        = applicationBuilderWithFake(new InsufficientConfidenceLevel)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when the user used an unaccepted auth provider" - {
        "must redirect the user to the unauthorised page" in {
          val app        = applicationBuilderWithFake(new UnsupportedAuthProvider)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when the user has an unsupported affinity group" - {
        "must redirect the user to the unauthorised page" in {
          val app        = applicationBuilderWithFake(new UnsupportedAffinityGroup)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when the user has an unsupported credential role" - {
        "must redirect the user to the unauthorised page" in {
          val app        = applicationBuilderWithFake(new UnsupportedCredentialRole)
          val authAction = app.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when given legacy enrolments without eori" - {
        "must redirect to unauthorised page" in {
          val legacyEnrolmentsWithoutEori: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, None, "123", "Activated"),
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(legacyEnrolmentsWithoutEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when given new enrolments without eori" - {
        "must redirect to unauthorised page" in {
          val newEnrolmentsWithoutEori: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, None, "999", "Activated"),
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newEnrolmentsWithoutEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when given user has no active legacy enrolments but new group has" - {
        "must redirect to unauthorised page with group access" in {
          val legacyEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "999", "NotYetActivated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(legacyEnrolmentsWithEoriButNoActivated ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedWithGroupAccessController.onPageLoad().url
        }
      }

      "when given user has no active legacy enrolments but legacy group has" - {
        "must redirect to unauthorised page with group access" in {
          val legacyEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "999", "NotYetActivated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(legacyEnrolmentsWithEoriButNoActivated ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedWithGroupAccessController.onPageLoad().url
        }
      }

      "when given user has no active new enrolments but new group has" - {
        "must redirect to unauthorised page with group access" in {
          val newEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "123", "NotYetActivated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newEnrolmentsWithEoriButNoActivated ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedWithGroupAccessController.onPageLoad().url
        }
      }

      "when given user has no active new enrolments but legacy group has" - {
        "must redirect to unauthorised page with group access" in {
          val newEnrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "123", "NotYetActivated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newEnrolmentsWithEoriButNoActivated ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedWithGroupAccessController.onPageLoad().url
        }
      }

      "when given user has no enrolments but group has" - {
        "must redirect to unauthorised page with group access" in {
          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Enrolments(Set.empty) ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedWithGroupAccessController.onPageLoad().url
        }
      }

      "when given both user and group has no enrolments" - {
        "must redirect to ECC enrolment splash page" in {
          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Enrolments(Set.empty) ~ Some("testName")))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe frontendAppConfig.eccEnrolmentSplashPage
        }
      }

      "when given user has no enrolments and there is no group" - {
        "must redirect to ECC enrolment splash page" in {
          when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Enrolments(Set.empty) ~ None))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))
          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe frontendAppConfig.eccEnrolmentSplashPage
        }
      }

      "when given legacy enrolments with eori" - {
        "must return Ok" in {
          val legacyEnrolmentsWithEori: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "123", "NotYetActivated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "999", "Activated"),
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(legacyEnrolmentsWithEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }

      "when given both new and legacy enrolments with eori" - {
        "must return Ok" in {
          val newAndLegacyEnrolmentsWithEori: Enrolments = Enrolments(
            Set(
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newAndLegacyEnrolmentsWithEori ~ Some("testName")))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }

      "when given a new enrolment without a key, and a legacy enrolment with eori" - {
        "must return Ok" in {
          val newWithoutEoriLegacyWithEori: Enrolments = Enrolments(
            Set(
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, None, "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newWithoutEoriLegacyWithEori ~ Some("testName")))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }

      "when given a legacy enrolment without a key, and a new enrolment with eori" - {
        "must return Ok" in {
          val newWithEoriLegacyWithoutEori: Enrolments = Enrolments(
            Set(
              createEnrolment(LEGACY_ENROLMENT_KEY, None, "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newWithEoriLegacyWithoutEori ~ Some("testName")))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }

      "when given a group legacy enrolment, and a new enrolment without a key" - {
        "must redirect to unauthorised page (because newer enrolment takes precedence)" in {
          val newWithoutEori: Enrolments = Enrolments(
            Set(
              createEnrolment(NEW_ENROLMENT_KEY, None, "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newWithoutEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(true))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }

      "when given new enrolments with eori" - {
        "must return Ok" in {
          val newEnrolmentsWithEori: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "123", "NotYetActivated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "456", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(newEnrolmentsWithEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(LEGACY_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe OK
        }
      }
    }

    "when P5" - {

      val applicationBuilderWithMock: Application = p5GuiceApplicationBuilder()
        .overrides(
          bind[EnrolmentStoreConnector].toInstance(mockEnrolmentStoreConnector),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      "when given legacy enrolments with eori" - {
        "must redirect to guidance page to migrate to new enrolment" in {
          val legacyEnrolmentsWithEori: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "123", "NotYetActivated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "999", "Activated"),
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(legacyEnrolmentsWithEori ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe frontendAppConfig.enrolmentGuidancePage
        }
      }

      "when given legacy enrolments with eori and deactivated new enrolment" - {
        "must redirect to guidance page to migrate to new enrolment" in {
          val enrolments: Enrolments = Enrolments(
            Set(
              createEnrolment("IR-CT", Some("UTR"), "456", "Activated"),
              createEnrolment(NEW_ENROLMENT_KEY, Some(NEW_ENROLMENT_ID_KEY), "123", "NotYetActivated"),
              createEnrolment(LEGACY_ENROLMENT_KEY, Some(LEGACY_ENROLMENT_ID_KEY), "999", "Activated"),
              createEnrolment("IR-SA", Some("UTR"), "123", "Activated")
            )
          )

          when(mockAuthConnector.authorise[Enrolments ~ Some[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(enrolments ~ Some("testName")))

          when(mockEnrolmentStoreConnector.checkGroupEnrolments(any(), eqTo(NEW_ENROLMENT_KEY))(any())).thenReturn(Future.successful(false))

          val authAction = applicationBuilderWithMock.injector.instanceOf[AuthenticatedIdentifierAction]
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe frontendAppConfig.enrolmentGuidancePage
        }
      }
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
    reset(mockEnrolmentStoreConnector)
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
