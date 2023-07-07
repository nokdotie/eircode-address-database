package ie.nok.ecad.services.apiautoaddressie

import ie.nok.http.Client
import ie.nok.ecad.{EircodeAddressDatabaseData, Coordinates}
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import ie.nok.ecad.services.apiautoaddressie.customers._
import scala.util.chaining.scalaUtilChainingOps
import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, DecoderOps}
import zio.http.{Client => ZioClient}
import ie.nok.ecad.services.apiautoaddressie.customers.Customer

object ApiAutoAddressIe {
  val live: ZLayer[ZioClient, Throwable, ApiAutoAddressIe] =
    ZLayer.fromFunction(
      new ApiAutoAddressIe(_, List(AnPostCom, FinderEircodeIe))
    )
}

class ApiAutoAddressIe(client: ZioClient, customers: List[Customer])
    extends EircodeAddressDatabaseDataService {

  private def requestBodyAsJson[A: JsonDecoder](
      url: String
  ): ZIO[ZioClient, Throwable, A] =
    Client
      .requestBodyAsJson(url)

  private def getEircodeAddressDatabaseData(
      customer: Customer,
      address: String
  ): ZIO[ZioClient, Throwable, List[EircodeAddressDatabaseData]] = for {
    apiKey <- customer.getApiKey
    findAddressResponse <- customer
      .getFindAddressUrl(apiKey, address)
      .pipe { requestBodyAsJson[FindAddress.Response] }
    addressIds = findAddressResponse
      .pipe { case FindAddress.Response(addressId, addressType, options) =>
        FindAddress.ResponseOption(addressId, addressType) +: options
      }
      .collect {
        case FindAddress.ResponseOption(Some(addressId), Some(addressType))
            if addressType.text == "ResidentialAddressPoint" =>
          addressId
      }
    getEcadDataResponse <- addressIds
      .map { customer.getEcadDataUrl(apiKey, _) }
      .map { requestBodyAsJson[GetEcadData.Response] }
      .pipe { ZIO.collectAll }

    res = getEcadDataResponse.flatMap { data =>
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

  override def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] =
    customers
      .map { customer => getEircodeAddressDatabaseData(customer, address) }
      .pipe {
        case Nil          => ZIO.fail(new Exception("No customers"))
        case head :: tail => ZIO.firstSuccessOf(head, tail)
      }
      .provide(ZLayer.succeed(client))

}
