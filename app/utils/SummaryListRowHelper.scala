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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Format.controlDecisionDateFormatter

import java.time.LocalDateTime

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

  def formatAsDate(answer: LocalDateTime): Content =
    answer.format(controlDecisionDateFormatter).toText

  protected def formatEnumAsText[T](messageKeyPrefix: String)(answer: T): Content =
    formatEnumAsString(messageKeyPrefix)(answer).toText

  protected def formatEnumAsString[T](messageKeyPrefix: String)(answer: T): String =
    messages(s"$messageKeyPrefix.$answer")

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

  protected def buildRowWithNoChangeLink(
    prefix: String,
    answer: Content,
    args: Any*
  ): SummaryListRow =
    buildSimpleRow(
      prefix = prefix,
      label = messages(s"$prefix", args: _*),
      answer = answer,
      id = None,
      call = None
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

  def buildRowFromPath(
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

  def buildRemovableRow(
    prefix: String,
    answer: Content,
    id: String,
    changeCall: Call,
    removeCall: Call,
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = messages(s"$prefix", args: _*).toKey,
      value = Value(answer),
      actions = Some(
        Actions(items =
          List(
            ActionItem(
              content = messages("site.edit").toText,
              href = changeCall.url,
              visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args: _*)),
              attributes = Map("id" -> s"change-$id")
            ),
            ActionItem(
              content = messages("site.delete").toText,
              href = removeCall.url,
              visuallyHiddenText = Some(messages(s"$prefix.remove.hidden", args: _*)),
              attributes = Map("id" -> s"remove-$id")
            )
          )
        )
      )
    )
}
