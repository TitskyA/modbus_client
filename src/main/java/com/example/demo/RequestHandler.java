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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
@Getter
@Setter
public class RequestHandler implements ServiceRequestHandler {

    private Logger logger = Logger.getLogger(RequestHandler.class.getName());

    private List<Short> readingRegisters;
    private short writingRegister;
    private List<Short> writingRegisters = new ArrayList<>();
    private List<Byte> readingCoils;
    private int writingCoil;
    private List<Byte> writingCoils = new ArrayList<>();
    private int countOfWritingCoils;

    @Override
    public void onReadHoldingRegisters(ServiceRequest<ReadHoldingRegistersRequest, ReadHoldingRegistersResponse> service) {
        ReadHoldingRegistersRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());
        try {
            for (int i=0; i < request.getQuantity(); i++) {
                buffer.writeShort(readingRegisters.get(i));
            }
        } catch (NullPointerException exception) {
            logger.log(Level.WARNING, "Массив читаемых регистров пуст");
        }
        service.sendResponse(new ReadHoldingRegistersResponse(buffer));
    }

    @Override
    public void onWriteSingleRegister(ServiceRequest<WriteSingleRegisterRequest, WriteSingleRegisterResponse> service) {
        WriteSingleRegisterRequest request = service.getRequest();
        service.sendResponse(new WriteSingleRegisterResponse(request.getAddress(), request.getValue()));
        writingRegister = (short) request.getValue();
    }

    @Override
    public void onWriteMultipleRegisters(ServiceRequest<WriteMultipleRegistersRequest, WriteMultipleRegistersResponse> service) {
        WriteMultipleRegistersRequest request = service.getRequest();
        service.sendResponse(new WriteMultipleRegistersResponse(request.getAddress(), request.getQuantity()));
        ByteBuf buffer = request.getValues();
        writingRegisters.clear();
        for (int i=0; i < request.getQuantity(); i++) {
            writingRegisters.add(buffer.readShort());
        }
    }

    @Override
    public void onReadCoils(ServiceRequest<ReadCoilsRequest, ReadCoilsResponse> service) {
        ReadCoilsRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());
        try {
            for (Byte i: readingCoils) {
                buffer.writeByte(i);
            }
        } catch (NullPointerException exception) {
            logger.log(Level.WARNING, "Массив читаемых койлов пуст");
        }
        service.sendResponse(new ReadCoilsResponse(buffer));
    }

    @Override
    public void onWriteSingleCoil(ServiceRequest<WriteSingleCoilRequest, WriteSingleCoilResponse> service) {
        WriteSingleCoilRequest request = service.getRequest();
        service.sendResponse(new WriteSingleCoilResponse(request.getAddress(), request.getValue()));
        writingCoil = request.getValue();
    }

    @Override
    public void onWriteMultipleCoils(ServiceRequest<WriteMultipleCoilsRequest, WriteMultipleCoilsResponse> service) {
        WriteMultipleCoilsRequest request = service.getRequest();
        countOfWritingCoils = request.getQuantity();
        service.sendResponse(new WriteMultipleCoilsResponse(request.getAddress(), request.getQuantity()));
        ByteBuf buffer = request.getValues();
        writingCoils.clear();
        for (int i=0; i<request.getQuantity(); i=i+8) {
            writingCoils.add(buffer.readByte());
        }
    }

}
