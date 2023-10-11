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

import models.requests.IdentifierRequest
import play.api.mvc.{ActionFunction, Request}

import javax.inject.Inject

class Actions @Inject() (
  identifierAction: IdentifierAction,
  p5SwitchAction: P5SwitchActionProvider
) {

  def identify(): IdentifierAction = identifierAction

  def checkP5Switch(): ActionFunction[Request, IdentifierRequest] =
    identify() andThen p5SwitchAction()
}
