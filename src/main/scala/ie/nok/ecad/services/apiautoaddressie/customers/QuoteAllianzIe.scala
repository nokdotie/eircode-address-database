package ie.nok.ecad.services.apiautoaddressie.customers

import ie.nok.ecad.services.apiautoaddressie.customers.Customer
import ie.nok.http.Client
import zio.ZIO
import zio.http.Client as ZioClient

import java.net.URLEncoder

object QuoteAllianzIe extends Customer {
  override def getApiKey: ZIO[ZioClient, Throwable, String] = {
    val url =
      "https://quote.allianz.ie/familyhomeb2cui/main.e8aa63de211d86a28222.js"
    val apiKeyRegex =
      """gammaKey:m\.gammaKey\|\|"([0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12})"""".r

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
