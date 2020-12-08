package com.buaa.compilec0.library;

public enum LibFunctions {
    /**
     * 读入一个有符号整数
     */
    GETINT,
    /**
     * 读入一个浮点数
     */
    GETDOUBLE,
    /**
     * 读入一个字符
     */
    GETCHAR,
    /**
     * 输出一个整数
     */
    PUTINT,
    /**
     * 输出一个浮点数
     */
    PUTDOUBLE,
    /**
     * 输出一个字符
     */
    PUTCHAR,
    /**
     * 将编号为这个整数的全局常量看作字符串输出
     */
    PUTSTR,
    /**
     * 输出一个换行
     */
    PUTLN,
    /**
     * 不是库函数
     */
    NOTLIBFUN
}
