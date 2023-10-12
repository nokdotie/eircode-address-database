package ie.nok.ecad.services.dominosie

import ie.nok.http.Client.requestBodyAsJson
import ie.nok.ecad.{Eircode, EircodeAddressDatabaseData, Coordinates}
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import java.net.URLEncoder
import scala.util.chaining.scalaUtilChainingOps
import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, DeriveJsonDecoder}
import zio.http.Client

object DominosIe {

  def live: ZLayer[Client, Throwable, DominosIe] =
    ZLayer.fromFunction { DominosIe(_) }

}

class DominosIe(client: Client) extends EircodeAddressDatabaseDataService {

  private case class Response(
      data: ResponseData
  )
  private given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  private case class ResponseData(
      items: List[ResponseDataItem]
  )
  private given JsonDecoder[ResponseData] = DeriveJsonDecoder.gen[ResponseData]

  private case class ResponseDataItem(
      buildingName: String,
      buildingNumber: String,
      streetAddress: String,
      localityName: String,
      postCode: String,
      coordinates: Option[ResponseDataItemCoordinates]
  )
  private given JsonDecoder[ResponseDataItem] =
    DeriveJsonDecoder.gen[ResponseDataItem]

  private case class ResponseDataItemCoordinates(
      latitude: BigDecimal,
      longitude: BigDecimal
  )
  private given JsonDecoder[ResponseDataItemCoordinates] =
    DeriveJsonDecoder.gen[ResponseDataItemCoordinates]

  private def url(address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://www.dominos.ie/api/location/v1/search?searchText=$urlEncodedAddress"
  }

  private def getAddress(item: ResponseDataItem): List[String] =
    List(
      item.buildingName,
      item.buildingNumber,
      item.streetAddress,
      item.localityName,
      item.postCode
    ).map { _.trim }.filter { _.nonEmpty }

  private def getCoordinates(item: ResponseDataItem): Option[Coordinates] =
    item.coordinates.map { coordinates =>
      Coordinates(
        latitude = coordinates.latitude,
        longitude = coordinates.longitude
      )
    }

  override def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] =
    requestBodyAsJson[Response](url(address))
      .provide(ZLayer.succeed(client))
      .flatMap {
        _.data.items
          .map {
            case item if item.coordinates.isEmpty =>
              getEircodeAddressDatabaseData(item.postCode)
            case item =>
              EircodeAddressDatabaseData(
                eircode = Eircode.findFirstIn(item.postCode),
                address = getAddress(item),
                coordinates = getCoordinates(item).getOrElse { ??? }
              )
                .pipe { List(_) }
                .pipe { ZIO.succeed }
          }
          .pipe { ZIO.collectAll }
          .map { _.flatten }

      }
}
