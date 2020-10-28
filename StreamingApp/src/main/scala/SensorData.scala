import play.api.libs.json.{Format, Json}

case class SensorData(humidity: Double,
                      radiation_level: Int,
                      ambient_temperature: Double,
                      timestamp: Int,
                      photosensor: Double,
                      sensor_uuid: String) {

  override def toString: String = s"Sensor ID: $sensor_uuid -> " +
    s"ambient_temperature: $ambient_temperature "
//    s"humidity: $humidity " +
//    s"radiation_level: $radiation_level " +
//    s"timestamp: $timestamp " +
//    s"photosensor: $photosensor"


}

object SensorData {
  implicit val format: Format[SensorData] = Json.format
}