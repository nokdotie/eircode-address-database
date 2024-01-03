package ie.nok.ecad.services

import ie.nok.ecad.EircodeAddressDatabaseData
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import zio.ZIO

trait EircodeAddressDatabaseDataService {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]]
}

object EircodeAddressDatabaseDataService {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[EircodeAddressDatabaseDataService, Throwable, List[
    EircodeAddressDatabaseData
  ]] =
    ZIO.serviceWithZIO(_.getEircodeAddressDatabaseData(address))
}
