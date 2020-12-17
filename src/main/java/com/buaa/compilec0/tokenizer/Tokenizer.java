package com.buaa.compilec0.tokenizer;

import com.buaa.compilec0.error.ErrorCode;
import com.buaa.compilec0.error.TokenizeError;
import com.buaa.compilec0.util.Pos;
import org.springframework.expression.spel.SpelEvaluationException;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "EOF", it.currentPos(), it.currentPos());
        }
        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            //分析整数
            return lexUInt();
        } else if (Character.isAlphabetic(peek) || peek == '_') {
            //分析标识符或者关键字,保留字
            return lexIdentOrKeyword();
        } else if (peek == '"') {
            //分析字符串常量
            return lexStringLiteral();
        } else {
            //分析运算符
            var token = lexOperatorOrUnknown();
            if (token.getTokenType() == TokenType.COMMENT) {
                var line = token.getStartPos().row;
                while (token.getStartPos().row == line) {
                    token = nextToken();
                }
            }
            return token;
        }
    }

    private Token lexUInt() throws TokenizeError {
        Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
        StringBuilder tmpString = new StringBuilder();
        char ch = it.peekChar();
        while (Character.isDigit(ch)) {
            ch = it.nextChar();
            tmpString.append(ch);
            ch = it.peekChar();
        }
        int _value = Integer.parseInt(tmpString.toString());
        return new Token(TokenType.UINT_LITERAL, _value, startPos, it.currentPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
        StringBuilder tmpString = new StringBuilder();
        char ch = it.peekChar();
        while (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch) || ch == '_') {
            ch = it.nextChar();
            tmpString.append(ch);
            ch = it.peekChar();
        }
        String token = tmpString.toString();
        return new Token(isIdentOrReserve(token), token, startPos, it.currentPos());
    }

    /**
     * 分析是否是一个字符串字面量
     *
     * @return
     * @throws TokenizeError
     */
    private Token lexStringLiteral() throws TokenizeError {
        Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
        StringBuilder tmpString = new StringBuilder();
        it.nextChar();
        var isValid = true;
        char ch = it.peekChar();
        while (true) {
            var ascii = Integer.valueOf(ch);
            if (ch == '\u0000') {
                throw new TokenizeError(ErrorCode.InvalidString, startPos);
            }
            if (ascii < 128 && ascii != 34 && ascii != 0x5c && ascii !=  0x0B && ascii != 0x0C && ascii != 0x0D) {
                ch = it.nextChar();
                tmpString.append(ch);
                ch = it.peekChar();
            }
            else if(ch == '\\') {
                it.nextChar();
                ch = it.peekChar();
                switch (ch){
                    case '\'':
                        tmpString.append('\'');
                        break;
                    case '"':
                        tmpString.append('"');
                        break;
                    case '\\':
                        tmpString.append('\\');
                        break;
                    case 'n':
                        tmpString.append('\n');
                        break;
                    case 't':
                        tmpString.append('\t');
                        break;
                    case 'r':
                        tmpString.append('\r');
                        break;
                    default:
                        isValid = false;
                        break;
                }
                if (!isValid)
                    break;
                it.nextChar();
                ch = it.peekChar();
            }
            else if (ch == '"'){
                it.nextChar();
                break;
            }
            else {
                isValid = false;
                break;
            }
        }
        if (isValid)
            return new Token(TokenType.STRING_LITERAL, tmpString.toString(), startPos, it.currentPos());
        else
            throw new TokenizeError(ErrorCode.InvalidInput, startPos);
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", startPos, it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', startPos, it.currentPos());
            }

            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '/') {
                    it.nextChar();
                    return new Token(TokenType.COMMENT, "//", startPos, it.currentPos());
                }
                return new Token(TokenType.DIV, '/', startPos, it.currentPos());
            }

            case '=': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", startPos, it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', startPos, it.currentPos());
            }

            case '!': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", startPos, it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidOperator, startPos);
                }
            }

            case '<': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", startPos, it.currentPos());
                }
                return new Token(TokenType.LT, '<', startPos, it.currentPos());
            }

            case '>': {
                char ch = it.peekChar();
                Pos startPos = new Pos(it.currentPos().row, it.currentPos().col);
                if (ch == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", startPos, it.currentPos());
                }
                return new Token(TokenType.GT, '>', startPos, it.currentPos());
            }

            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    /**
     * 判断是标识符还是保留字
     *
     * @param token 传入的String
     * @return TokenType
     */
    private TokenType isIdentOrReserve(String token) {
        if (token.equals("fn")) {
            return TokenType.FN_KW;
        }
        if (token.equals("let")) {
            return TokenType.LET_KW;
        }
        if (token.equals("const")) {
            return TokenType.CONST_KW;
        }
        if (token.equals("as")) {
            return TokenType.AS_KW;
        }
        if (token.equals("while")) {
            return TokenType.WHILE_KW;
        }
        if (token.equals("if")) {
            return TokenType.IF_KW;
        }
        if (token.equals("else")) {
            return TokenType.ELSE_KW;
        }
        if (token.equals("return")) {
            return TokenType.RETURN_KW;
        }
        if (token.equals("break")) {
            return TokenType.BREAK_KW;
        }
        if (token.equals("continue")) {
            return TokenType.CONTINUE_KW;
        }
        if (token.equals("int")) {
            return TokenType.INT;
        }
        if (token.equals("void")) {
            return TokenType.VOID;
        }
        if (token.equals("double")) {
            return TokenType.DOUBLE;
        }
        return TokenType.IDENT;
    }
}