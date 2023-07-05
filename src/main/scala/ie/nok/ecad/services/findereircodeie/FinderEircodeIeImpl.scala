package ie.nok.ecad.services.findereircodeie

import ie.nok.ecad.{EircodeAddressDatabaseData, Coordinates}
import ie.nok.ecad.services.findereircodeie.{
  FindAddress,
  GetEcadData,
  GetIdentity
}
import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, DecoderOps}
import zio.http.Client
import scala.util.chaining.scalaUtilChainingOps

object FinderEircodeIeImpl {
  val live: ZLayer[Client, Throwable, FinderEircodeIe] =
    ZLayer.fromFunction(new FinderEircodeIeImpl(_))
}

class FinderEircodeIeImpl(client: Client) extends FinderEircodeIe {
  private def request[A: JsonDecoder](url: String): ZIO[Any, Throwable, A] =
    client
      .get(url)
      .flatMap { _.body.asString }
      .flatMap { body =>
        body
          .fromJson[A]
          .left
          .map { err => Throwable(s"$err: $body") }
          .pipe(ZIO.fromEither)
      }

  private def getResidentialAddressId(
      response: FindAddress.Response
  ): ZIO[Any, Throwable, String] =
    response
      .pipe { case FindAddress.Response(addressId, addressType, options) =>
        FindAddress.ResponseOption(addressId, addressType) +: options
      }
      .collect {
        case FindAddress.ResponseOption(Some(addressId), Some(addressType))
            if addressType.text == "ResidentialAddressPoint" =>
          addressId
      }
      .pipe {
        case head :: Nil => ZIO.succeed(head)
        case Nil         => ZIO.fail(new Throwable("No addressId found"))
        case many        => ZIO.fail(new Throwable("Too many addressIds found"))
      }

  override def getEircodeAddressDatabaseData(
      eircodeOrAddress: String
  ): ZIO[Any, Throwable, EircodeAddressDatabaseData] = for {
    getIdentity <- GetIdentity.url
      .pipe(request[GetIdentity.Response])
    findAddress <- FindAddress
      .url(getIdentity.key, eircodeOrAddress)
      .pipe(request[FindAddress.Response])
    addressId <- getResidentialAddressId(findAddress)
    getEcadData <- GetEcadData
      .url(getIdentity.key, addressId)
      .pipe(request[GetEcadData.Response])
    res = EircodeAddressDatabaseData(
      eircode = getEcadData.eircodeInfo.eircode,
      address = getEcadData.geographicAddress.english,
      coordinates = getEcadData.spatialInfo.etrs89.location
        .pipe { location =>
          Coordinates(
            longitude = location.longitude,
            latitude = location.latitude
          )
        }
    )
  } yield res
}
