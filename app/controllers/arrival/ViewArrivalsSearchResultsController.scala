/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.arrival

import config.{FrontendAppConfig, SearchResultsAppConfig}
import connectors.ArrivalMovementConnector
import controllers.actions.IdentifierAction
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{ViewArrival, ViewArrivalMovements}
import views.html.arrival.ViewArrivalsSearchResultsView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewArrivalsSearchResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  connector: ArrivalMovementConnector,
  searchResultsAppConfig: SearchResultsAppConfig,
  view: ViewArrivalsSearchResultsView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private lazy val pageSize = searchResultsAppConfig.maxSearchResults

  def onPageLoad(mrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      mrn.trim match {
        case mrn if mrn.isEmpty =>
          Future.successful(Redirect(routes.ViewAllArrivalsController.onPageLoad(None)))
        case mrn =>
          connector.getArrivalSearchResults(mrn, pageSize).flatMap {
            case Some(allArrivals) =>
              val viewMovements: Seq[ViewArrival] = allArrivals.arrivals.map(ViewArrival(_))

              Future.successful(
                Ok(
                  view(
                    mrn = mrn,
                    dataRows = ViewArrivalMovements.apply(viewMovements).dataRows,
                    retrieved = allArrivals.retrievedArrivals,
                    tooManyResults = allArrivals.tooManyResults
                  )
                )
              )

            case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
      }
  }
}
