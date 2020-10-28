import json
import random
import threading
import time

from kafka import KafkaConsumer, KafkaProducer

class Consumer(threading.Thread):

    def run(self):
        stream = KafkaConsumer(bootstrap_servers='localhost:9092',
                               auto_offset_reset='latest',
                               value_deserializer=lambda m: json.loads(m.decode('utf-8')))

        stream.subscribe(['sensor-data'])

        for message in stream:
            print(f'From [ {message.topic} ] -> {message.value}')

if __name__ == '__main__':
    threads = [
        Consumer()
    ]

    for t in threads:
        t.start()
