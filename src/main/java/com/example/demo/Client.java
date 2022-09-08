package com.example.demo;

import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import org.springframework.stereotype.Component;

@Component
public class Client {

    ModbusTcpSlaveConfig config = new ModbusTcpSlaveConfig.Builder().build();
    ModbusTcpSlave slave = new ModbusTcpSlave(config);
    final RequestHandler requestHandler;

    {
        slave.bind("localhost", 502);
    }

    public Client(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        slave.setRequestHandler(requestHandler);
    }

}
