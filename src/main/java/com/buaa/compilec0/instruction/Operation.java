package com.buaa.compilec0.instruction;

public enum Operation {
    ILL,    //illegal instruction
    LIT,    //load int   stack[sp]=x, sp++
    LOD,    //load       stack[sp]=stack[x], sp++
    STO,    //store      stack[x]=stack[sp-1], sp--
    ADD,    //+          stack[sp-2] += stack[sp-1], sp--
    SUB,    //-          stack[sp-2] -= stack[sp-1], sp--
    MUL,    //*          stack[sp-2] *= stack[sp-1], sp--
    DIV,    ///          stack[sp-2] /= stack[sp-1], sp--
    WRT     //print      print stack[sp-1]
}