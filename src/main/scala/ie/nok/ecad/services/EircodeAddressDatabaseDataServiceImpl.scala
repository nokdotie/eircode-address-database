package ie.nok.ecad.services

import ie.nok.ecad.{Eircode, EircodeAddressDatabaseData}
import ie.nok.ecad.services.findereircodeie.FinderEircodeIe
import ie.nok.ecad.services.mapsgooglecom.MapsGoogleCom
import ie.nok.ecad.services.toolshousinggovie.ToolsHousingGovIe
import scala.util.chaining.scalaUtilChainingOps
import zio.{ZIO, ZLayer}

object EircodeAddressDatabaseDataServiceImpl {

  val live: ZLayer[
    FinderEircodeIe & MapsGoogleCom & ToolsHousingGovIe,
    Throwable,
    EircodeAddressDatabaseDataService
  ] =
    ZLayer.fromFunction {
      (
          finderEircodeIe: FinderEircodeIe,
          mapsGoogleCom: MapsGoogleCom,
          toolsHousingGovIe: ToolsHousingGovIe
      ) =>
        List(finderEircodeIe, mapsGoogleCom, toolsHousingGovIe)
          .pipe { new EircodeAddressDatabaseDataServiceImpl(_) }
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
      Eircode.findFirstIn(address)
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
      .tap { list =>
        println(s"ADDRESS: $address")
        list.foreach { element =>
          println(s" - ${element.address.mkString(", ")}")
        }
        println("")

        ZIO.unit
      }
  }

}
