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

import generated.{GNSSType, RecoveryNotificationType}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.text.SimpleDateFormat
import java.util.Currency
import javax.xml.datatype.XMLGregorianCalendar
import scala.util.{Success, Try}

class SummaryListRowHelper(implicit messages: Messages) {

  def extractOptionalRow(x: Option[SummaryListRow]): Seq[SummaryListRow] = x.map(Seq(_)).getOrElse(Seq.empty)

  def formatAsYesOrNo(answer: Boolean): Content =
    messages {
      if (answer) {
        "site.yes"
      } else {
        "site.no"
      }
    }.toText

  protected def formatAsText[T](answer: T): Content = s"$answer".toText

  protected def formatAsCoordinates(answer: GNSSType): Content =
    s"(${answer.latitude}, ${answer.longitude})".toText

  def formatAsDate(answer: XMLGregorianCalendar): Content = {
    val date      = answer.toGregorianCalendar.getTime
    val formatter = new SimpleDateFormat("dd MMMM yyyy")
    formatter
      .format(date)
      .toText
  }

  def formatAsDateAndTime(answer: XMLGregorianCalendar): Content = {
    val date      = answer.toGregorianCalendar.getTime
    val formatter = new SimpleDateFormat("dd MMMM yyyy 'at' h:mma")
    formatter
      .format(date)
      .replace("PM", "pm")
      .replace("AM", "am")
      .toText
  }

  def formatAsIncidentDateTime(answer: XMLGregorianCalendar): Content = {
    val date      = answer.toGregorianCalendar.getTime
    val formatter = new SimpleDateFormat("d MMMM yyyy HH:mm")
    formatter.format(date).toText
  }

  def formatAsCurrency(recoveryNotification: RecoveryNotificationType): Content = {
    val value = recoveryNotification match {
      case RecoveryNotificationType(_, _, amountClaimed, currency) =>
        Try(Currency.getInstance(currency).getSymbol) match {
          case Success(currency) => s"$currency$amountClaimed"
          case _                 => s"$amountClaimed $currency"
        }
    }
    value.toText
  }

  def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Option[Call],
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = messages(s"$prefix", args: _*).toKey,
      value = Value(answer),
      actions = call.map {
        x =>
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = x.url,
                visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args: _*)),
                attributes = id.fold[Map[String, String]](Map.empty)(
                  id => Map("id" -> id)
                )
              )
            )
          )
      }
    )

  protected def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): SummaryListRow =
    buildSimpleRow(
      prefix = prefix,
      label = messages(s"$prefix", args: _*),
      answer = answer,
      id = id,
      call = Some(call),
      args = args: _*
    )

  protected def buildSimpleRow(
    prefix: String,
    label: String,
    answer: Content,
    id: Option[String],
    call: Option[Call],
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = label.toKey,
      value = Value(answer),
      actions = call.map {
        route =>
          Actions(
            items = List(
              ActionItem(
                content = messages("site.edit").toText,
                href = route.url,
                visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args: _*)),
                attributes = id.fold[Map[String, String]](Map.empty)(
                  id => Map("id" -> id)
                )
              )
            )
          )
      }
    )
}
