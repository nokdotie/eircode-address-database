package ie.nok.ecad.services.findereircodeie

import ie.nok.ecad.EircodeAddressDatabaseData
import zio.ZIO

trait FinderEircodeIe {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, EircodeAddressDatabaseData]
}

object FinderEircodeIe {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[FinderEircodeIe, Throwable, EircodeAddressDatabaseData] =
    ZIO.serviceWithZIO(_.getEircodeAddressDatabaseData(address))
}
