import json
import random
import threading
import time
import pandas as pd
from tabulate import tabulate
from pprint import pprint

from kafka import KafkaConsumer, KafkaProducer

from pubnub.callbacks import SubscribeCallback
from pubnub.enums import PNStatusCategory, PNOperationType
from pubnub.pnconfiguration import PNConfiguration
from pubnub.pubnub import PubNub


class SensorDataCallback(SubscribeCallback):
    message_ = None
    sink = None

    def presence(self, pubnub, presence):
        pass

    def status(self, pubnub, status):
        if status.category == PNStatusCategory.PNUnexpectedDisconnectCategory:
            print("PNStatusCategory.PNUnexpectedDisconnectCategory")

        elif status.category == PNStatusCategory.PNConnectedCategory:
            print("PNStatusCategory.PNConnectedCategory")

        elif status.category == PNStatusCategory.PNReconnectedCategory:
            print("PNStatusCategory.PNReconnectedCategory")

        elif status.category == PNStatusCategory.PNDecryptionErrorCategory:
            print("PNStatusCategory.PNDecryptionErrorCategory")

    def message(self, pubnub, message):
        self.message_ = message.message


class ProducerSensorData(threading.Thread):
    pubnub = None
    callback = None
    producer = None

    sensors = ["probe-28500df7", "probe-608a53a4",
               "probe-9fcc9d16", "probe-b796b83f", "probe-123d9907"]

    random.seed = 42

    def initializePubNubConector(self):
        pnconfig = PNConfiguration()
        pnconfig.subscribe_key = "SUBSCRIBE_KEY"
        pnconfig.publish_key = "PUBLISH_KEY"
        self.pubnub = PubNub(pnconfig)
        self.callback = SensorDataCallback()
        self.callback.sink = self.producer

    def subscribeToChannel(self, channel_id):
        self.pubnub.add_listener(self.callback)
        self.pubnub.subscribe().channels(channel_id).execute()

    def run(self):

        self.producer = KafkaProducer(bootstrap_servers='localhost:9092',
                                      value_serializer=lambda v: json.dumps(v).encode('utf-8'))

        self.initializePubNubConector()
        self.subscribeToChannel('pubnub-sensor-network')

        tempo = 0
        temps = []

        while True:
            pn_message = self.callback.message_
            if (pn_message != None):
                sensor_id = self.sensors[random.randint(0, 4)]
                __message = {
                    'sensor_uuid': sensor_id,
                    'ambient_temperature': float(pn_message['ambient_temperature']),
                    'humidity': float(pn_message['humidity']),
                    'radiation_level': int(pn_message['radiation_level']),
                    'photosensor': float(pn_message['photosensor']),
                    'timestamp': int(pn_message['timestamp'])
                }
                time.sleep(1.0)
                pprint(sensor_id + ' - ' +
                       str(__message['ambient_temperature']))

                temps.append(__message)
                self.producer.send('sensor-data', __message)
                tempo += 1
                if (tempo % 10 == 0):
                    df = pd.DataFrame(temps)
                    estatisticas = df.groupby('sensor_uuid')[
                        'ambient_temperature'].agg(['min', 'max', 'mean'])
                    print("10segs marca...")
                    print('*********************************************************')
                    print("Estatisticas por sensor")
                    print(tabulate(estatisticas, headers='keys', tablefmt='psql'))
                    temps = []


if __name__ == '__main__':
    threads = [
        ProducerSensorData()
    ]

    for t in threads:
        t.start()
