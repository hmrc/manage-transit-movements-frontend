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

import play.api.data.Form
import uk.gov.hmrc.viewmodels.{MessageInterpolators, Radios}

sealed trait WhatDoYouWantToDoOptions

object WhatDoYouWantToDoOptions extends Enumerable.Implicits {

  case object ArrivalNotifications extends WithName("arrivalNotifications") with WhatDoYouWantToDoOptions
  case object DepartureViewOldDeclarations extends WithName("departureViewOldDeclarations") with WhatDoYouWantToDoOptions
  case object DepartureMakeDeclarations extends WithName("departureMakeDeclarations") with WhatDoYouWantToDoOptions
  case object DepartureViewDeclarations extends WithName("departureViewDeclarations") with WhatDoYouWantToDoOptions
  case object NorthernIrelandMovements extends WithName("northernIrelandMovements") with WhatDoYouWantToDoOptions

  val values: Seq[WhatDoYouWantToDoOptions] = Seq(
    ArrivalNotifications,
    DepartureViewOldDeclarations,
    DepartureMakeDeclarations,
    DepartureViewDeclarations,
    NorthernIrelandMovements
  )

  def radios(form: Form[_], showPrivateBetaButtons: Boolean): Seq[Radios.Item] = {
    val field = form("value")

    val privateBetaButtons = Seq(
      Radios.Radio(msg"whatDoYouWantToDo.arrivalNotificationsText", ArrivalNotifications.toString),
      Radios.Radio(msg"whatDoYouWantToDo.departureViewOldDeclarationsText", DepartureViewOldDeclarations.toString),
      Radios.Radio(msg"whatDoYouWantToDo.departureMakeDeclarationsText", DepartureMakeDeclarations.toString),
      Radios.Radio(msg"whatDoYouWantToDo.departureViewDeclarationsText", DepartureViewDeclarations.toString),
      Radios.Radio(msg"whatDoYouWantToDo.northernIrelandMovementsText", NorthernIrelandMovements.toString)
    )

    val publicBetaButtons = Seq(
      Radios.Radio(msg"whatDoYouWantToDo.arrivalNotificationsText", ArrivalNotifications.toString),
      Radios.Radio(msg"whatDoYouWantToDo.departureMakeDeclarationsText", DepartureMakeDeclarations.toString),
      Radios.Radio(msg"whatDoYouWantToDo.northernIrelandMovementsText", NorthernIrelandMovements.toString)
    )

    val radioButtons = if (showPrivateBetaButtons) privateBetaButtons else publicBetaButtons
    Radios(field, radioButtons)
  }

  implicit val enumerable: Enumerable[WhatDoYouWantToDoOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
