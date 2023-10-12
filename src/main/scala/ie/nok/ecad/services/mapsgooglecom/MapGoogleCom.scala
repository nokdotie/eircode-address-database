package ie.nok.ecad.services.mapsgooglecom

import com.google.maps.{GeoApiContext, GeocodingApi, GeolocationApi}
import com.google.maps.model.{AddressComponentType, ComponentFilter}
import ie.nok.ecad.services.EircodeAddressDatabaseDataService
import ie.nok.ecad.{Eircode, EircodeAddressDatabaseData, Coordinates}
import scala.util.chaining.scalaUtilChainingOps
import zio.{Scope, System, ZIO, ZLayer}

object MapsGoogleCom {

  def live: ZLayer[Scope, Throwable, MapsGoogleCom] =
    System
      .env("GOOGLE_MAPS_API_KEY")
      .someOrFail(
        // https://console.cloud.google.com/apis/credentials/key/5aada99d-3a47-45c2-93be-ba6f9cd34bc1?project=deed-ie
        new Exception("Environment variable not set: GOOGLE_MAPS_API_KEY")
      )
      .map { apiKey =>
        ZIO.attempt {
          GeoApiContext
            .Builder()
            .apiKey(apiKey)
            .build()
        }
      }
      .flatMap { ZIO.fromAutoCloseable(_) }
      .map { new MapsGoogleCom(_) }
      .pipe { ZLayer.fromZIO }

}

class MapsGoogleCom(context: GeoApiContext)
    extends EircodeAddressDatabaseDataService {

  override def getEircodeAddressDatabaseData(
      address: String
  ): ZIO[Any, Throwable, List[EircodeAddressDatabaseData]] =
    GeocodingApi
      .newRequest(context)
      .address(address)
      .region("IE")
      .pipe { req => ZIO.attemptBlocking { req.await() } }
      .map { results =>
        results.toList
          .distinctBy { _.formattedAddress }
          .map { result =>
            EircodeAddressDatabaseData(
              eircode = Eircode.findFirstIn(result.formattedAddress),
              address = result.formattedAddress.split(", ").dropRight(1).toList,
              Coordinates(
                latitude = result.geometry.location.lat,
                longitude = result.geometry.location.lng
              )
            )
          }
      }

}
