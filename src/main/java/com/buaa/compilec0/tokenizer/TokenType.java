package com.buaa.compilec0.tokenizer;

public enum TokenType {

    /** 空 */
    None,

    /**
     * 1、保留字部分
     */
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,
    /**
     * 类型表示：ty-> IDENT
     * 在 C0 中，用到类型的地方使用一个标识符表示。
     * 这个标识符的所有可能值就是上面列出的基础类型。
     * 填入其他值的情况应被视为编译错误。
     */
    /** 基础类型 int*/
    INT,
    /** 空类型 void */
    VOID,
    /** 扩展类型 double*/
    DOUBLE,


    /**
     * 标识符
     */
    IDENT,

    /**
     * 字面量
     * 基础 c0 有两种字面量，分别是 无符号整数 和 字符串常量。
     * 扩展 c0 增加了 浮点数常量 和 字符常量。
     * 常数：无符号数、布尔常数、字符串常数等
     */
    /** 无符号整数 */
    UINT_LITERAL,

    /** 字符串常量 */
    STRING_LITERAL,

    /** 浮点数常量 */
    DOUBLE_LITERAL,

    /** 字符串常量 */
    CHAR_LITERAL,

    /**
     * 分界符
     */
    /** 加号 +*/
    PLUS,
    /** 减号 -*/
    MINUS,
    /** 乘号 **/
    MUL,
    /** 除号 /*/
    DIV,
    /** 等于 */
    ASSIGN,
    /** 双等于号 */
    EQ,
    /** 不等于 */
    NEQ,
    /** 小于 */
    LT,
    /** 大于 */
    GT,
    /** 小于等于 */
    LE,
    /** 大于等于 */
    GE,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /** 分号 */
    SEMICOLON,
    /** 注释 */
    COMMENT,


//    /** 无符号整数 */
//    Uint,
//    /** 标识符 */
//    Ident,
//    /** Begin */
//    Begin,
//    /** End */
//    End,
//    /** Var */
//    Var,
//    /** Const */
//    Const,
//    /** Print */
//    Print,
//    /** 加号 */
//    Plus,
//    /** 减号 */
//    Minus,
//    /** 乘号 */
//    Mult,
//    /** 除号 */
//    Div,
//    /** 等号 */
//    Equal,
//    /** 分号 */
//    Semicolon,
//    /** 左括号 */
//    LParen,
//    /** 右括号 */
//    RParen,
//    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case FN_KW:
                return "FN_KW";
            case LET_KW:
                return "LET_KW";
            case CONST_KW:
                return "CONST_KW";
            case AS_KW:
                return "AS_KW";
            case WHILE_KW:
                return "WHILE_KW";
            case IF_KW:
                return "IF_KW";
            case ELSE_KW:
                return "ELSE_KW";
            case RETURN_KW:
                return "RETURN_KW";
            case BREAK_KW:
                return "BREAK_KW";
            case CONTINUE_KW:
                return "CONTINUE_KW";
            case INT:
                return "INT";
            case VOID:
                return "VOID";
            case DOUBLE:
                return "DOUBLE";
            case UINT_LITERAL:
                return "UINT_LITERAL";
            case STRING_LITERAL:
                return "STRING_LITERAL";
            case DOUBLE_LITERAL:
                return "DOUBLE_LITERAL";
            case CHAR_LITERAL:
                return "CHAR_LITERAL";
            case IDENT:
                return "IDENT";
            case PLUS:
                return "PLUS";
            case MINUS:
                return "MINUS";
            case MUL:
                return "MUL";
            case DIV:
                return "DIV";
            case ASSIGN:
                return "ASSIGN";
            case EQ:
                return "EQ";
            case NEQ:
                return "NEQ";
            case LT:
                return "LT";
            case GT:
                return "GT";
            case LE:
                return "LE";
            case GE:
                return "GE";
            case L_PAREN:
                return "L_PAREN";
            case R_PAREN:
                return "R_PAREN";
            case L_BRACE:
                return "L_BRACE";
            case R_BRACE:
                return "R_BRACE";
            case ARROW:
                return "ARROW";
            case COMMA:
                return "COMMA";
            case COLON:
                return "COLON";
            case SEMICOLON:
                return "SEMICOLON";
            case COMMENT:
                return "COMMENT";


            case None:
                return "NullToken";
//            case Begin:
//                return "Begin";
//            case Const:
//                return "Const";
//            case Div:
//                return "DivisionSign";
//            case EOF:
//                return "EOF";
//            case End:
//                return "End";
//            case Equal:
//                return "EqualSign";
//            case Ident:
//                return "Identifier";
//            case LParen:
//                return "LeftBracket";
//            case Minus:
//                return "MinusSign";
//            case Mult:
//                return "MultiplicationSign";
//            case Plus:
//                return "PlusSign";
//            case Print:
//                return "Print";
//            case RParen:
//                return "RightBracket";
//            case Semicolon:
//                return "Semicolon";
//            case Uint:
//                return "UnsignedInteger";
//            case Var:
//                return "Var";
            default:
                return "InvalidToken";
        }
    }
}
