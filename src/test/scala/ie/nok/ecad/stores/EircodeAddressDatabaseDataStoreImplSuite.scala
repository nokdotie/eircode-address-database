package ie.nok.ecad.stores

import ie.nok.ecad.services.EircodeAddressDatabaseDataServiceImpl
import ie.nok.ecad.services.apiautoaddressie.ApiAutoAddressIe
import ie.nok.ecad.services.dominosie.DominosIe
import ie.nok.ecad.services.mapsgooglecom.MapsGoogleCom
import ie.nok.ecad.services.toolshousinggovie.ToolsHousingGovIe
import ie.nok.zio.ZIO
import scala.util.chaining.scalaUtilChainingOps
import zio.Scope
import zio.http.Client

class EircodeAddressDatabaseDataStoreImplSuite extends munit.FunSuite {
  test("Retrieve EircodeAddressDatabaseData from a store") {
    val result = EircodeAddressDatabaseDataStore
      .getEircodeAddressDatabaseData("R95W7X4")
      .provide(
        Scope.default,
        Client.default,
        EircodeAddressDatabaseDataServiceImpl.live,
        EircodeAddressDatabaseDataStoreImpl.live
      )
      .pipe { ZIO.unsafeRun(_) }
      .getOrElse { reason => fail(s"Unsafe run failed: $reason") }

    assert(result.nonEmpty, "No addresses found")
  }
}
