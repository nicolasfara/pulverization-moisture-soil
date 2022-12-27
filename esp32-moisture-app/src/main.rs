use std::io::{Read, Write};
use std::net::TcpStream;
use std::thread::sleep;
use std::time::Duration;
use esp_idf_hal::adc::{AdcChannelDriver, AdcDriver, Atten11dB};
use esp_idf_hal::adc::config::Config;
use esp_idf_sys as _; // If using the `binstart` feature of `esp-idf-sys`, always keep this module imported
use esp_idf_hal::gpio::*;
use esp_idf_hal::delay::FreeRtos;
use esp_idf_hal::{
    peripherals::Peripherals,
};
use esp_idf_svc::{
    wifi::EspWifi,
    nvs::EspDefaultNvsPartition,
    eventloop::EspSystemEventLoop,
};
use embedded_svc::wifi::{ClientConfiguration, Wifi, Configuration};
use log::info;

fn main() -> anyhow::Result<()> {
    // It is necessary to call this function once. Otherwise some patches to the runtime
    // implemented by esp-idf-sys might not link properly. See https://github.com/esp-rs/esp-idf-template/issues/71
    esp_idf_sys::link_patches();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take().unwrap();
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let mut led = PinDriver::output(peripherals.pins.gpio4)?;
    let mut adc = AdcDriver::new(peripherals.adc2, &Config::new().calibration(true))?;
    let mut adc_pin: AdcChannelDriver<'_, Gpio12, Atten11dB<_>> = AdcChannelDriver::new(peripherals.pins.gpio12)?;

    let mut wifi_driver = EspWifi::new(
        peripherals.modem,
        sys_loop,
        Some(nvs)
    ).unwrap();

    wifi_driver.set_configuration(&Configuration::Client(ClientConfiguration {
        ssid: "Farabegoli Wi-Fi".into(),
        password: "farabegoliprivate".into(),
        ..Default::default()
    })).unwrap();

    wifi_driver.start().unwrap();
    wifi_driver.connect().unwrap();
    while !wifi_driver.is_connected().unwrap() {
        let config = wifi_driver.get_configuration().unwrap();
        println!("Waiting for station {:?}", config);
    }
    println!("Should be connected now");

    let mut stream = TcpStream::connect("1.1.1.1:80")?;
    let err = stream.try_clone();
    if let Err(err) = err {
        info!(
            "Duplication of file descriptors does not work (yet) on the ESP-IDF, as expected: {}",
            err
        );
    }

    stream.write_all("GET / HTTP/1.0\n\n".as_bytes())?;

    let mut result = Vec::new();

    stream.read_to_end(&mut result)?;

    info!(
        "1.1.1.1 returned:\n=================\n{}\n=================\nSince it returned something, all is OK",
        std::str::from_utf8(&result)?);


    loop {
        println!("IP info: {:?}", wifi_driver.sta_netif().get_ip_info().unwrap());
        sleep(Duration::new(10,0))
        // led.set_high()?;
        // FreeRtos::delay_ms(1000);
        // led.set_low()?;
        // FreeRtos::delay_ms(1000);
        // let read = adc.read(&mut adc_pin).unwrap();
        // println!("ADC read: {}", read);
    }
}
