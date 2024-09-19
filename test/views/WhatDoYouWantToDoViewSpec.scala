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
import models.{Availability, Feature}
import org.scalacheck.Arbitrary.arbitrary
import play.api.Application
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatDoYouWantToDoView

class WhatDoYouWantToDoViewSpec extends ViewBehaviours with Generators {

  private val arrivalsFeature        = arbitrary[Feature].sample.value
  private val departuresFeature      = arbitrary[Feature].sample.value
  private val draftDeparturesFeature = arbitrary[Feature].sample.value

  private val arrivalsHref        = nonEmptyString.sample.value
  private val departuresHref      = nonEmptyString.sample.value
  private val draftDeparturesHref = nonEmptyString.sample.value

  def applyView(
    application: Application,
    arrivalsFeature: Feature,
    departuresFeature: Feature,
    draftDeparturesFeature: Feature
  ): HtmlFormat.Appendable =
    application.injector
      .instanceOf[WhatDoYouWantToDoView]
      .apply(arrivalsFeature, departuresFeature, draftDeparturesFeature)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable =
    applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature)

  override val prefix: String = "whatDoYouWantToDo"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("h2", "Arrivals")

  behave like pageWithContent("h2", "Departures")

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
      val arrivalsFeature = Feature(Availability.Unavailable, arrivalsHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable")
    }

    "none" - {
      val arrivalsFeature = Feature(Availability.Empty, arrivalsHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "You have no arrival notifications")
    }

    "available" - {
      val arrivalsFeature = Feature(Availability.NonEmpty, arrivalsHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithLink(
        doc,
        "view-arrival-notifications",
        "View arrival notifications",
        arrivalsHref
      )
    }
  }

  "departures" - {
    "unavailable" - {
      val departuresFeature = Feature(Availability.Unavailable, departuresHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "View departure declarations is currently unavailable")
    }

    "none" - {
      val departuresFeature = Feature(Availability.Empty, departuresHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "You have no departure declarations")
    }

    "available" - {
      val departuresFeature = Feature(Availability.NonEmpty, departuresHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithLink(
        doc,
        "view-departure-declarations",
        "View departure declarations",
        departuresHref
      )
    }
  }

  "draft departures" - {
    "unavailable" - {
      val draftDeparturesFeature = Feature(Availability.Unavailable, draftDeparturesHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "Draft departure declarations unavailable")
    }

    "none" - {
      val draftDeparturesFeature = Feature(Availability.Empty, draftDeparturesHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithContent(doc, "p", "You have no draft departure declarations")
    }

    "available" - {
      val draftDeparturesFeature = Feature(Availability.NonEmpty, draftDeparturesHref)

      val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

      behave like pageWithLink(
        doc,
        "view-draft-departures",
        "View draft departure declarations",
        draftDeparturesHref
      )
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
        val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

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
        val doc = parseView(applyView(app, arrivalsFeature, departuresFeature, draftDeparturesFeature))

        behave like pageWithoutContent(doc, "h2", "Guarantees")
        behave like pageWithoutLink(doc, "check-guarantee-balance")
        behave like pageWithoutContent(doc, "p", paragraph)
      }
    }
  }
}
