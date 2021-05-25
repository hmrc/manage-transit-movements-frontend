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

import models.EoriNumber
import models.requests.IdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.DisplayDeparturesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrivateBetaActionFilter @Inject()(displayDeparturesService: DisplayDeparturesService, implicit val executionContext: ExecutionContext)
    extends ActionRefiner[IdentifierRequest, IdentifierRequest] {
  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    displayDeparturesService
      .showDepartures(EoriNumber(request.eoriNumber))
      .map {
        case true  => Right(request)
        case false => Left(Redirect(controllers.routes.OldServiceInterstitialController.onPageLoad()))
      }
  }
}
