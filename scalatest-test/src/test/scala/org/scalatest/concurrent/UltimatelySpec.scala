/*
 * Copyright 2001-2013 Artima, Inc.
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
package org.scalatest.concurrent

import org.scalatest._
import Ultimately._
import SharedHelpers.thisLineNumber
import time.{Millisecond, Span, Millis}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.exceptions.TestPendingException
import org.scalatest.exceptions.TestFailedDueToTimeoutException
// SKIP-SCALATESTJS,NATIVE-START
import SharedHelpers.serializeRoundtrip
// SKIP-SCALATESTJS,NATIVE-END
import scala.concurrent.Future


class UltimatelySpec extends AsyncFunSpec with Matchers with OptionValues {

  describe("The ultimately construct") {

    it("should just return if the by-name returns normally") {

      ultimately { Future { 1 + 1 should equal (2) } }
    }

    it("should invoke the function just once if the by-name returns normally the first time") {

      var count = 0
      ultimately {
        Future { 
          count += 1
          1 + 1 should equal (2)
        }
      } map { _ =>
        count should equal (1)
      }
    }

    it("should invoke the function just once and return the result if the by-name returns normally the first time") {

      var count = 0
      ultimately {
        Future {
          count += 1
          99
        }
      } map { result =>
        count should equal (1)
        result should equal (99)
      }
    }

    it("should invoke the function five times if the by-name throws an exception four times before finally returning normally the fifth time") {

      var count = 0
      ultimately {
        Future {
          count += 1
          if (count < 5) throw new Exception
          1 + 1 should equal (2)
        }
      } map { _ => 
        count should equal (5)
      }
    }

    it("should ultimately blow up with a TestFailedDueToTimeoutException if the by-name continuously throws an exception") {

      var count = 0
      recoverToExceptionIf[TestFailedDueToTimeoutException] {
        ultimately {
          Future {
            count += 1
            throw new RuntimeException
            ()
          }
        }
      } map { caught =>
        caught.message.value should include ("Attempted " + count.toString + " times")
        caught.failedCodeLineNumber.value should equal (thisLineNumber - 9)
        caught.failedCodeFileName.value should be ("UltimatelySpec.scala")
        caught.timeout should be (Span(150, Millis))
      }
    }

    it("should ultimately blow up with a TFE if the by-name continuously throws an exception, and include the last failure message in the TFE message") {

      var count = 0
      recoverToExceptionIf[TestFailedException] {
        ultimately {
          Future {
            count += 1
            1 + 1 should equal (3)
          }
        }
      } map { caught =>
        caught.message.value should include ("Attempted " + count.toString + " times")
        caught.message.value should include ("2 did not equal 3")
        caught.failedCodeLineNumber.value should equal (thisLineNumber - 9)
        caught.failedCodeFileName.value should be ("UltimatelySpec.scala")
        caught.getCause.getClass.getName should be ("org.scalatest.exceptions.TestFailedException")
        caught.getCause.getMessage should be ("2 did not equal 3")
      }
    }

    it("should provides correct stack depth when ultimately is called from the overload method") {

      recoverToExceptionIf[TestFailedException] {
        ultimately(timeout(Span(100, Millis)), interval(Span(1, Millisecond))) { Future { 1 + 1 should equal (3) } }
      } map { caught1 =>
        caught1.failedCodeLineNumber.value should equal (thisLineNumber - 2)
        caught1.failedCodeFileName.value should be ("UltimatelySpec.scala")
      }
      
      recoverToExceptionIf[TestFailedException] {
        ultimately(timeout(Span(100, Millis))) { Future { 1 + 1 should equal (3) } }
      } map { caught3 =>
        caught3.failedCodeLineNumber.value should equal (thisLineNumber - 2)
        caught3.failedCodeFileName.value should be ("UltimatelySpec.scala")
      }
      
      recoverToExceptionIf[TestFailedException] {
        ultimately(interval(Span(1, Millisecond))) { Future { 1 + 1 should equal (3) }  }
      } map { caught4 =>
        caught4.failedCodeLineNumber.value should equal (thisLineNumber - 2)
        caught4.failedCodeFileName.value should be ("UltimatelySpec.scala")
      }
    }

    it("should by default invoke an always-failing by-name for at least 150 millis") {
      var startTime: Option[Long] = None
      recoverToSucceededIf[TestFailedException] {
        startTime = Some(System.currentTimeMillis)
        ultimately {
          Future { 1 + 1 should equal (3) }
        }
      } map { _ =>
        (System.currentTimeMillis - startTime.get).toInt should be >= (150)
      }
    }

    it("should, if an alternate implicit Timeout is provided, invoke an always-failing by-name by at least the specified timeout") {

      implicit val patienceConfig = PatienceConfig(timeout = Span(1500, Millis))

      var startTime: Option[Long] = None
      recoverToSucceededIf[TestFailedException]{
        ultimately {
          Future { 
            if (startTime.isEmpty)
              startTime = Some(System.currentTimeMillis)
            1 + 1 should equal (3)
          }
        }
      } map { _ =>
        (System.currentTimeMillis - startTime.get).toInt should be >= (1500)
      }
    }

    it("should, if an alternate explicit timeout is provided, invoke an always-failing by-name by at least the specified timeout") {

      var startTime: Option[Long] = None
      recoverToSucceededIf[TestFailedException] {
        ultimately (timeout(Span(1250, Millis))) {
          Future {
            if (startTime.isEmpty)
              startTime = Some(System.currentTimeMillis)
            1 + 1 should equal (3)
          } 
        } 
      } map { _ =>
        (System.currentTimeMillis - startTime.get).toInt should be >= (1250)
      }
    }

    it("should, if an alternate explicit timeout is provided along with an explicit interval, invoke an always-failing by-name by at least the specified timeout, even if a different implicit is provided") {

      implicit val patienceConfig = PatienceConfig(timeout = Span(500, Millis), interval = Span(2, Millis))
      
      var startTime: Option[Long] = None
      recoverToSucceededIf[TestFailedException] {
        ultimately (timeout(Span(1388, Millis)), interval(Span(1, Millisecond))) {
          Future {
            if (startTime.isEmpty)
              startTime = Some(System.currentTimeMillis)
            1 + 1 should equal (3)
          } 
        } 
      } map { _ =>
        (System.currentTimeMillis - startTime.get).toInt should be >= (1388)
      }
    }
    
    ignore("should allow errors that do not normally cause a test to fail through immediately when thrown") {

      // This test won't work because that VME is thrown later when the execution context job queue is worked on
      var count = 0
      assertThrows[VirtualMachineError] {
        ultimately {
          Future {
            count += 1
            throw new VirtualMachineError {}
            1 + 1 should equal (3)
          }
        }
      }
      count should equal (1)
    }
    
    it("should allow TestPendingException, which does not normally cause a test to fail, through immediately when thrown") {

      var count = 0
      recoverToSucceededIf[TestPendingException] {
        ultimately {
          Future {
            count += 1
            pending
          }
        }
      } map { _ =>
        count should equal (1)
      }
    }

    it("should, when reach before first interval, wake up every 1/10 of the interval.") {
      var count = 0
      var startTime: Option[Long] = None
      recoverToSucceededIf[TestFailedException] {
        ultimately(timeout(Span(1000, Millis)), interval(Span(100, Millis))) {
          Future {
            if (startTime.isEmpty) {
              startTime = Some(System.nanoTime)
              count += 1
            }
            else {
              val durationMillis = (System.nanoTime - startTime.get) / 1000000
              if (durationMillis < 100)
                count += 1
            }
            1 + 1 should equal (3)
          }
        }
      } map { _ =>
        count should be > (1)
      }
    }

// SKIP-SCALATESTJS,NATIVE-START
    // TODO: This is failing (on the JVM) and I'm not sure why. Figure it out.
    ignore("should blow up with a TFE that is serializable") {
      recoverToExceptionIf[TestFailedException] {
        ultimately {
          Future { 1 should equal (2) }
        }
      } map { e =>
        serializeRoundtrip(e)
        succeed
      }
    }
// SKIP-SCALATESTJS,NATIVE-END
  }
}

