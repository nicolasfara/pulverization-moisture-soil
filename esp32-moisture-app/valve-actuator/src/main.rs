extern crate core;

use std::fs::read;
use std::time::Duration;
use std::io::{BufRead, BufReader, Read};
use std::net::{Ipv4Addr, TcpStream};

use embedded_svc::wifi::{ClientConfiguration, Configuration, Wifi};
use esp_idf_hal::peripherals::Peripherals;
use esp_idf_hal::gpio::PinDriver;
use esp_idf_svc::{
    eventloop::EspSystemEventLoop,
    nvs::EspDefaultNvsPartition,
    wifi::EspWifi,
};
use esp_idf_svc::netif::{EspNetif, EspNetifWait};
use esp_idf_sys as _;

fn main() -> anyhow::Result<()> {
    // It is necessary to call this function once. Otherwise some patches to the runtime
    // implemented by esp-idf-sys might not link properly. See https://github.com/esp-rs/esp-idf-template/issues/71
    esp_idf_sys::link_patches();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take()?;
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let mut valve_pin = PinDriver::output(peripherals.pins.gpio4)?;

    let mut wifi_driver = EspWifi::new(peripherals.modem, sys_loop.clone(), Some(nvs)).unwrap();

    wifi_driver.set_configuration(&Configuration::Client(ClientConfiguration {
        ssid: env!("SSID").into(),
        password: env!("PASSWORD").into(),
        ..Default::default()
    })).unwrap();

    wifi_driver.start().unwrap();
    wifi_driver.connect().unwrap();
    while !wifi_driver.is_connected().unwrap() {
        let config = wifi_driver.get_configuration().unwrap();
        println!("Waiting for station {:?}", config);
    }

    if !EspNetifWait::new::<EspNetif>(wifi_driver.sta_netif(), &sys_loop.clone()).unwrap().wait_with_timeout(
        Duration::from_secs(20),
        || { wifi_driver.is_connected().unwrap() && wifi_driver.sta_netif().get_ip_info().unwrap().ip != Ipv4Addr::new(0,0,0,0) }
    ) {
        panic!("")
    }

    let mut stream = TcpStream::connect(format!("{}:8088", env!("ACTUATORS_IP")))?;
    let err = stream.try_clone();
    if let Err(err) = err {
        println!(
            "Duplication of file descriptors does not work (yet) on the ESP-IDF, as expected: {}",
            err
        )
    }

    let reader = BufReader::new(&mut stream);

    for line in reader.lines() {
        let message = line.expect("Unable to read a new line");
        match message.as_str() {
            "0" => { println!("Set LOW"); valve_pin.set_low()? }
            "1" => { println!("Set HIGH"); valve_pin.set_high()? }
            _ => println!("Unable to parse the value: {}", message)
        }
    }
    Ok(())
}
