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

package utils

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}
import javax.xml.datatype.XMLGregorianCalendar

object Format {

  def formatMovementUpdatedDate(movementUpdatedDate: LocalDate): String = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    movementUpdatedDate.format(formatter)
  }

  def formatMovementUpdatedTime(movementUpdatedTime: LocalTime): String = {
    val formatter = DateTimeFormatter.ofPattern("h:mma")
    movementUpdatedTime.format(formatter).toLowerCase
  }

  def formatDeclarationAcceptanceDate(declarationAcceptanceDate: XMLGregorianCalendar): String = {
    val date      = declarationAcceptanceDate.toGregorianCalendar.getTime
    val formatter = new SimpleDateFormat("dd/MM/yyyy")
    formatter.format(date)
  }
}
