package ie.nok.ecad.services.findereircodeie

import ie.nok.ecad.{EircodeAddressDatabaseData, Coordinates}
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import ie.nok.ecad.services.findereircodeie.{
  FindAddress,
  GetEcadData,
  GetIdentity
}
import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, DecoderOps}
import zio.http.Client
import scala.util.chaining.scalaUtilChainingOps
import ie.nok.ecad.services.EircodeAddressDatabaseDataService

object FinderEircodeIe {
  val live: ZLayer[Client, Throwable, FinderEircodeIe] =
    ZLayer.fromFunction(new FinderEircodeIe(_))
}

class FinderEircodeIe(client: Client)
    extends EircodeAddressDatabaseDataService {
  private def request[A: JsonDecoder](url: String): ZIO[Any, Throwable, A] =
    Client
      .request(url)
      .provide(ZLayer.succeed(client))
      .flatMap { _.body.asString }
      .flatMap { body =>
        body
          .fromJson[A]
          .left
          .map { err => Throwable(s"$err: $body") }
          .pipe(ZIO.fromEither)
      }

  override def getEircodeAddressDatabaseData(
      eircodeOrAddress: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] = for {
    getIdentity <- GetIdentity.url
      .pipe(request[GetIdentity.Response])
    findAddress <- FindAddress
      .url(getIdentity.key, eircodeOrAddress)
      .pipe(request[FindAddress.Response])
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
      .map { request[GetEcadData.Response] }
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
