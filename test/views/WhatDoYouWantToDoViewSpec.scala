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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatDoYouWantToDoView

class WhatDoYouWantToDoViewSpec extends ViewBehaviours with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(
        "microservice.services.features.isPhase5Enabled" -> false
      )

  private val arrivalsFeatures        = arbitrary[Features].sample.value
  private val departuresFeatures      = arbitrary[Features].sample.value
  private val draftDeparturesFeatures = arbitrary[Features].sample.value

  private val arrivalsAvailability   = arbitrary[Availability].sample.value
  private val departuresAvailability = arbitrary[Availability].sample.value

  private val p4ArrivalsHref      = nonEmptyString.sample.value
  private val p4DeparturesHref    = nonEmptyString.sample.value
  private val p5ArrivalsHref      = nonEmptyString.sample.value
  private val p5DeparturesHref    = nonEmptyString.sample.value
  private val draftDeparturesHref = nonEmptyString.sample.value

  def applyView(
    application: Application,
    arrivalsFeatures: Features,
    departuresFeatures: Features,
    draftDeparturesFeatures: Features
  ): HtmlFormat.Appendable =
    application.injector
      .instanceOf[WhatDoYouWantToDoView]
      .apply(arrivalsFeatures, departuresFeatures, draftDeparturesFeatures)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable =
    applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures)

  override val prefix: String = "whatDoYouWantToDo"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("h2", "Arrivals")

  behave like pageWithContent("h2", "Departures")

  "phase 4 enabled and phase 5 disabled" - {
    val arrivalsFeatures = Features(
      phase4 = Some(Feature(arrivalsAvailability, p4ArrivalsHref)),
      phase5 = None
    )

    val departuresFeatures = Features(
      phase4 = Some(Feature(departuresAvailability, p4DeparturesHref)),
      phase5 = None
    )

    val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

    behave like pageWithLink(
      doc,
      "make-arrival-notification",
      "Make an arrival notification",
      "http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number"
    )

    behave like pageWithLink(
      doc,
      "make-departure-declaration",
      "Make a departure declaration",
      "http://localhost:9489/manage-transit-movements-departures/local-reference-number"
    )

    "arrivals" - {
      "unavailable" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.Unavailable, p4ArrivalsHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable")
      }

      "none" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.Empty, p4ArrivalsHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no arrival notifications")
      }

      "available" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.NonEmpty, p4ArrivalsHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-arrival-notifications",
          "View arrival notifications",
          p4ArrivalsHref
        )
      }
    }

    "departures" - {
      "unavailable" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.Unavailable, p4DeparturesHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View departure declarations is currently unavailable")
      }

      "none" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.Empty, p4DeparturesHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no departure declarations")
      }

      "available" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.NonEmpty, p4DeparturesHref)),
          phase5 = None
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-departure-declarations",
          "View departure declarations",
          p4DeparturesHref
        )
      }
    }

    "draft departures" - {
      val draftDeparturesFeatures = Features(
        phase4 = None,
        phase5 = None
      )

      val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

      behave like pageWithoutLink(
        doc,
        "view-draft-departures"
      )
    }
  }

  "phase 4 disabled and phase 5 enabled" - {
    val arrivalsFeatures = Features(
      phase4 = None,
      phase5 = Some(Feature(arrivalsAvailability, p5ArrivalsHref))
    )

    val departuresFeatures = Features(
      phase4 = None,
      phase5 = Some(Feature(departuresAvailability, p5DeparturesHref))
    )

    val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

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
          phase5 = Some(Feature(Availability.Unavailable, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable")
      }

      "none" - {
        val arrivalsFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no arrival notifications")
      }

      "available" - {
        val arrivalsFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

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
          phase5 = Some(Feature(Availability.Unavailable, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View departure declarations is currently unavailable")
      }

      "none" - {
        val departuresFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no departure declarations")
      }

      "available" - {
        val departuresFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-departure-declarations",
          "View departure declarations",
          p5DeparturesHref
        )
      }
    }

    "draft departures" - {
      "unavailable" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Unavailable, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "Draft departure declarations unavailable")
      }

      "none" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.Empty, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no draft departure declarations")
      }

      "available" - {
        val draftDeparturesFeatures = Features(
          phase4 = None,
          phase5 = Some(Feature(Availability.NonEmpty, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-draft-departures",
          "View draft departure declarations",
          draftDeparturesHref
        )
      }
    }
  }

  "phase 4 enabled and phase 5 enabled" - {
    val arrivalsFeatures = Features(
      phase4 = Some(Feature(arrivalsAvailability, p4ArrivalsHref)),
      phase5 = Some(Feature(arrivalsAvailability, p5ArrivalsHref))
    )

    val departuresFeatures = Features(
      phase4 = Some(Feature(departuresAvailability, p4DeparturesHref)),
      phase5 = Some(Feature(departuresAvailability, p5DeparturesHref))
    )

    val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

    behave like pageWithLink(
      doc,
      "make-ncts-4-arrival-notification",
      "Make a GB arrival notification (NCTS 4)",
      "http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number"
    )

    behave like pageWithLink(
      doc,
      "make-ncts-5-arrival-notification",
      "Make an XI arrival notification (NCTS 5)",
      "http://localhost:10121/manage-transit-movements/arrivals"
    )

    behave like pageWithLink(
      doc,
      "make-ncts-4-departure-declaration",
      "Make a GB departure declaration (NCTS 4)",
      "http://localhost:9489/manage-transit-movements-departures/local-reference-number"
    )

    behave like pageWithLink(
      doc,
      "make-ncts-5-departure-declaration",
      "Make an XI departure declaration (NCTS 5)",
      "http://localhost:10120/manage-transit-movements/departures"
    )

    "arrivals" - {
      "unavailable" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.Unavailable, p4ArrivalsHref)),
          phase5 = Some(Feature(Availability.Unavailable, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View NCTS 4 arrival notifications is currently unavailable")
        behave like pageWithContent(doc, "p", "View NCTS 5 arrival notifications is currently unavailable")
      }

      "none" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.Empty, p4ArrivalsHref)),
          phase5 = Some(Feature(Availability.Empty, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no NCTS 4 arrival notifications")
        behave like pageWithContent(doc, "p", "You have no NCTS 5 arrival notifications")
      }

      "available" - {
        val arrivalsFeatures = Features(
          phase4 = Some(Feature(Availability.NonEmpty, p4ArrivalsHref)),
          phase5 = Some(Feature(Availability.NonEmpty, p5ArrivalsHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-ncts-4-arrival-notifications",
          "View NCTS 4 arrival notifications",
          p4ArrivalsHref
        )

        behave like pageWithLink(
          doc,
          "view-ncts-5-arrival-notifications",
          "View NCTS 5 arrival notifications",
          p5ArrivalsHref
        )
      }
    }

    "departures" - {
      "unavailable" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.Unavailable, p4DeparturesHref)),
          phase5 = Some(Feature(Availability.Unavailable, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "View NCTS 4 departure declarations is currently unavailable")
        behave like pageWithContent(doc, "p", "View NCTS 5 departure declarations is currently unavailable")
      }

      "none" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.Empty, p4DeparturesHref)),
          phase5 = Some(Feature(Availability.Empty, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no NCTS 4 departure declarations")
        behave like pageWithContent(doc, "p", "You have no NCTS 5 departure declarations")
      }

      "available" - {
        val departuresFeatures = Features(
          phase4 = Some(Feature(Availability.NonEmpty, p4DeparturesHref)),
          phase5 = Some(Feature(Availability.NonEmpty, p5DeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-ncts-4-departure-declarations",
          "View NCTS 4 departure declarations",
          p4DeparturesHref
        )

        behave like pageWithLink(
          doc,
          "view-ncts-5-departure-declarations",
          "View NCTS 5 departure declarations",
          p5DeparturesHref
        )
      }
    }

    "draft departures" - {
      "unavailable" - {
        val draftDeparturesFeatures = Features(
          phase4 = Some(Feature(Availability.Unavailable, draftDeparturesHref)),
          phase5 = Some(Feature(Availability.Unavailable, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "Draft departure declarations unavailable")
      }

      "none" - {
        val draftDeparturesFeatures = Features(
          phase4 = Some(Feature(Availability.Empty, draftDeparturesHref)),
          phase5 = Some(Feature(Availability.Empty, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "p", "You have no draft departure declarations")
      }

      "available" - {
        val draftDeparturesFeatures = Features(
          phase4 = Some(Feature(Availability.NonEmpty, draftDeparturesHref)),
          phase5 = Some(Feature(Availability.NonEmpty, draftDeparturesHref))
        )

        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithLink(
          doc,
          "view-draft-departures",
          "View draft departure declarations",
          draftDeparturesHref
        )
      }
    }
  }

  "phase 4 disabled and phase 5 disabled" - {
    val arrivalsFeatures = Features(
      phase4 = None,
      phase5 = None
    )

    val departuresFeatures = Features(
      phase4 = None,
      phase5 = None
    )

    val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

    behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable")
    behave like pageWithContent(doc, "p", "View departure declarations is currently unavailable")
  }

  "guarantee balance" - {
    "when enabled" - {
      val app = super
        .guiceApplicationBuilder()
        .configure(
          "microservice.services.features.isGuaranteeBalanceEnabled" -> true
        )
        .build()

      running(app) {
        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithContent(doc, "h2", "Guarantees")
        behave like pageWithLink(
          doc,
          "check-guarantee-balance",
          "Check your guarantee balance",
          "http://localhost:9462/check-transit-guarantee-balance/start?referral=ncts"
        )
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
        val doc = parseView(applyView(app, arrivalsFeatures, departuresFeatures, draftDeparturesFeatures))

        behave like pageWithoutContent(doc, "h2", "Guarantees")
        behave like pageWithoutLink(
          doc,
          "check-guarantee-balance"
        )
      }
    }
  }
}
