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

package models

import play.api.libs.json.{JsObject, Json, OWrites}

case class ViewMovementAction(href: String, key: String)

object ViewMovementAction {
  implicit val writes: OWrites[ViewMovementAction] =
    new OWrites[ViewMovementAction] {
      override def writes(o: ViewMovementAction): JsObject = Json.obj(
        "href" -> o.href,
        "key"  -> o.key
      )
    }
}
