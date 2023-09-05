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

import models.departureP5.GuaranteeReference
import play.api.i18n.Messages

case class GuaranteeRejectedP5ViewModel(guaranteeReferences: Seq[GuaranteeReference]) {

  def paragraph1(implicit messages: Messages): String = {

    if (guaranteeReferences.length == 1 && guaranteeReferences.head.InvalidGuaranteeReason.length == 1) {
      messages("")
    } else if (guaranteeReferences.length == 1 && guaranteeReferences.head.InvalidGuaranteeReason.length > 1) {
      messages("")
    } else if ()
  }

//Paragraph - 1 guarantee; 1 error code:
//  There is a problem with the guarantee in this declaration. Amend the error and resend the declaration.
//  Paragraph - 1 guarantee; 2+ error codes:
//  There is a problem with the guarantee in this declaration. Amend the errors and resend the declaration.
//  Paragraph - 2+ guarantees; 1 error code:
//  There is a problem with the guarantees in this declaration. Amend the error and resend the declaration.
//  Paragraph - 2+ guarantees; 2+ error codes:
//  There is a problem with the guarantees in this declaration. Amend the errors and resend the declaration.
//  Label:

}
