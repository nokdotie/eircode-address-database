package ie.nok.ecad.services.findereircodeie

import ie.nok.http.Client
import ie.nok.ecad.{EircodeAddressDatabaseData, Coordinates}
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import ie.nok.ecad.services.findereircodeie.{
  FindAddress,
  GetEcadData,
  GetIdentity
}
import scala.util.chaining.scalaUtilChainingOps
import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, DecoderOps}
import zio.http.{Client => ZioClient}

object FinderEircodeIe {
  val live: ZLayer[ZioClient, Throwable, FinderEircodeIe] =
    ZLayer.fromFunction(new FinderEircodeIe(_))
}

class FinderEircodeIe(client: ZioClient)
    extends EircodeAddressDatabaseDataService {

  private def requestBodyAsJson[A: JsonDecoder](
      url: String
  ): ZIO[Any, Throwable, A] =
    Client
      .requestBodyAsJson(url)
      .provide(ZLayer.succeed(client))

  override def getEircodeAddressDatabaseData(
      eircodeOrAddress: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] = for {
    getIdentity <- GetIdentity.url
      .pipe { requestBodyAsJson[GetIdentity.Response] }
    findAddress <- FindAddress
      .url(getIdentity.key, eircodeOrAddress)
      .pipe { requestBodyAsJson[FindAddress.Response] }
    addressIds = findAddress
      .pipe { case FindAddress.Response(addressId, addressType, options) =>
        FindAddress.ResponseOption(addressId, addressType) +: options
      }
      .collect {
        case FindAddress.ResponseOption(Some(addressId), Some(addressType))
            if addressType.text == "ResidentialAddressPoint" =>
          addressId
      }
    getEcadData <- addressIds
      .map {
        GetEcadData
          .url(getIdentity.key, _)
      }
      .map { requestBodyAsJson[GetEcadData.Response] }
      .pipe { ZIO.collectAll }

    res = getEcadData.flatMap { data =>
      val eircode = data.eircodeInfo.eircode.patch(3, " ", 0)
      val coordinates = data.spatialInfo.etrs89.location
        .pipe { location =>
          Coordinates(
            longitude = location.longitude,
            latitude = location.latitude
          )
        }

      List(
        EircodeAddressDatabaseData(
          eircode = eircode,
          address = data.postalAddress.english :+ eircode,
          coordinates = coordinates
        ),
        EircodeAddressDatabaseData(
          eircode = eircode,
          address = data.postalAddress.english :+ eircode,
          coordinates = coordinates
        )
      )
    }
  } yield res
}
