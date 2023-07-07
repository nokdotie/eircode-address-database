package ie.nok.ecad.services.apiautoaddressie.customers

import ie.nok.http.Client
import java.net.URLEncoder
import zio.ZIO
import zio.http.{Client => ZioClient}
import ie.nok.ecad.services.apiautoaddressie.customers.Customer

object QuoteZurichIe extends Customer {
  override def getApiKey: ZIO[ZioClient, Throwable, String] = {
    val url =
      "https://quote.zurich.ie/resources/definition/generic/Resources/en/us/custom__1677858767000.js"
    val apiKeyRegex =
      """key: "([0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12})"""".r

    Client
      .requestBody(url)
      .map { apiKeyRegex.findFirstMatchIn }
      .someOrFail(new Exception("API key not found"))
      .map { _.group(1) }
  }

  override def getFindAddressUrl(key: String, address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://api.autoaddress.ie/2.0/findaddress?key=$key&address=$urlEncodedAddress"
  }

  override def getEcadDataUrl(key: String, addressId: String): String =
    s"https://api.autoaddress.ie/2.0/GetEcadData?key=$key&ecadid=$addressId"

}
