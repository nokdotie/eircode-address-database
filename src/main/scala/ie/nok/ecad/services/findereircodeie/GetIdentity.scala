package ie.nok.ecad.services.findereircodeie

import zio.json.{DeriveJsonDecoder, JsonDecoder}

protected[findereircodeie] object GetIdentity {
  case class Response(key: String)
  given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  val url: String = "https://api-finder.eircode.ie/Latest/findergetidentity"
}
