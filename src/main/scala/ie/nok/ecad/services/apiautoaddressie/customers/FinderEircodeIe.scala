package ie.nok.ecad.services.apiautoaddressie.customers

import ie.nok.http.Client
import java.net.URLEncoder
import zio.ZIO
import zio.json.{JsonDecoder, DeriveJsonDecoder}
import zio.http.{Client => ZioClient}
import ie.nok.ecad.services.apiautoaddressie.customers.Customer

object FinderEircodeIe extends Customer {
  private case class ResponseGetApiKey(key: String)
  private given JsonDecoder[ResponseGetApiKey] =
    DeriveJsonDecoder.gen[ResponseGetApiKey]

  override def getApiKey: ZIO[ZioClient, Throwable, String] = {
    val url = "https://api-finder.eircode.ie/Latest/findergetidentity"
    Client
      .requestBodyAsJson[ResponseGetApiKey](url)
      .map { _.key }
  }

  override def getFindAddressUrl(key: String, address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://api-finder.eircode.ie/Latest/finderfindaddress?key=$key&address=$urlEncodedAddress"
  }

  override def getEcadDataUrl(key: String, addressId: String): String =
    s"https://api-finder.eircode.ie/Latest/findergetecaddata?key=$key&addressId=$addressId"

}
