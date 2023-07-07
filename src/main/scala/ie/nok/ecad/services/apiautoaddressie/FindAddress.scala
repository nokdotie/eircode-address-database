package ie.nok.ecad.services.apiautoaddressie

import zio.json.{DeriveJsonDecoder, JsonDecoder}

protected[apiautoaddressie] object FindAddress {
  case class AddressId(value: String)
  given JsonDecoder[AddressId] =
    JsonDecoder.string
      .widen[Any]
      .orElse(JsonDecoder.int.widen[Any])
      .map { any => AddressId(any.toString) }

  case class Response(
      addressId: Option[AddressId],
      addressType: Option[ResponseAddressType],
      options: List[ResponseOption]
  )
  given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  case class ResponseAddressType(
      text: String
  )
  given JsonDecoder[ResponseAddressType] =
    DeriveJsonDecoder.gen[ResponseAddressType]

  case class ResponseOption(
      addressId: Option[AddressId],
      addressType: Option[ResponseAddressType]
  )
  given JsonDecoder[ResponseOption] = DeriveJsonDecoder.gen[ResponseOption]

}
