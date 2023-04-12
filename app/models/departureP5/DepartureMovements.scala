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

package models.departureP5

import models.{Movement, Movements}
import play.api.libs.json.{Reads, __}

case class DepartureMovements(departureMovements: Seq[DepartureMovement]) extends Movements {
  override val movements: Seq[Movement]  = departureMovements
  override val retrieved: Int            = departureMovements.length
  override val totalMatched: Option[Int] = None
}

object DepartureMovements {

  implicit lazy val reads: Reads[DepartureMovements] =
    (__ \ "departures").read[Seq[DepartureMovement]].map(DepartureMovements.apply)
}


