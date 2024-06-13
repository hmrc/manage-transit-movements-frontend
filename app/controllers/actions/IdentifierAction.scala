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
import play.api.Logging
import models.Enrolment
import models.Enrolment.LegacyEnrolment
import models.requests.IdentifierRequest
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
  enrolmentStoreConnector: EnrolmentStoreConnector
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig)
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
          logger.info(s"Enrolment keys found: '${enrolments.enrolments.map(_.key).mkString(",")}'")
          def checkEnrolment[E <: Enrolment](e: E)(implicit hc: HeaderCarrier): Future[Option[Either[Result, Result]]] = {
            logger.info(s"Enrolment key given: ${e.key}")
            enrolments.enrolments
              .filter(_.isActivated)
              .find(_.key.equals(e.key)) match {
              case Some(enrolment) =>
                logger.info(s"Enrolment found for ${e.key}")
                enrolment.getIdentifier(e.identifierKey) match {
                  case Some(enrolmentIdentifier) =>
                    e match {
                      case _: LegacyEnrolment if config.phase5Enabled =>
                        logger.info(s"User with EORI $enrolmentIdentifier is on legacy enrolment")
                        Future.successful(Some(Right(Redirect(config.enrolmentGuidancePage))))
                      case _ =>
                        logger.info(s"EORI found for ${e.key}")
                        block(IdentifierRequest(request, enrolmentIdentifier.value)).map(Right(_)).map(Some(_))
                    }
                  case None =>
                    logger.info(s"EORI not found for ${e.key}")
                    Future.successful(Some(Left(Redirect(routes.UnauthorisedController.onPageLoad()))))

                }
              case None =>
                maybeGroupId match {
                  case Some(groupId) =>
                    enrolmentStoreConnector.checkGroupEnrolments(groupId, e.key).map {
                      case true =>
                        Some(Left(Redirect(routes.UnauthorisedWithGroupAccessController.onPageLoad())))
                      case false =>
                        None
                    }
                  case None =>
                    logger.info("No Enrolment and No group id")
                    Future.successful(None)
                }
            }
          }

          def rec(enrolments: List[Enrolment], results: List[Result] = Nil): Future[Result] = {
            logger.info(s"Results: ${results.mkString(",")}")
            enrolments match {
              case Nil =>
                Future.successful(results.headOption.getOrElse(Redirect(config.eccEnrolmentSplashPage)))
              case head :: tail =>
                checkEnrolment(head)
                  .flatMap {
                    case Some(Right(onEnrolmentFound))   => Future.successful(onEnrolmentFound)
                    case Some(Left(onEnrolmentNotFound)) => rec(tail, results :+ onEnrolmentNotFound)
                    case None                            => rec(tail, results)
                  }
            }
          }

          rec(List(config.newEnrolment, config.legacyEnrolment))
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
