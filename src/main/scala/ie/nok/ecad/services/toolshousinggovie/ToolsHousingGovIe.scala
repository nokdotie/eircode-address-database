package ie.nok.ecad.services.toolshousinggovie

import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import ie.nok.ecad.{Coordinates, Eircode, EircodeAddressDatabaseData}
import ie.nok.http.Client
import zio.http.Client as ZioClient
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.{ZIO, ZLayer}

import java.net.URLEncoder

object ToolsHousingGovIe {

  def live: ZLayer[ZioClient, Throwable, ToolsHousingGovIe] =
    ZLayer.fromFunction { ToolsHousingGovIe(_) }

}

class ToolsHousingGovIe(client: ZioClient) extends EircodeAddressDatabaseDataService {

  private case class Response(
      candidates: List[ResponseCandidate]
  )
  private given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  private case class ResponseCandidate(
      attributes: ResponseCandidateAttributes
  )
  private given JsonDecoder[ResponseCandidate] =
    DeriveJsonDecoder.gen[ResponseCandidate]

  private case class ResponseCandidateAttributes(
      Addr_Line_1: String,
      Addr_Line_2: String,
      Addr_Line_3: String,
      Addr_Line_4: String,
      Addr_Line_5: String,
      Addr_Line_6: String,
      Addr_Line_7: String,
      Addr_Line_8: String,
      Eircode: String,
      Latitude: BigDecimal,
      Longitude: BigDecimal
  )
  private given JsonDecoder[ResponseCandidateAttributes] =
    DeriveJsonDecoder.gen[ResponseCandidateAttributes]

  private def url(address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://tools.housing.gov.ie/arcgis/rest/services/Locator/EircodeLocator/GeocodeServer/findAddressCandidates?outFields=*&f=json&SingleLine=$urlEncodedAddress"
  }

  override def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] =
    Client
      .requestBodyAsJson[Response](url(address))
      .provide(ZLayer.succeed(client))
      .map {
        _.candidates.map { candidate =>
          EircodeAddressDatabaseData(
            eircode = Eircode.findFirstIn(candidate.attributes.Eircode),
            address = List(
              candidate.attributes.Addr_Line_1,
              candidate.attributes.Addr_Line_2,
              candidate.attributes.Addr_Line_3,
              candidate.attributes.Addr_Line_4,
              candidate.attributes.Addr_Line_5,
              candidate.attributes.Addr_Line_6,
              candidate.attributes.Addr_Line_7,
              candidate.attributes.Addr_Line_8
            ).map { _.trim }.filter { _.nonEmpty },
            Coordinates(
              latitude = candidate.attributes.Latitude,
              longitude = candidate.attributes.Longitude
            )
          )
        }
      }

}
