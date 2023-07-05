package ie.nok.ecad

case class EircodeAddressDatabaseData(
    eircode: String,
    address: List[String],
    coordinates: Coordinates
)

case class Coordinates(
    latitude: BigDecimal,
    longitude: BigDecimal
)
