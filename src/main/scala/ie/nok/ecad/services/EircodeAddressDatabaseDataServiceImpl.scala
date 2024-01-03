package ie.nok.ecad.services

import ie.nok.ecad.services.apiautoaddressie.ApiAutoAddressIe
import ie.nok.ecad.services.dominosie.DominosIe
import ie.nok.ecad.services.mapsgooglecom.MapsGoogleCom
import ie.nok.ecad.services.toolshousinggovie.ToolsHousingGovIe
import ie.nok.ecad.{Eircode, EircodeAddressDatabaseData}
import zio.http.Client
import zio.{Scope, ZEnvironment, ZIO, ZLayer}

import scala.util.chaining.scalaUtilChainingOps

object EircodeAddressDatabaseDataServiceImpl {

  val live: ZLayer[
    Scope & Client,
    Throwable,
    EircodeAddressDatabaseDataService
  ] =
    List(
      ApiAutoAddressIe.live,
      DominosIe.live,
      MapsGoogleCom.live,
      ToolsHousingGovIe.live
    )
      .pipe { ZLayer.collectAll(_) }
      .andTo {
        ZLayer.fromFunction { EircodeAddressDatabaseDataServiceImpl(_) }
      }

}

class EircodeAddressDatabaseDataServiceImpl(
    services: List[EircodeAddressDatabaseDataService]
) extends EircodeAddressDatabaseDataService {

  override def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] = {
    val addresses = List(
      Option(address),
      Eircode.findFirstIn(address).map { _.value }
    ).flatten

    val serviceAddressTuple = services.flatMap { service =>
      addresses.map { address => (service, address) }
    }

    serviceAddressTuple
      .map { (service, address) =>
        service.getEircodeAddressDatabaseData(address)
      }
      .pipe { ZIO.collectAll }
      .map { _.flatten }
  }

}
