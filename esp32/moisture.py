import network
import socket
import json
from machine import Pin
from machine import ADC

def do_connect():
    import network
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print('connecting to network...')
        wlan.connect('OnePlus 9', 'ca9magqs')
        while not wlan.isconnected():
            pass
    print('network config:', wlan.ifconfig())


do_connect()

def logic():
    import time
    s = socket.socket()
    sockaddr = socket.getaddrinfo('192.168.169.213', 1672)[0][-1]
    s.connect(sockaddr)

    adcPin = Pin(34, Pin.IN)
    adc = ADC(adcPin)

    while True:
        r = adc.read_u16()
        p = {
          "moisture": r
        }
        s.send(bytes(json.dumps(p) + '\0', 'utf8'))
        time.sleep(1)

logic()
