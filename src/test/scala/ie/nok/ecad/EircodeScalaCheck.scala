package ie.nok.ecad

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

private val genEircode: Gen[Eircode] = arbitrary[String].map { Eircode.apply }

given Arbitrary[Eircode] = Arbitrary(genEircode)
