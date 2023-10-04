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
import config.{Phase5, Phase5Switch}
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}

class P5SwitchActionProvider @Inject() (phase5: Phase5)(implicit ec: ExecutionContext) {

  def apply[T <: Phase5Switch](switch: Phase5 => T): ActionFilter[IdentifierRequest] =
    new P5SwitchAction(switch(phase5))
}

class P5SwitchAction[T <: Phase5Switch](switch: T)(implicit val executionContext: ExecutionContext) extends ActionFilter[IdentifierRequest] with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
    Future.successful {
      if (switch.enabled) None else Some(Redirect(routes.ErrorController.notFound()))
    }
}
