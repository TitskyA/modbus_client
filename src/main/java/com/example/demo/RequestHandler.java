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

//    private List<Byte> coils = new ArrayList<>();
    private Byte[] coils = new Byte[250];
    private Short[] registers = new Short[125];
//    private List<Short> registers = new ArrayList<>();
    private short writingRegister;
    private int writingCoil;
    private int countOfWritingCoils;

    @Override
    public void onReadHoldingRegisters(ServiceRequest<ReadHoldingRegistersRequest, ReadHoldingRegistersResponse> service) {
        ReadHoldingRegistersRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());

            for (int i = 0; i < registers.length; i++) {
                buffer.setShort(i, registers[i]);
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
        System.out.println("старт " + request.getAddress());
        System.out.println("количество" + request.getQuantity());
        for (int i = request.getAddress(); i < request.getQuantity(); i++) {
            registers[i] = buffer.getShort(i);
        }
    }

    @Override
    public void onReadCoils(ServiceRequest<ReadCoilsRequest, ReadCoilsResponse> service) {
        ReadCoilsRequest request = service.getRequest();
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(request.getQuantity());
        try {
            for (Byte i: coils) {
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
        coils[request.getAddress()] = (writingCoil == 65280 ? (byte) 1 : (byte) 0);
    }

    @Override
    public void onWriteMultipleCoils(ServiceRequest<WriteMultipleCoilsRequest, WriteMultipleCoilsResponse> service) {
        WriteMultipleCoilsRequest request = service.getRequest();
        countOfWritingCoils = request.getQuantity();
        int startByte = request.getAddress() / 8 + 1;
        service.sendResponse(new WriteMultipleCoilsResponse(request.getAddress(), request.getQuantity()));
        ByteBuf buffer = request.getValues();
        for (int i=0; i<request.getQuantity(); i=i+8) {
            Byte b = buffer.getByte(startByte);
        }
    }

}
