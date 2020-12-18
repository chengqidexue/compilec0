package com.buaa.compilec0.assembler;

import com.buaa.compilec0.symbol.DataType;

import java.io.File;
import java.io.FileOutputStream;

public class BinaryCode {
    private Assembler assembler;
    private FileOutputStream fileOutputStream;

    public BinaryCode(Assembler assembler) {
        this.assembler = assembler;
    }

    public void writeToOutput(File outputFile) throws Exception {
        fileOutputStream = new FileOutputStream(outputFile);
        //魔数magic
        byte[] magic = {0x72, 0x30, 0x3b, 0x3e};
        fileOutputStream.write(magic);
        //version
        byte[] version = {0x00, 0x00, 0x00, 0x01};
        fileOutputStream.write(version);
        //全局变量表
        writeGlobalVariableList();
        //函数列表
        writeFunctionList();
    }


    private void writeGlobalVariableList() throws Exception {
        //长度
        long globalCount = assembler.globals.size() + 1;
        System.out.println(globalCount);
        fileOutputStream.write(longTo4Byte(globalCount));
        //写入globals
        for (Global global : assembler.globals) {
            writeGlobal(global);
        }
        //写入_start
        writeGlobal(assembler.start);
    }

    private void writeGlobal(Global global) throws Exception {
        //is_const
        byte isConstant = 0x00;
        if (global.getGlobalType() == GlobalType.CONSTANT) {
            isConstant = 0x01;
        }
        fileOutputStream.write(isConstant);
        //value.count
        long globalValueCount = global.getData().length;
        fileOutputStream.write(longTo4Byte(globalValueCount));
        //value
        fileOutputStream.write(global.getData());
    }

    private void writeFunctionList() throws Exception {
        //count
        long functionCount = assembler.functions.size() + 1;
        fileOutputStream.write(longTo4Byte(functionCount));
        //写入start
        writeFunction(assembler.startFunction);
        //写入列表
        for (Function function : assembler.functions) {
            writeFunction(function);
        }
    }

    private void writeFunction(Function function) throws Exception {
        //name
        long functionName = function.getGlobalOffset();
        fileOutputStream.write(longTo4Byte(functionName));
        //返回值
        long retSlots = 0;
        if (function.getReturnType() == DataType.INT || function.getReturnType() == DataType.DOUBLE) {
            retSlots = 1;
        }
        fileOutputStream.write(longTo4Byte(retSlots));
        //参数大小
        long paramSlots = function.getParamSize();
        fileOutputStream.write(longTo4Byte(paramSlots));
        //局部变量数目
        long locSlots = function.getLocalVariableSize();
        fileOutputStream.write(longTo4Byte(locSlots));
        //指令数目
        long bodyCount = function.getInstructions().size();
        fileOutputStream.write(longTo4Byte(bodyCount));

        //写入instructions
        for (Instruction instruction : function.getInstructions()) {
            writeInstruction(instruction);
        }
    }

    private void writeInstruction(Instruction instruction) throws Exception {
        switch (instruction.opt) {
            case nop:
                write1((byte) 0x00);
                break;
            case push:
                write9((byte) 0x01, (long) instruction.getX());
                break;
            case pop:
                write1((byte) 0x02);
                break;
            case popn:
                write5((byte) 0x03, (long) instruction.getX());
                break;
            case dup:
                write1((byte) 0x04);
                break;
            case loca:
                write5((byte) 0x0a, (long) instruction.getX());
                break;
            case arga:
                write5((byte) 0x0b, (long) instruction.getX());
                break;
            case globa:
                write5((byte) 0x0c, (long) instruction.getX());
                break;
            case load8:
                write1((byte) 0x10);
                break;
            case load16:
                write1((byte) 0x11);
                break;
            case load32:
                write1((byte) 0x12);
                break;
            case load64:
                write1((byte) 0x13);
                break;
            case store8:
                write1((byte) 0x14);
                break;
            case store16:
                write1((byte) 0x15);
                break;
            case store32:
                write1((byte) 0x16);
                break;
            case store64:
                write1((byte) 0x17);
                break;
            case alloc:
                write1((byte) 0x18);
                break;
            case free:
                write1((byte) 0x19);
                break;
            case stackalloc:
                write5((byte) 0x1a, (long) instruction.getX());
                break;
            case addi:
                write1((byte) 0x20);
                break;
            case subi:
                write1((byte) 0x21);
                break;
            case muli:
                write1((byte) 0x22);
                break;
            case divi:
                write1((byte) 0x23);
                break;
            case addf:
                write1((byte) 0x24);
                break;
            case subf:
                write1((byte) 0x25);
                break;
            case mulf:
                write1((byte) 0x26);
                break;
            case divf:
                write1((byte) 0x27);
                break;
            case divu:
                write1((byte) 0x28);
                break;
            case shl:
                write1((byte) 0x29);
                break;
            case shr:
                write1((byte) 0x2a);
                break;
            case and:
                write1((byte) 0x2b);
                break;
            case or:
                write1((byte) 0x2c);
                break;
            case xor:
                write1((byte) 0x2d);
                break;
            case not:
                write1((byte) 0x2e);
                break;
            case cmpi:
                write1((byte) 0x30);
                break;
            case cmpu:
                write1((byte) 0x31);
                break;
            case cmpf:
                write1((byte) 0x32);
                break;
            case negi:
                write1((byte) 0x34);
                break;
            case negf:
                write1((byte) 0x35);
                break;
            case itof:
                write1((byte) 0x36);
                break;
            case ftoi:
                write1((byte) 0x37);
                break;
            case shrl:
                write1((byte) 0x38);
                break;
            case setlt:
                write1((byte) 0x39);
                break;
            case setgt:
                write1((byte) 0x3a);
                break;
            case br:
                write5((byte) 0x41, (int) instruction.getX());
                break;
            case brfalse:
                write5((byte) 0x42, (int) instruction.getX());
                break;
            case brtrue:
                write5((byte) 0x43, (int) instruction.getX());
                break;
            case call:
                write5((byte) 0x48, (long) instruction.getX());
                break;
            case ret:
                write1((byte) 0x49);
                break;
            case callname:
                write5((byte) 0x4a, (long) instruction.getX());
                break;
            case scani:
                write1((byte) 0x50);
                break;
            case scanc:
                write1((byte) 0x51);
                break;
            case scanf:
                write1((byte) 0x52);
                break;
            case printi:
                write1((byte) 0x54);
                break;
            case printc:
                write1((byte) 0x55);
                break;
            case printf:
                write1((byte) 0x56);
                break;
            case prints:
                write1((byte) 0x57);
                break;
            case println:
                write1((byte) 0x58);
                break;
            case panic:
                write1((byte) 0xfe);
                break;
            default:
                throw new Exception();
        }
    }

    private void write1(byte operation) throws Exception {
        fileOutputStream.write(operation);
    }

    private void write5(byte operation, long x) throws Exception {
        fileOutputStream.write(operation);
        fileOutputStream.write(longTo4Byte(x));
    }

    private void write5(byte operation, int x) throws Exception {
        fileOutputStream.write(operation);
        fileOutputStream.write(intTo4Byte(x));
    }

    private void write9(byte operation, long x) throws Exception {
        fileOutputStream.write(operation);
        fileOutputStream.write(longTo8Byte(x));
    }

    private byte[] longTo4Byte(long value) {
        byte[] src = new byte[4];
        src[3] =  (byte) (value & 0xFF);
        src[2] =  (byte) ((value >>> 8) & 0xFF);
        src[1] =  (byte) ((value >>> 16) & 0xFF);
        src[0] =  (byte) ((value >>> 24) & 0xFF);
        return src;
    }

    private byte[] intTo4Byte(int value) {
        byte[] src = new byte[4];
        src[3] =  (byte) (value & 0xFF);
        src[2] =  (byte) ((value >> 8) & 0xFF);
        src[1] =  (byte) ((value >> 16) & 0xFF);
        src[0] =  (byte) ((value >> 24) & 0xFF);
        return src;
    }

    private byte[] longTo8Byte(long value) {
        byte[] src = new byte[8];
        for (int i = 7; i >= 0; i--) {
            src[i] = (byte) ((value >>> (8*(7-i))) & 0xFF);
        }
        return src;
    }
}