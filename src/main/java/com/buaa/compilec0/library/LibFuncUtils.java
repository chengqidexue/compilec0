package com.buaa.compilec0.library;

import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.tokenizer.TokenType;

public class LibFuncUtils {
    /**
     * 判断是不是标准库里面的函数
     * @param token 函数名字的 token
     * @return  返回库函数的种类，不是的话返回LibFunctions.NOTLIBFUN
     */
    public static LibFunctions isLibFunction(Token token) {
        var func = LibFunctions.NOTLIBFUN;
        if (token.getTokenType() != TokenType.IDENT) {
            return func;
        } else {
            var funcName = token.getValue();
            if (funcName.equals("getint")) {
                func = LibFunctions.GETINT;
            }
            else if (funcName.equals("getdouble")) {
                func = LibFunctions.GETDOUBLE;
            }
            else if (funcName.equals("getchar")) {
                func = LibFunctions.GETCHAR;
            }
            else if (funcName.equals("putint")) {
                func = LibFunctions.PUTINT;
            }
            else if (funcName.equals("putdouble")) {
                func = LibFunctions.PUTDOUBLE;
            }
            else if (funcName.equals("putchar")) {
                func = LibFunctions.PUTCHAR;
            }
            else if (funcName.equals("putstr")) {
                func = LibFunctions.PUTSTR;
            }
            else if (funcName.equals("putln")) {
                func = LibFunctions.PUTLN;
            }
        }
        return func;
    }
}