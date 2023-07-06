package ie.nok.ecad.stores

import ie.nok.ecad.EircodeAddressDatabaseData
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import zio.{ZIO, ZLayer}
import ie.nok.ecad.stores.EircodeAddressDatabaseDataStore

object EircodeAddressDatabaseDataStoreImpl {

  val live: ZLayer[
    EircodeAddressDatabaseDataService,
    Throwable,
    EircodeAddressDatabaseDataStore
  ] =
    ZLayer.fromFunction(new EircodeAddressDatabaseDataStoreImpl(_))

}

class EircodeAddressDatabaseDataStoreImpl(
    finderEircodeIe: EircodeAddressDatabaseDataService
) extends EircodeAddressDatabaseDataStore {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, Option[EircodeAddressDatabaseData]] =
    finderEircodeIe.getEircodeAddressDatabaseData(address).map { _.headOption }
}
