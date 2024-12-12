/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.SpecBase
import connectors.DeparturesDraftsP5Connector
import models.departure.drafts.{Limit, Skip}
import models.{DeparturesSummary, LockCheck, Sort}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class DraftDepartureServiceSpec extends SpecBase {

  val mockConnector: DeparturesDraftsP5Connector = mock[DeparturesDraftsP5Connector]
  val service                                    = new DraftDepartureService(mockConnector)

  "MongoDraftDepartureService" - {

    "getLRNs" - {
      "the connector returns a valid DeparturesSummary" in {
        val mockDeparturesSummary = mock[DeparturesSummary]
        when(mockConnector.lrnFuzzySearch(any[String], any[Limit])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mockDeparturesSummary)))

        val lrn   = "lrn"
        val limit = Limit(1)

        whenReady(service.getLRNs(lrn, limit)) {
          result =>
            result must be(Some(mockDeparturesSummary))
        }

        verify(mockConnector).lrnFuzzySearch(lrn, limit)(hc)
      }
    }

    "sortOrGetDrafts" - {

      "sortDraftDepartures without LRN" - {
        "the connector returns a valid DeparturesSummary" in {
          val mockDeparturesSummary = mock[DeparturesSummary]
          val sortParams            = mock[Sort]
          val limit                 = Limit(1)
          val skip                  = Skip(2)

          when(mockConnector.sortDraftDepartures(any[Sort], any[Limit], any[Skip])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(mockDeparturesSummary)))

          whenReady(service.sortOrGetDrafts(None, Some(sortParams), limit, skip)) {
            result =>
              result must be(Some(mockDeparturesSummary))
          }

          verify(mockConnector).sortDraftDepartures(sortParams, limit, skip)(hc)
        }
      }

      "sortDraftDepartures with LRN" - {
        "the connector returns a valid DeparturesSummary" in {
          val mockDeparturesSummary = mock[DeparturesSummary]
          val sortParams            = mock[Sort]
          val lrn                   = "lrn"
          val limit                 = Limit(10)
          val skip                  = Skip(5)

          when(mockConnector.sortDraftDepartures(any[Sort], any[Limit], any[Skip], any[String])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(mockDeparturesSummary)))

          whenReady(service.sortOrGetDrafts(Some(lrn), Some(sortParams), limit, skip)) {
            result =>
              result must be(Some(mockDeparturesSummary))
          }

          verify(mockConnector).sortDraftDepartures(sortParams, limit, skip, lrn)(hc)
        }
      }

      "getLRNs with skip and limit" - {
        "the connector returns a valid DeparturesSummary" in {
          val mockDeparturesSummary = mock[DeparturesSummary]
          val partialLRN            = "lrn"
          val skip                  = Skip(1)
          val limit                 = Limit(2)

          when(mockConnector.getLRNs(any[String], any[Skip], any[Limit])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(mockDeparturesSummary)))

          whenReady(service.sortOrGetDrafts(Some(partialLRN), None, limit, skip)) {
            result =>
              result must be(Some(mockDeparturesSummary))
          }

          verify(mockConnector).getLRNs(partialLRN, skip, limit)(hc)
        }
      }

      "getPagedDepartureSummary" - {
        "the connector returns a valid DeparturesSummary" in {
          val mockDeparturesSummary = mock[DeparturesSummary]
          val limit                 = Limit(1)
          val skip                  = Skip(2)

          when(mockConnector.getAllDeparturesSummary(any[Limit], any[Skip])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(mockDeparturesSummary)))

          whenReady(service.sortOrGetDrafts(None, None, limit, skip)) {
            result =>
              result must be(Some(mockDeparturesSummary))
          }

          verify(mockConnector).getAllDeparturesSummary(limit, skip)(hc)
        }
      }
    }

    "deleteDraftDeparture" - {
      "the connector returns a successful HttpResponse" in {
        val mockResponse = mock[HttpResponse]
        val lrn          = "lrn"

        when(mockConnector.deleteDraftDeparture(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(mockResponse))

        whenReady(service.deleteDraftDeparture(lrn)) {
          result =>
            result must be(mockResponse)
        }

        verify(mockConnector).deleteDraftDeparture(lrn)(hc)
      }
    }

    "checkLock" - {
      "the connector returns a LockCheck" in {
        val mockLockCheck = mock[LockCheck]
        val lrn           = "lrn"

        when(mockConnector.checkLock(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(mockLockCheck))

        whenReady(service.checkLock(lrn)) {
          result =>
            result must be(mockLockCheck)
        }

        verify(mockConnector).checkLock(lrn)(hc)
      }
    }
  }
}
