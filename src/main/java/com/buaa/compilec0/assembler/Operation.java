package com.buaa.compilec0.assembler;

public enum Operation {
    nop,
    push,
    pop,
    popn,
    dup,
    loca,
    arga,
    globa,
    load8,
    load16,
    load32,
    load64,
    store8,
    store16,
    store32,
    store64,
    alloc,
    free,
    stackalloc,
    addi,
    subi,
    muli,
    divi,
    addf,
    subf,
    mulf,
    divf,
    divu,
    shl,
    shr,
    and,
    or,
    xor,
    not,
    cmpi,
    cmpu,
    cmpf,
    negi,
    negf,
    itof,
    ftoi,
    shrl,
    setlt,
    setgt,
    br,
    brfalse,
    brtrue,
    call,
    ret,
    callname,
    scani,
    scanc,
    scanf,
    printi,
    printc,
    printf,
    prints,
    println,
    panic;

    @Override
    public String toString() {
        switch (this){
            case nop:
                return "Nop";
            case push:
                return "Push";
            case pop:
                return "Pop";
            case popn:
                return "PopN";
            case dup:
                return "Dup";
            case loca:
                return "Loca";
            case arga:
                return "Arga";
            case globa:
                return "Global";
            case load8:
                return "Load8";
            case load16:
                return "Load16";
            case load32:
                return "Load32";
            case load64:
                return "Load64";
            case store8:
                return "Store8";
            case store16:
                return "Store16";
            case store32:
                return "Store32";
            case store64:
                return "Store64";
            case alloc:
                return "Alloc";
            case free:
                return "Free";
            case stackalloc:
                return "StackAlloc";
            case addi:
                return "AddI";
            case subi:
                return "SubI";
            case muli:
                return "MulI";
            case divi:
                return "DivI";
            case addf:
                return "AddF";
            case subf:
                return "SubF";
            case mulf:
                return "MulF";
            case divf:
                return "DivF";
            case divu:
                return "DivU";
            case shl:
                return "Shl";
            case shr:
                return "Shr";
            case and:
                return "And";
            case or:
                return "Or";
            case xor:
                return "Xor";
            case not:
                return "Not";
            case cmpi:
                return "CmpI";
            case cmpu:
                return "CmpU";
            case cmpf:
                return "CmpF";
            case negi:
                return "NegI";
            case negf:
                return "NegF";
            case itof:
                return "IToF";
            case ftoi:
                return "FToI";
            case shrl:
                return "ShrL";
            case setlt:
                return "SetLt";
            case setgt:
                return "SetGt";
            case br:
                return "Br";
            case brfalse:
                return "BrFalse";
            case brtrue:
                return "BrTrue";
            case call:
                return "Call";
            case ret:
                return "Ret";
            case callname:
                return "CallName";
            case scani:
                return "ScanI";
            case scanc:
                return "ScanC";
            case scanf:
                return "ScanF";
            case printi:
                return "PrintI";
            case printc:
                return "PrintC";
            case printf:
                return "PrintF";
            case prints:
                return "PrintS";
            case println:
                return "PrintLn";
            case panic:
                return "Panic";
            default:
                return "Error";
        }
    }
}