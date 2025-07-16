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

package models

import base.SpecBase
import models.ArrivalId.*
import play.api.mvc.PathBindable

class ArrivalIdSpec extends SpecBase {

  "ArrivalId" - {

    "bind a path parameter correctly" in {
      val pathBindable = implicitly[PathBindable[ArrivalId]]
      val key          = "arrivalId"
      val value        = "456"

      val bindResult = pathBindable.bind(key, value)
      bindResult.value mustEqual ArrivalId(456)
    }

    "unbind a path parameter correctly" in {
      val pathBindable = implicitly[PathBindable[ArrivalId]]
      val key          = "arrivalId"
      val arrivalId    = ArrivalId(789)

      val unbindResult = pathBindable.unbind(key, arrivalId)
      unbindResult mustEqual
        "789"
    }

  }
}
