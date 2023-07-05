package ie.nok.ecad.stores

import ie.nok.ecad.EircodeAddressDatabaseData
import ie.nok.ecad.services.findereircodeie.FinderEircodeIe
import zio.{ZIO, ZLayer}

object EircodeAddressDatabaseDataStoreImpl {

  val live
      : ZLayer[FinderEircodeIe, Throwable, EircodeAddressDatabaseDataStore] =
    ZLayer.fromFunction(new EircodeAddressDatabaseDataStoreImpl(_))

}

class EircodeAddressDatabaseDataStoreImpl(finderEircodeIe: FinderEircodeIe)
    extends EircodeAddressDatabaseDataStore {
  def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, Option[EircodeAddressDatabaseData]] =
    finderEircodeIe.getEircodeAddressDatabaseData(address).asSome
}
