# Projeto Final

### Processamento e Análise de Streaming de dados

### Professor: Lucas Gaspar

Neste projeto realizamos a ingestão e processamento de dados de sensores em tempo real (simulados).

Utilizamos as tecnologias e serviços abaixo relacionadas:

| Tecnologia / Serviço | Uso                                     | Links                                                      |
| -------------------- | --------------------------------------- | ---------------------------------------------------------- |
| PubNub               | Simulação stream sensores               | <https://www.pubnub.com/developers/realtime-data-streams/> |
| Apache Kafka         | Ingestão de dados                       | <https://kafka.apache.org/>                                |
| Apache Flink         | Processamento do stream                 | <https://flink.apache.org/>                                |
| Python               | Leitura do stream e pré processamento   | <http://www.python.org/>                                   |
| Scala                | Código do processamento no Apache Flink | <https://www.scala-lang.org/>                              |

#### Descrição do projeto

O projeto tem por objetivo demonstrar o consumo de dados de sensores via streaming. Sobre esses dados são realizados processamentos, calculando-se várias métricas. Os dados oriundos dos sensores são inseridos numa instância do **Apache Kafka**. Os dados lá inseridos serão consumidos e processados em uma instância do **Apache Flink**.

Os dados serão lidos usando-se uma janela de tempo (time window) de tamanho fixo de 10 segundos.
Após a coleta pelo Flink, os dados sofrem um processamento de onde obtemos valores máximo, mínimos e médio para um dos valores fornecidos, no caso, a temperatura do ambiente.

Os dados dos sensores são fornecidos pelo serviço de API em tempo real, PubNub, e são entregues em formato JSON:

```javascript
{
 "photosensor": "818.43",
 "ambient_temperature": "29.58",
 "radiation_level": "199",
 "sensor_uuid":
 "probe-2a1a1099",
 "timestamp": 1603906280,
 "humidity": "87.9544"
}
```

Para o presente trabalho usamos apenas os valores referentes a temperatura ambiente: `ambient_temperature`.
Os dados são lidos por uma classe **Python** que faz uso da API fornecida pelo serviço PubNub.
Essa mesma classe também é responsável por inserir os dados na instância do **Apache Kafka**, usando para isso um tópico que chamammos `sensor-data`.
Após a ingestão dos dados no tópico do **Apache Kafka**, o consumo ocorrer no **Apache Flink**, onde temos seu processamento via janela de tempo, e o cálculo da temperatura máxima, mínima e média ocorridas nesse tempo. Esses valores são printados no console, usado como sink.

#### Conclusão e perspectivas

O consumo de dados em tempo real é, podemos dizer, algo ubiquo nos tempos de hoje. O presente trabalho demonstrou um case de consumo de dados, realizado com o **Apache Flink**. Esse framework realiza tanto o consumo de dados em tempo real quanto em batch e apresentou uma API muito intuitiva e versátil, permitindo diversos tipos de computações complexas. Em perspectivas futuras esperamos realizar novos processamentos dos demais atributos lidos: photosensor,humidity, etc realizando busca de padrões usando _CEP_ e também a gravação dos dados em banco de dados _NoSql_, por exemplo ElasticSearch, e montagem de um dashaboard para exibição dos dados e métricas em tempo real.
