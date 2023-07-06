package ie.nok.ecad

object Eircode {
  // https://autoaddress2.helpscoutdocs.com/article/94-general-do-you-have-a-regex-for-eircode
  val regex = """\b([A-Z][0-9]{2}|D6W) ?([0-9A-Z]{4})\b""".r

  def findFirstIn(address: String): Option[String] = regex
    .findFirstMatchIn(address)
    .map { first => s"${first.group(1)} ${first.group(2)}" }
}
