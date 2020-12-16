package com.buaa.compilec0.assembler;

public class Instruction {
    private Operation opt;
    Integer x;
    private int defaultOptionNumber = -111111111;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = defaultOptionNumber;
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    @Override
    public String toString() {
        if (x == defaultOptionNumber) {
            return opt.toString();
        } else {
            return opt.toString() + "(" + x + ")\n";
        }
    }
}