package ie.nok.ecad

case class EircodeAddressDatabaseData(
    eircode: Option[Eircode],
    address: List[String],
    coordinates: Coordinates
)

case class Coordinates(
    latitude: BigDecimal,
    longitude: BigDecimal
)
