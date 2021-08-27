/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import controllers.actions._
import forms.WhatDoYouWantToDoFormProvider
import models.{Arrivals, Departures, WhatDoYouWantToDoOptions}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import controllers.departure.{routes => departureRoutes}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  renderer: Renderer,
  formProvider: WhatDoYouWantToDoFormProvider,
  val arrivalMovementConnector: ArrivalMovementConnector,
  val departuresMovementConnector: DeparturesMovementConnector,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (Action andThen identify) async {
    implicit request =>
      val form = formProvider()

      if (appConfig.isNIJourneyEnabled) {
        for {
          arrivals   <- arrivalMovementConnector.getArrivals()
          departures <- departuresMovementConnector.getDepartures()
          html       <- renderIndexPage(arrivals, departures)
        } yield Ok(html)
      } else {

        val json = Json.obj(
          "form"        -> form,
          "radios"      -> WhatDoYouWantToDoOptions.radios(form),
          "warningText" -> msg"whatDoYouWantToDo.warningText"
        )

        renderer.render("whatDoYouWantToDo.njk", json).map(Ok(_))
      }
  }

  def onSubmit(): Action[AnyContent] = (Action andThen identify) async {
    implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj(
              "form"        -> formWithErrors,
              "radios"      -> WhatDoYouWantToDoOptions.radios(formProvider()),
              "warningText" -> msg"whatDoYouWantToDo.warningText"
            )
            renderer.render("whatDoYouWantToDo.njk", json).map(BadRequest(_))
          },
          {
            case WhatDoYouWantToDoOptions.GBMovements =>
              Future.successful(Redirect(routes.IndexController.onPageLoad()))
            case WhatDoYouWantToDoOptions.NorthernIrelandMovements =>
              Future.successful(Redirect(routes.NorthernIrelandInterstitialController.onPageLoad()))
          }
        )
  }

  private def renderIndexPage(arrivals: Option[Arrivals], departures: Option[Departures])(implicit requestHeader: RequestHeader): Future[Html] =
    renderer
      .render(
        "index.njk",
        Json.obj(
          "declareArrivalNotificationUrl"  -> appConfig.declareArrivalNotificationStartUrl,
          "viewArrivalNotificationUrl"     -> controllers.arrival.routes.ViewArrivalsController.onPageLoad().url,
          "arrivalsAvailable"              -> arrivals.nonEmpty,
          "hasArrivals"                    -> arrivals.exists(_.arrivals.nonEmpty),
          "declareDepartureDeclarationUrl" -> appConfig.declareDepartureStartWithLRNUrl,
          "viewDepartureNotificationUrl"   -> departureRoutes.ViewDeparturesController.onPageLoad().url,
          "departuresAvailable"            -> departures.nonEmpty,
          "hasDepartures"                  -> departures.exists(_.departures.nonEmpty)
        )
      )

}
