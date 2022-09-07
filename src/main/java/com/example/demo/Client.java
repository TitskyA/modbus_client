package com.example.demo;

import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public void onReadHoldingRegisters(List<Short> readingRegisters) {
        requestHandler.setRegisters(readingRegisters);
    }

    public Short onWriteSingleRegister() {
        return requestHandler.getWritingRegister();
    }

    public List<Short> onWriteMultipleRegisters() {
        return requestHandler.getRegisters();
    }

    public void onReadCoils(List<Boolean> readingCoils) {
        byte newByte = 0;
        int counter = 0;
        List<Byte> coils = new ArrayList<>();
        for (Boolean coil: readingCoils) {
            if (coil) {
                newByte = (byte) (newByte | ((byte) 1 << counter));
            }
            counter++;
            if (counter == 8) {
                coils.add(newByte);
                newByte = 0;
                counter = 0;
            }
        }
        if (readingCoils.size() % 8 != 0) {
            coils.add(newByte);
        }
        requestHandler.setCoils(coils);
    }

    public int onWriteSingleCoil() {
        return (requestHandler.getWritingCoil() == 65280 ? 1 : 0);
    }

    public List<Byte> onWriteMultipleCoils() {
        List<Byte> coils = requestHandler.getCoils();
        List<Byte> output = new ArrayList<>();

        for (Byte coil : coils) {
            for (int j = 0; j < 8; j++) {
                if ((coil & (1 << j)) == (1 << j)) {
                    output.add((byte) 1);
                } else {
                    output.add((byte) 0);
                }
            }
        }
        return output.stream().limit(requestHandler.getCountOfWritingCoils()).toList();
    }
}
