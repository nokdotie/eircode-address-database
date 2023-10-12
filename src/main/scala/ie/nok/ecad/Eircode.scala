package ie.nok.ecad

import scala.util.chaining.scalaUtilChainingOps

case class Eircode(value: String) extends AnyVal
object Eircode {
  // https://autoaddress2.helpscoutdocs.com/article/94-general-do-you-have-a-regex-for-eircode
  val regex =
    """\b(?:(a(4[125s]|6[37]|7[5s]|[8b][1-6s]|9[12468b])|c1[5s]|d([0o][1-9sb]|1[0-8osb]|2[024o]|6w)|e(2[15s]|3[24]|4[15s]|[5s]3|91)|f(12|2[368b]|3[15s]|4[25s]|[5s][26]|9[1-4])|h(1[2468b]|23|[5s][34]|6[25s]|[79]1)|k(3[246]|4[5s]|[5s]6|67|7[8b])|n(3[79]|[49]1)|p(1[247]|2[45s]|3[126]|4[37]|[5s][16]|6[17]|7[25s]|[8b][15s])|r(14|21|3[25s]|4[25s]|[5s][16]|9[35s])|t(12|23|34|4[5s]|[5s]6)|v(1[45s]|23|3[15s]|42|9[2-5s])|w(12|23|34|91)|x(3[5s]|42|91)|y(14|2[15s]|3[45s]))\s?[abcdefhknoprtsvwxy\d]{4})\b""".pipe {
      r => s"(?i)${r}"
    }.r

  private def search(address: String): Option[String] =
    regex
      .findFirstMatchIn(address)
      .map { _.group(0) }

  private def sanitize(eircode: String): String =
    eircode
      .toUpperCase()
      .replaceAll(" ", "")
      .replaceAll("O", "0")

  def findFirstIn(address: String): Option[Eircode] =
    search(address)
      .map(sanitize)
      .map(Eircode(_))

  def unzip(address: String): (String, Option[Eircode]) = {
    val eircode = search(address)
    val addressWithoutEircode = eircode.fold(address) { eircode =>
      address.replace(eircode, "").replaceAll("[\\s,]+$", "")
    }

    (addressWithoutEircode, eircode.map(sanitize).map(Eircode(_)))
  }
}
