/*
 * Copyright 2001-2015 Artima, Inc.
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
package org.scalatest

import org.scalactic.PrettyMethods
import Expectation._
import org.scalatest.exceptions.TestFailedException
import SharedHelpers.thisLineNumber

class FactSpec extends FreeSpec with Matchers with PrettyMethods { // with ExpectationHavePropertyMatchers {
/*
  "A Fact" - {
    val falseFact = Falsism("1 did not equal 2", "1 equaled 2", "1 did not equal 2", "1 equaled 2")
    val trueFact = Truism("1 did not equal 2", "1 equaled 2", "1 did not equal 2", "1 equaled 2")
    "should have isTrue and isFalse methods" in {
      falseFact.isTrue shouldBe false
      falseFact.isFalse shouldBe true
      trueFact.isTrue shouldBe true
      trueFact.isFalse shouldBe false
    }
    "should have a toAssertion method that either returns Succeeded or throws TestFailedException with the correct error message and stack depth" in {
      trueFact.toAssertion shouldBe Succeeded
      val caught = the [TestFailedException] thrownBy falseFact.toAssertion
      caught should have message "1 did not equal 2"
      caught.failedCodeLineNumber shouldEqual Some(thisLineNumber - 2)
      caught.failedCodeFileName shouldBe Some("FactSpec.scala")
    }
    "should offer a toBoolean method, even though it is redundant with isTrue" in {
      falseFact.toBoolean shouldBe false
      trueFact.toBoolean shouldBe true
    }
  }
*/
}
