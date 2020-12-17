package com.buaa.compilec0.assembler;

public class Instruction {
    public int index;          //顺序
    public Operation opt;      //操作符
    public Integer x;                  //操作数
    private int defaultOptionNumber = -111111111;

    public Instruction(int index, Operation opt) {
        this.opt = opt;
        this.x = defaultOptionNumber;
    }

    public Instruction(int index, Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public int getDefaultOptionNumber() {
        return defaultOptionNumber;
    }

    public void setDefaultOptionNumber(int defaultOptionNumber) {
        this.defaultOptionNumber = defaultOptionNumber;
    }

    @Override
    public String toString() {
        if (x == defaultOptionNumber) {
            return opt.toString();
        } else {
            return opt.toString() + "(" + x + ")";
        }
    }
}