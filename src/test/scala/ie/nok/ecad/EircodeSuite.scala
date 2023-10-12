package ie.nok.ecad

import scala.util.chaining.scalaUtilChainingOps

class EircodeSuite extends munit.FunSuite {
  def testEircode(address: String, expected: Option[String]) =
    address
      .pipe { Eircode.findFirstIn }
      .map { _.value }
      .pipe { eircode => assert(eircode == expected) }

  test("Eircode.findFirstIn returns None if no valid Eircode is present") {
    testEircode("12 THE GREEN, AYRESFIELDS, KILKENNY", None)
  }

  test("Eircode.findFirstIn returns only the Eircode") {
    testEircode(
      "12 THE GREEN, AYRESFIELDS, KILKENNY, R95 W7X4",
      Some("R95W7X4")
    )
  }

  test("Eircode.findFirstIn uppercases the Eircode") {
    testEircode("r95w7x4", Some("R95W7X4"))
  }

  test("Eircode.findFirstIn removes the space") {
    testEircode("R95 W7X4", Some("R95W7X4"))
  }

  test("Eircode.findFirstIn replaces O with 0") {
    testEircode("R95W7XO", Some("R95W7X0"))
    testEircode("R95W7Xo", Some("R95W7X0"))
  }

  test("Eircode.unzip returns address and eircode") {
    "12 THE GREEN, AYRESFIELDS, KILKENNY, R95 W7X4"
      .pipe { Eircode.unzip }
      .pipe { (address, eircode) =>
        assert(address == "12 THE GREEN, AYRESFIELDS, KILKENNY")
        assert(eircode == Some(Eircode("R95W7X4")))
      }
  }
}
