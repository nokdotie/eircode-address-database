package ie.nok.ecad.stores

import ie.nok.ecad.services.apiautoaddressie.ApiAutoAddressIe
import ie.nok.ecad.services.dominosie.DominosIe
import ie.nok.ecad.services.mapsgooglecom.MapsGoogleCom
import ie.nok.ecad.services.toolshousinggovie.ToolsHousingGovIe
import ie.nok.zio.ZIO
import scala.util.chaining.scalaUtilChainingOps
import zio.Scope
import zio.http.Client

class EircodeAddressDatabaseDataStoreImplSuite extends munit.FunSuite {
  test("Retrieve EircodeAddressDatabaseData from api.autoaddress.ie") {
    val result = EircodeAddressDatabaseDataStore
      .getEircodeAddressDatabaseData("R95W7X4")
      .provide(
        Client.default,
        ApiAutoAddressIe.live,
        EircodeAddressDatabaseDataStoreImpl.live
      )
      .pipe { ZIO.unsafeRun(_) }
      .getOrElse { reason => fail(s"Unsafe run failed: $reason") }

    assert(result.nonEmpty, "No addresses found")
  }

  // Ignored because dominos.ie blocks GitHub Actions
  test("Retrieve EircodeAddressDatabaseData from dominos.ie".ignore) {
    val result = EircodeAddressDatabaseDataStore
      .getEircodeAddressDatabaseData("R95W7X4")
      .provide(
        Client.default,
        DominosIe.live,
        EircodeAddressDatabaseDataStoreImpl.live
      )
      .pipe { ZIO.unsafeRun(_) }
      .getOrElse { reason => fail(s"Unsafe run failed: $reason") }

    assert(result.nonEmpty, "No addresses found")
  }

  // Ignored because maps.google.com requires an API key
  test("Retrieve EircodeAddressDatabaseData from maps.google.com".ignore) {
    val result = EircodeAddressDatabaseDataStore
      .getEircodeAddressDatabaseData("R95W7X4")
      .provide(
        Scope.default,
        MapsGoogleCom.live,
        EircodeAddressDatabaseDataStoreImpl.live
      )
      .pipe { ZIO.unsafeRun(_) }
      .getOrElse { reason => fail(s"Unsafe run failed: $reason") }

    assert(result.nonEmpty, "No addresses found")
  }

  test("Retrieve EircodeAddressDatabaseData from tools.housing.gov.ie") {
    val result = EircodeAddressDatabaseDataStore
      .getEircodeAddressDatabaseData("R95W7X4")
      .provide(
        Client.default,
        ToolsHousingGovIe.live,
        EircodeAddressDatabaseDataStoreImpl.live
      )
      .pipe { ZIO.unsafeRun(_) }
      .getOrElse { reason => fail(s"Unsafe run failed: $reason") }

    assert(result.nonEmpty, "No addresses found")
  }
}
