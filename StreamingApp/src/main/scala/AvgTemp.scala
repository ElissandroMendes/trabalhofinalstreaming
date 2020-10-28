import org.apache.flink.api.common.functions.AggregateFunction

/**
 *  Classe que realização agregação dos dados.
 *  Aqui realizamos a acumulação dos valores e quantidade que são usadas no fim para o cáculo da temperatura média.
 **/
class AvgTemp extends AggregateFunction[SensorData, (String, Double, Int), (String, Double)] {
  override def createAccumulator(): (String, Double, Int) = ("", 0.0, 0)
  override def add(value: SensorData, accumulator: (String, Double, Int)): (String, Double, Int) = {
    (value.sensor_uuid, value.ambient_temperature + accumulator._2, accumulator._3 + 1)
  }
  override def merge(a: (String, Double, Int), b: (String, Double, Int)): (String, Double, Int) = {
    (a._1, a._2 + b._2, a._3 + b._3)
  }

  override def getResult(accumulator: (String, Double, Int)): (String, Double) = {
    (accumulator._1, accumulator._2 / accumulator._3)
  }
}