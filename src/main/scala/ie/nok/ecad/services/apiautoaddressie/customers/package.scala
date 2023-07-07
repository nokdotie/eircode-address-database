package ie.nok.ecad.services.apiautoaddressie.customers

import zio.ZIO
import zio.http.Client

trait Customer {
  def getApiKey: ZIO[Client, Throwable, String]

  def getFindAddressUrl(key: String, address: String): String
  def getEcadDataUrl(key: String, addressId: String): String
}
