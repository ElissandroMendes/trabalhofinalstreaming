import java.util.Properties
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import play.api.libs.json.Json

object KafkaReading {
  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "test")

    // Obtendo o ambiente de execução para o Flink
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI()

    // Seta um consumer utilizando um conector para o Apache Kafka
    // Lemos o tópico sensro-data, onde teremos os dados dos sensores
    val sensorDataConsumer = new FlinkKafkaConsumer[String]("sensor-data", new SimpleStringSchema(), properties)
    sensorDataConsumer.setStartFromLatest()

    // Seta consumer no ambiente de execução
    // As informações estão no formato texto, porém, representam um JSON, então realizamos o parse usando Play Json
    val stream = env
      .addSource(sensorDataConsumer)
      .map(Json.parse(_).as[SensorData])

    // Aqui criamos uma keyedStream e uma janela temporal de 10 segundos
    val streamBySensor = stream
      .keyBy(sensor => sensor.sensor_uuid)
      .timeWindow(Time.seconds(10))

    // Aqui aplicamos uma operação de reduce para calcular a temperatura máxima presente nos dados da janela
    // Note-se também o filtro aplicado; limitamos os dados para melhor acompanhamento dos resultados.
    val maxTemperature = streamBySensor
      .reduce(
        (sensor1, sensor2) => if (sensor1.ambient_temperature > sensor2.ambient_temperature) sensor1 else sensor2
      )
      .filter((sensor: SensorData) => sensor.sensor_uuid == "probe-28500df7")

    // Aqui aplicamos uma operação de reduce para calcular a temperatura mínima presente nos dados da janela
    // Note-se também o filtro aplicado; limitamos os dados para melhor acompanhamento dos resultados.
    val minTemperature = streamBySensor
      .reduce(
        (sensor1, sensor2) => if (sensor1.ambient_temperature < sensor2.ambient_temperature) sensor1 else sensor2
      )
      .filter((sensor: SensorData) => sensor.sensor_uuid == "probe-28500df7")

    // Aqui aplicamos uma operação de agregação.
    // Usamos uma classe que estende AggregateFunction.
    // #see AvgTemp
    val avgTemperature = streamBySensor
      .aggregate(new AvgTemp).filter(sensor => sensor._1 == "probe-28500df7")

    // Por fim usamos como sink das computações um simples print().
    // Note o uso da versão overloading do método print() que nos permite nomear o que será escrito no console.
    maxTemperature.print("Temp max")
    minTemperature.print("Temp min")
    avgTemperature.print("Temp média")

/*
    val streamTemp = stream
      .addSource(sensorDataConsumer)
      .map(Json.parse(_).as[SensorData])
      .map(sensor => (sensor.sensor_uuid, sensor.ambient_temperature))

    val tempBySensor = streamTemp
      .keyBy(0)
      .timeWindow(Time.seconds(10))

    val maxTemperature = tempBySensor.reduce((s1, s2) => (s1._1, s1._2.max(s2._2)))
    val minTemperature = tempBySensor.reduce((s1, s2) => (s1._1, s1._2.min(s2._2)))
*/
    env.execute("Lendo dados de sensores via Kafka topic")
  }
}
