package com.example.demo;

import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.*;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
@Getter
@Setter
public class RequestHandler implements ServiceRequestHandler {

    private Logger logger = Logger.getLogger(RequestHandler.class.getName());

    private boolean[] coils = new boolean[65535];
    private short[] registers = new short[65535];
    private short writingRegister;
    private int writingCoil;
    private int countOfWritingCoils;

    @Override
    public void onReadHoldingRegisters(ServiceRequest<ReadHoldingRegistersRequest, ReadHoldingRegistersResponse> service) {
        ReadHoldingRegistersRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());

            for (int i = request.getAddress(); i < request.getAddress() + request.getQuantity(); i++) {
                buffer.writeShort(registers[i]);
            }

        service.sendResponse(new ReadHoldingRegistersResponse(buffer));
    }

    @Override
    public void onWriteSingleRegister(ServiceRequest<WriteSingleRegisterRequest, WriteSingleRegisterResponse> service) {
        WriteSingleRegisterRequest request = service.getRequest();
        service.sendResponse(new WriteSingleRegisterResponse(request.getAddress(), request.getValue()));
        registers[request.getAddress()] = (short) request.getValue();
    }

    @Override
    public void onWriteMultipleRegisters(ServiceRequest<WriteMultipleRegistersRequest, WriteMultipleRegistersResponse> service) {
        WriteMultipleRegistersRequest request = service.getRequest();
        service.sendResponse(new WriteMultipleRegistersResponse(request.getAddress(), request.getQuantity()));
        ByteBuf buffer = request.getValues();
        for (int i = request.getAddress(); i < request.getAddress() + request.getQuantity(); i++) {
            registers[i] = buffer.readShort();
        }
    }

    @Override
    public void onReadCoils(ServiceRequest<ReadCoilsRequest, ReadCoilsResponse> service) {
        ReadCoilsRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());
        byte newByte = 0;
        int counter = 0;
        for (int i = request.getAddress(); i < request.getAddress() + request.getQuantity(); i++) {
            if (coils[i]) {
                newByte = (byte) (newByte | (byte) 1 << counter);
            }
            counter ++;

            if (counter == 8) {
                buffer.writeByte(newByte);
                newByte = 0;
                counter = 0;
            }
        }
        if (request.getQuantity() % 8 != 0) {
            buffer.writeByte(newByte);
        }
        service.sendResponse(new ReadCoilsResponse(buffer));
    }

    @Override
    public void onWriteSingleCoil(ServiceRequest<WriteSingleCoilRequest, WriteSingleCoilResponse> service) {
        WriteSingleCoilRequest request = service.getRequest();
        service.sendResponse(new WriteSingleCoilResponse(request.getAddress(), request.getValue()));
        coils[request.getAddress()] = (request.getValue() == 65280);
    }

    @Override
    public void onWriteMultipleCoils(ServiceRequest<WriteMultipleCoilsRequest, WriteMultipleCoilsResponse> service) {
        WriteMultipleCoilsRequest request = service.getRequest();
        countOfWritingCoils = request.getQuantity();
        service.sendResponse(new WriteMultipleCoilsResponse(request.getAddress(), request.getQuantity()));
        ByteBuf buffer = request.getValues();
        for (int i=request.getAddress(); i<request.getQuantity(); i=i+8) {
            byte packOfCoils = buffer.readByte();
            for (int j = 0; j < 8; j++) {
                if (i + j < request.getQuantity()) {
                    coils[i+j] = (packOfCoils & (1 << j)) == (1 << j);
                }
            }
        }
    }

}
