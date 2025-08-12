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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.GuaranteeReference
import models.departureP5.GuaranteeReferenceTable
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel.GuaranteeRejectedNotAmendableP5ViewModelProvider

class GuaranteeRejectedNotAmendableP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "GuaranteeRejectedP5ViewModel" - {

    val acceptanceDate = arbitraryXMLGregorianCalendar.arbitrary.sample.value

    "when there is one guarantee with one error" - {

      val guaranteeReferences = Seq(
        GuaranteeReference(
          grn = "GRN 1",
          invalidGuarantees = Seq(
            models.InvalidGuaranteeReason(
              error = "Code 1_1 - Description 1_1",
              furtherInformation = Some("Text 1_1")
            )
          )
        )
      )

      val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider()
      val result            = viewModelProvider.apply(guaranteeReferences, lrn.toString, mrn, acceptanceDate)

      "must return correct number of guarantees" in {
        result.tables mustEqual Seq(
          GuaranteeReferenceTable(
            title = "Guarantee reference 1",
            grn = "GRN 1",
            table = new Table(
              rows = Seq(
                Seq(
                  TableRow(content = Text("Code 1_1 - Description 1_1")),
                  TableRow(content = Text("Text 1_1"))
                )
              ),
              head = Some(
                Seq(
                  HeadCell(content = Text("Error")),
                  HeadCell(content = Text("Further information"))
                )
              )
            )
          )
        )
      }

      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual
          "There is a problem with the guarantee in this declaration. Review the error and make a new declaration with the right information."
      }

      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)."
      }

      "must return link content" in {
        result.link mustEqual "Make another departure declaration"
      }
    }

    "when there is one guarantee with multiple errors" - {

      val guaranteeReferences = Seq(
        GuaranteeReference(
          grn = "GRN 1",
          invalidGuarantees = Seq(
            models.InvalidGuaranteeReason(
              error = "Code 1_1 - Description 1_1",
              furtherInformation = Some("Text 1_1")
            ),
            models.InvalidGuaranteeReason(
              error = "Code 1_2 - Description 1_2",
              furtherInformation = Some("Text 1_2")
            )
          )
        )
      )

      val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider()
      val result            = viewModelProvider.apply(guaranteeReferences, lrn.toString, mrn, acceptanceDate)

      "must return correct number of guarantees" in {
        result.tables mustEqual Seq(
          GuaranteeReferenceTable(
            title = "Guarantee reference 1",
            grn = "GRN 1",
            table = new Table(
              rows = Seq(
                Seq(
                  TableRow(content = Text("Code 1_1 - Description 1_1")),
                  TableRow(content = Text("Text 1_1"))
                ),
                Seq(
                  TableRow(content = Text("Code 1_2 - Description 1_2")),
                  TableRow(content = Text("Text 1_2"))
                )
              ),
              head = Some(
                Seq(
                  HeadCell(content = Text("Error")),
                  HeadCell(content = Text("Further information"))
                )
              )
            )
          )
        )
      }

      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual
          "There is a problem with the guarantee in this declaration. Review the errors and make a new declaration with the right information."
      }

      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
      }

      "must return link content" in {
        result.link mustEqual "Make another departure declaration"
      }
    }

    "when there are multiple guarantees with multiple errors" - {

      val guaranteeReferences = Seq(
        GuaranteeReference(
          grn = "GRN 1",
          invalidGuarantees = Seq(
            models.InvalidGuaranteeReason(
              error = "Code 1_1 - Description 1_1",
              furtherInformation = Some("Text 1_1")
            ),
            models.InvalidGuaranteeReason(
              error = "Code 1_2 - Description 1_2",
              furtherInformation = Some("Text 1_2")
            )
          )
        ),
        GuaranteeReference(
          grn = "GRN 2",
          invalidGuarantees = Seq(
            models.InvalidGuaranteeReason(
              error = "Code 2_1 - Description 2_1",
              furtherInformation = Some("Text 2_1")
            ),
            models.InvalidGuaranteeReason(
              error = "Code 2_2 - Description 2_2",
              furtherInformation = Some("Text 2_2")
            )
          )
        )
      )

      val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider()
      val result            = viewModelProvider.apply(guaranteeReferences, lrn.toString, mrn, acceptanceDate)

      "must return correct number of guarantees" in {
        result.tables mustEqual Seq(
          GuaranteeReferenceTable(
            title = "Guarantee reference 1",
            grn = "GRN 1",
            table = new Table(
              rows = Seq(
                Seq(
                  TableRow(content = Text("Code 1_1 - Description 1_1")),
                  TableRow(content = Text("Text 1_1"))
                ),
                Seq(
                  TableRow(content = Text("Code 1_2 - Description 1_2")),
                  TableRow(content = Text("Text 1_2"))
                )
              ),
              head = Some(
                Seq(
                  HeadCell(content = Text("Error")),
                  HeadCell(content = Text("Further information"))
                )
              )
            )
          ),
          GuaranteeReferenceTable(
            title = "Guarantee reference 2",
            grn = "GRN 2",
            table = new Table(
              rows = Seq(
                Seq(
                  TableRow(content = Text("Code 2_1 - Description 2_1")),
                  TableRow(content = Text("Text 2_1"))
                ),
                Seq(
                  TableRow(content = Text("Code 2_2 - Description 2_2")),
                  TableRow(content = Text("Text 2_2"))
                )
              ),
              head = Some(
                Seq(
                  HeadCell(content = Text("Error")),
                  HeadCell(content = Text("Further information"))
                )
              )
            )
          )
        )
      }

      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual
          "There is a problem with the guarantees in this declaration. Review the errors and make a new declaration with the right information."
      }

      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
      }

      "must return link content" in {
        result.link mustEqual "Make another departure declaration"
      }
    }
  }
}
