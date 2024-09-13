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

package views

import generators.Generators
import models.{Availability, Feature, Features}
import org.scalacheck.Arbitrary.arbitrary
import play.api.Application
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatDoYouWantToDoView

class WhatDoYouWantToDoViewSpec extends ViewBehaviours with Generators {

  private val arrivalsFeatures        = arbitrary[Features].sample.value
  private val departuresFeatures      = arbitrary[Features].sample.value
  private val draftDeparturesFeatures = arbitrary[Features].sample.value

  private val arrivalsAvailability   = arbitrary[Availability].sample.value
  private val departuresAvailability = arbitrary[Availability].sample.value

  private val p5ArrivalsHref      = nonEmptyString.sample.value
  private val p5DeparturesHref    = nonEmptyString.sample.value
  private val draftDeparturesHref = nonEmptyString.sample.value

  def applyView(
    application: Application,
    arrivalsFeatures: Features,
    departuresFeatures: Features,
    draftDeparturesFeatures: Features,
    isOnLegacyEnrolment: Boolean
  ): HtmlFormat.Appendable =
    application.injector
      .instanceOf[WhatDoYouWantToDoView]
      .apply(arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable =
    applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment)

  override val prefix: String = "whatDoYouWantToDo"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("h2", "Arrivals")

  behave like pageWithContent("h2", "Departures")

  "phase 5 enabled" - {
    val isOnLegacyEnrolment = true

    val arrivalsFeatures = Features(
      phase4 = None,
      phase5 = Some(Feature(arrivalsAvailability, enabled = true, p5ArrivalsHref))
    )

    val departuresFeatures = Features(
      phase4 = None,
      phase5 = Some(Feature(departuresAvailability, enabled = true, p5DeparturesHref))
    )

    val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

    behave like pageWithLink(
      doc,
      "make-arrival-notification",
      "Make an arrival notification",
      "http://localhost:10121/manage-transit-movements/arrivals"
    )

    behave like pageWithLink(
      doc,
      "make-departure-declaration",
      "Make a departure declaration",
      "http://localhost:10120/manage-transit-movements/departures"
    )

    "arrivals" - {
      "unavailable" - {
        val arrivalsFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Unavailable, enabled = true, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable")
      }

      "none" - {
        val arrivalsFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, enabled = true, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "You have no arrival notifications")
      }

      "available" - {
        val arrivalsFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, enabled = true, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithLink(
          doc,
          "view-arrival-notifications",
          "View arrival notifications",
          p5ArrivalsHref
        )
      }
    }

    "departures" - {
      "unavailable" - {
        val departuresFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Unavailable, enabled = true, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "View departure declarations is currently unavailable")
      }

      "none" - {
        val departuresFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, enabled = true, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "You have no departure declarations")
      }

      "available" - {
        val departuresFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, enabled = true, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithLink(
          doc,
          "view-departure-declarations",
          "View departure declarations",
          p5DeparturesHref
        )
      }
    }

    "draft departures" - {
      val enabled = true

      "unavailable" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Unavailable, enabled, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "Draft departure declarations unavailable")
      }

      "none" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, enabled, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "p", "You have no draft departure declarations")
      }

      "available" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, enabled, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithLink(
          doc,
          "view-draft-departures",
          "View draft departure declarations",
          draftDeparturesHref
        )
      }
    }
  }

  "guarantee balance" - {
    val paragraph =
      "You can only check guarantee balances for GB declarations between 28 June and 1 July. Balances for XI declarations will be available from 1 July."

    val href = "http://localhost:9462/check-transit-guarantee-balance/start?referral=ncts"

    "when enabled" - {
      val app = super
        .guiceApplicationBuilder()
        .configure(
          "microservice.services.features.isGuaranteeBalanceEnabled" -> true
        )
        .build()

      running(app) {
        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithContent(doc, "h2", "Guarantees")

        behave like pageWithLink(
          doc,
          "check-guarantee-balance",
          "Check your guarantee balance",
          href
        )

        behave like pageWithoutContent(doc, "p", paragraph)
      }
    }

    "when disabled" - {
      val app = super
        .guiceApplicationBuilder()
        .configure(
          "microservice.services.features.isGuaranteeBalanceEnabled" -> false
        )
        .build()

      running(app) {
        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment))

        behave like pageWithoutContent(doc, "h2", "Guarantees")
        behave like pageWithoutLink(doc, "check-guarantee-balance")
        behave like pageWithoutContent(doc, "p", paragraph)
      }
    }
  }

  "warning text" - {
    "must render" - {
      "when on legacy enrolment" - {
        val app = super
          .guiceApplicationBuilder()
          .build()

        running(app) {
          val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment = true))

          behave like pageWithWarningText(
            doc,
            "You need to upgrade your NCTS subscription Phase 5 of NCTS went live on 1 July 2024. To continue using NCTS, you need to upgrade your subscription from Phase 4 to Phase 5."
          )
        }
      }
    }

    "must not render" - {
      "when not on legacy enrolment" - {
        val app = super
          .guiceApplicationBuilder()
          .build()

        running(app) {
          val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures, isOnLegacyEnrolment = false))

          behave like pageWithoutWarningText(doc)
        }
      }
    }
  }
}
