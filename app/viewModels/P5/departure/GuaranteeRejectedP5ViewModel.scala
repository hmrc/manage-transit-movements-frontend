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

package viewModels.P5.departure

import config.FrontendAppConfig
import models.LocalReferenceNumber
import models.departureP5.GuaranteeReference
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import utils.GuaranteeRejectedP5Helper

case class GuaranteeRejectedP5ViewModel(guaranteeReferences: Seq[GuaranteeReference], lrn: LocalReferenceNumber)(implicit messages: Messages) {

  private val multipleGuaranteesOneReference: Boolean = {
    val multipleGuarantee = guaranteeReferences.length > 1
    val oneReference      = guaranteeReferences.forall(_.InvalidGuaranteeReason.length == 1)

    multipleGuarantee && oneReference
  }

  def paragraph1(implicit messages: Messages): String =
    if (guaranteeReferences.length == 1 && guaranteeReferences.head.InvalidGuaranteeReason.length == 1) {
      messages("guarantee.rejected.message.paragraph1.singular")
    } else if (guaranteeReferences.length == 1 && guaranteeReferences.head.InvalidGuaranteeReason.length > 1) {
      messages("guarantee.rejected.message.paragraph1.singularGuaranteePluralReference")
    } else if (multipleGuaranteesOneReference) {
      messages("guarantee.rejected.message.paragraph1.pluralGuaranteeSingularReference")
    } else {
      messages("guarantee.rejected.message.paragraph1.pluralGuaranteePluralReference")
    }

  def paragraph2(implicit messages: Messages): String = if (guaranteeReferences.length == 1 && guaranteeReferences.head.InvalidGuaranteeReason.length == 1) {
    messages("guarantee.rejected.message.contact.singular")
  } else {
    messages("guarantee.rejected.message.contact.plural")
  }

  def buttonContent(implicit messages: Messages): String =
    messages("guarantee.rejected.message.amendErrors")

  def link(implicit messages: Messages): String =
    messages("guarantee.rejected.message.makeAnotherDeparture")

  val toTables: Seq[(String, String, Table)] = new GuaranteeRejectedP5Helper(guaranteeReferences).toTables
}
