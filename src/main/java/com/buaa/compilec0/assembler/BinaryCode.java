package com.buaa.compilec0.assembler;

import java.io.File;

public class BinaryCode {
    private Assembler assembler;
    private File outputFile;

    public BinaryCode(Assembler assembler, File outputFile) {
        this.assembler = assembler;
        this.outputFile = outputFile;
    }

    public File writeToOutput() {
        return outputFile;
    }


}
