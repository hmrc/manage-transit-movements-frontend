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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

object Format {

  val dateFormatter: DateTimeFormatter               = DateTimeFormatter.ofPattern("yyyyMMdd")
  def dateFormatted(date: LocalDate): String         = date.format(dateFormatter)
  def dateFormatted(dateTime: LocalDateTime): String = dateTime.format(dateFormatter)

  val controlDecisionDateFormatter: DateTimeFormatter               = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  def controlDecisionDateFormatted(date: LocalDate): String         = date.format(controlDecisionDateFormatter)
  def controlDecisionDateFormatted(dateTime: LocalDateTime): String = dateTime.format(controlDecisionDateFormatter)

  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

  val dateDisplayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
