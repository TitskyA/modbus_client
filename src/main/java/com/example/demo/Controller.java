package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {

    private final Client client;

    public Controller(Client client) {
        this.client = client;
    }

    @PostMapping("/onReadHoldingRegisters")
    public void onReadHoldingRegisters(@RequestBody List<Short> readingRegisters) {
        client.onReadHoldingRegisters(readingRegisters);
    }

    @GetMapping("/onWriteSingleRegister")
    public Short onWriteSingleRegister() {
        return client.onWriteSingleRegister();
    }

    @GetMapping("/onWriteMultipleRegisters")
    public List<Short> onWriteMultipleRegisters() {
        return client.onWriteMultipleRegisters();
    }

    @PostMapping("/onReadCoils")
    public void onReadCoils(@RequestBody List<Boolean> readingCoils) {
        client.onReadCoils(readingCoils);
    }

    @GetMapping("/onWriteSingleCoil")
    public int onWriteSingleCoil() {
        return client.onWriteSingleCoil();
    }

    @GetMapping("/onWriteMultipleCoils")
    public List<Byte> onWriteMultipleCoils() {
        return client.onWriteMultipleCoils();
    }

}
