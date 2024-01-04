package ie.nok.ecad.stores

import ie.nok.ecad.EircodeAddressDatabaseData
import ie.nok.ecad.stores.EircodeAddressDatabaseDataStore
import zio.ZIO

trait EircodeAddressDatabaseDataStore {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, Option[EircodeAddressDatabaseData]]
}

object EircodeAddressDatabaseDataStore {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[EircodeAddressDatabaseDataStore, Throwable, Option[
    EircodeAddressDatabaseData
  ]] =
    ZIO.serviceWithZIO[EircodeAddressDatabaseDataStore](
      _.getEircodeAddressDatabaseData(address)
    )
}
