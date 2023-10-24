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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.EnrolmentStoreConnector
import controllers.routes
import logging.Logging
import models.EnrolmentStatus._
import models.requests.IdentifierRequest
import models.{Enrolment, EnrolmentStatus}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  enrolmentStoreConnector: EnrolmentStoreConnector
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(EmptyPredicate)
      .retrieve(Retrievals.allEnrolments and Retrievals.groupIdentifier) {
        case enrolments ~ maybeGroupId =>
          def checkEnrolment(enrolment: Enrolment)(implicit hc: HeaderCarrier): Future[EnrolmentStatus] =
            enrolment match {
              case Enrolment(key, identifierKey) =>
                enrolments.enrolments
                  .filter(_.isActivated)
                  .find(_.key.equals(key)) match {
                  case Some(enrolment) =>
                    enrolment.getIdentifier(identifierKey) match {
                      case Some(enrolmentIdentifier) =>
                        Future.successful(Enrolled(enrolmentIdentifier.value))
                      case None =>
                        Future.successful(EnrolmentIdentifierMissing)
                    }
                  case None =>
                    maybeGroupId match {
                      case Some(groupId) =>
                        enrolmentStoreConnector.checkGroupEnrolments(groupId, key).map {
                          case true =>
                            EnrolledInGroup
                          case false =>
                            NotEnrolled
                        }
                      case None =>
                        Future.successful(NotEnrolled)
                    }
                }
            }

          for {
            newEnrolment    <- checkEnrolment(config.newEnrolment)
            legacyEnrolment <- checkEnrolment(config.legacyEnrolment)
            result <- (newEnrolment, legacyEnrolment, config.phase5Enabled) match {
              case (Enrolled(enrolmentIdentifier), _, _) =>
                block(IdentifierRequest(request, enrolmentIdentifier))
              case (_, Enrolled(enrolmentIdentifier), false) =>
                block(IdentifierRequest(request, enrolmentIdentifier))
              case (_, Enrolled(enrolmentIdentifier), true) =>
                logger.info(s"User with EORI $enrolmentIdentifier is on legacy enrolment")
                Future.successful(Redirect(config.enrolmentGuidancePage))
              case (EnrolmentIdentifierMissing, _, _) | (_, EnrolmentIdentifierMissing, _) =>
                Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
              case (EnrolledInGroup, _, _) | (_, EnrolledInGroup, _) =>
                Future.successful(Redirect(routes.UnauthorisedWithGroupAccessController.onPageLoad()))
              case (NotEnrolled, _, _) | (_, NotEnrolled, _) =>
                Future.successful(Redirect(config.eccEnrolmentSplashPage))
            }
          } yield result
      }
  } recover {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: AuthorisationException =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

}
