package ie.nok.ecad

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

private val genEircode: Gen[Eircode] = arbitrary[String].map { Eircode.apply }

given Arbitrary[Eircode] = Arbitrary(genEircode)
