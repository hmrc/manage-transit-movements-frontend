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
import controllers.routes
import models.LockCheck.{LockCheckFailure, Locked, Unlocked}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.DraftDepartureService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class LockActionProvider @Inject() (service: DraftDepartureService)(implicit ec: ExecutionContext) {

  def apply(lrn: String): ActionFilter[IdentifierRequest] =
    new LockAction(lrn, service)

}

class LockAction(lrn: String, service: DraftDepartureService)(implicit val executionContext: ExecutionContext)
    extends ActionFilter[IdentifierRequest]
    with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    service.checkLock(lrn).map {
      case Unlocked         => None
      case Locked           => Some(Redirect(controllers.departureP5.drafts.routes.DraftLockedController.onPageLoad()))
      case LockCheckFailure => Some(Redirect(routes.ErrorController.technicalDifficulties()))
    }
  }

}
