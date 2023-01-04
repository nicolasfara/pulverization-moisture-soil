extern crate core;

use std::io::Write;
use std::net::{Ipv4Addr, TcpListener, TcpStream};
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use esp_idf_sys as _;
// If using the `binstart` feature of `esp-idf-sys`, always keep this module imported
use esp_idf_hal::adc;
use esp_idf_hal::{
    peripherals::Peripherals,
};
use esp_idf_svc::{
    wifi::EspWifi,
    nvs::EspDefaultNvsPartition,
    eventloop::EspSystemEventLoop,
};
use embedded_svc::wifi::{ClientConfiguration, Wifi, Configuration};
use esp_idf_svc::netif::{EspNetif, EspNetifWait};
use json::object;
use spmc::{channel, Receiver};

fn handle_client(mut stream: TcpStream, consumer: Receiver<f32>) {
    loop {
        match consumer.recv() {
            Ok(value) => {
                let payload = object! {
                    moisture: value
                };
                match stream.write_all(format!("{}\n", payload.dump()).as_bytes()) {
                    Ok(_) => {}
                    Err(err) => {
                        eprintln!("Failed to send moisture value: {}", err);
                        break;
                    }
                }
                sleep(Duration::new(2, 0))
            }
            Err(_) => {}
        }
    }
}

fn main() -> anyhow::Result<()> {
    // It is necessary to call this function once. Otherwise some patches to the runtime
    // implemented by esp-idf-sys might not link properly. See https://github.com/esp-rs/esp-idf-template/issues/71
    esp_idf_sys::link_patches();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take()?;
    let nvs = EspDefaultNvsPartition::take().unwrap();
    let mut adc_pin = adc::AdcChannelDriver::<_, adc::Atten11dB<adc::ADC1>>::new(peripherals.pins.gpio34)?;
    let mut adc = adc::AdcDriver::new(
        peripherals.adc1,
        &adc::config::Config::new().calibration(true)
    )?;

    let (mut producer, consumer) = channel();

    thread::spawn(move || {
        loop {
            let value = adc.read(&mut adc_pin).unwrap();
            let percentage = (value as f32 - 142.0) * 100.0 / 3954.0;
            println!("Read value: {}", percentage);
            producer.send(percentage).unwrap();
            sleep(Duration::new(2, 0))
        }
    });

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

    let listener = TcpListener::bind("0.0.0.0:8088").unwrap();
    println!("Server listening on port 8088");


    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                println!("New connection: {}", stream.peer_addr().unwrap());
                let cons = consumer.clone();
                thread::Builder::new()
                    .stack_size(7000).spawn(move || {
                    handle_client(stream, cons);
                }).ok();
            }
            Err(e) => {
                eprintln!("Error: {}", e);
            }
        }
    }
    Ok(())
}
