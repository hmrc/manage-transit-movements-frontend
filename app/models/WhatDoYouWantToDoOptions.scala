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

  case object MakeArrivalNotification extends WithName("makeArrivalNotification") with WhatDoYouWantToDoOptions
  case object ViewArrivalNotificationAfter extends WithName("viewArrivalNotificationAfter") with WhatDoYouWantToDoOptions
  case object ViewArrivalNotificationBefore extends WithName("viewArrivalNotificationBefore") with WhatDoYouWantToDoOptions
  case object MakeDepartureDeclaration extends WithName("makeDepartureDeclaration") with WhatDoYouWantToDoOptions
  case object ViewDepartureDeclaration extends WithName("viewDepartureDeclaration") with WhatDoYouWantToDoOptions
  case object ViewNorthernIreland extends WithName("viewNorthernIreland") with WhatDoYouWantToDoOptions

  val values: Seq[WhatDoYouWantToDoOptions] = Seq(
    MakeArrivalNotification,
    ViewArrivalNotificationAfter,
    ViewArrivalNotificationBefore,
    MakeDepartureDeclaration,
    ViewDepartureDeclaration,
    ViewNorthernIreland
  )

  def radios(form: Form[_]): Seq[Radios.Item] = {
    val field = form("value")
    val items = Seq(
      Radios.Radio(msg"whatDoYouWantToDo.makeArrivalNotificationText", MakeArrivalNotification.toString),
      Radios.Radio(msg"whatDoYouWantToDo.viewArrivalNotificationAfterText", ViewArrivalNotificationAfter.toString),
      Radios.Radio(msg"whatDoYouWantToDo.viewArrivalNotificationBeforeText", ViewArrivalNotificationBefore.toString),
      Radios.Radio(msg"whatDoYouWantToDo.makeDepartureDeclarationText", MakeDepartureDeclaration.toString),
      Radios.Radio(msg"whatDoYouWantToDo.viewDepartureDeclarationText", ViewDepartureDeclaration.toString),
      Radios.Radio(msg"whatDoYouWantToDo.viewNorthernIrelandText", ViewNorthernIreland.toString)
    )
    Radios(field, items)
  }

  implicit val enumerable: Enumerable[WhatDoYouWantToDoOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
