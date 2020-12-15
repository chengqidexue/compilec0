package com.buaa.compilec0.analyser;

import ch.qos.logback.classic.layout.TTLLLayout;
import com.buaa.compilec0.error.*;
import com.buaa.compilec0.instruction.Instruction;
import com.buaa.compilec0.symbol.DataType;
import com.buaa.compilec0.symbol.SymbolTable;
import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.tokenizer.TokenType;
import com.buaa.compilec0.tokenizer.Tokenizer;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    private SymbolTable symbolTable = new SymbolTable();

    /**
     * 符号表的层次
     * 永远指向我现在所分析到的层次
     */
    private int level = 0;

    /**
     * 下一个变量的栈偏移
     */
    private int nextOffset = 0;

    /**
     * 所有的tokens
     */
    ArrayList<Token> allTokens;
    /**
     * 要获取的token的编号
     */
    private int tokenIndex = -1;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * debug
     * @param tokenizer
     */
    boolean debug = true;

    public Analyser(Tokenizer tokenizer) {
        this.allTokens = new ArrayList<>();
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        /**
         * 首先将所有的tokens存到list中
         */
        getAllTokens();
        analyseProgram();
        return instructions;
    }

    /**
     * 获取所有的token到tokens
     */
    public void getAllTokens() throws TokenizeError {
        while (true) {
            var token = tokenizer.nextToken();
            if (token.getTokenType().equals(TokenType.EOF)) {
                this.allTokens.add(token);
                break;
            }
            this.allTokens.add(token);
        }
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = allTokens.get(tokenIndex+1);
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return 返回的是下一个token
     * @throws TokenizeError 不会抛出错误目前
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            this.tokenIndex++;
            return token;
        } else {
            return allTokens.get(++tokenIndex);
        }
    }

    /**
     * 将指针指向上一个token
     * 即：回退一步
     */
    private void back() throws TokenizeError {
        this.tokenIndex--;
        peekedToken = null;
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt 类型
     * @return true
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 如果下一个 token 的类型是数据类型(int、void、double), 则前进一个 token 并返回，否则抛出异常
     * int double void
     * @return 这个  token
     * @throws CompileError 类型不匹配
     */
    private Token expectType() throws CompileError {
        var token = peek();
        var tokenType = token.getTokenType();
        if (tokenType == TokenType.INT ||
                tokenType == TokenType.VOID ||
                tokenType == TokenType.DOUBLE) {
            return next();
        } else {
            ArrayList<TokenType> types = new ArrayList<>();
            types.add(TokenType.INT);
            types.add(TokenType.VOID);
            types.add(TokenType.DOUBLE);
            throw new ExpectedTokenError(types, token);
        }
    }

    /**
     * 把token的类型转化为数据类型
     * @param token
     * @return
     * @throws CompileError
     */
    private DataType getDataTypeFromToken(Token token) throws CompileError {
        if (token.getTokenType() == TokenType.INT) {
            return DataType.INT;
        } else if (token.getTokenType() == TokenType.DOUBLE) {
            return DataType.DOUBLE;
        } else if (token.getTokenType() == TokenType.VOID) {
            return DataType.VOID;
        } else {
            throw new AnalyzeError(ErrorCode.NotExistDataType, token.getStartPos());
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * @return
     */
    private int getNextOffset() {
        return this.nextOffset++;
    }

    /**
     * program -> item*
     * item -> function | decl_stmt
     */
    private void analyseProgram() throws CompileError {
        //push主函数的符号表
        symbolTable.pushSymbolTable();

        Token nextPeekToken = peek();
        while (true) {
            if (nextPeekToken.getTokenType() == TokenType.EOF) {
                break;
            } else if (nextPeekToken.getTokenType() == TokenType.FN_KW) {
                analyseFunction();
            } else if (nextPeekToken.getTokenType() == TokenType.LET_KW || nextPeekToken.getTokenType() == TokenType.CONST_KW) {
                analyseDeclareStatement();
            } else {
                throw new AnalyzeError(ErrorCode.InvalidInput, nextPeekToken.getStartPos());
            }
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     * function_param_list -> function_param (',' function_param)*
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     *
     * @throws CompileError
     */
    private void analyseFunction() throws CompileError {
        //fn
        expect(TokenType.FN_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        //(
        expect(TokenType.L_PAREN);
        var token = peek();
        if (token.getTokenType() != TokenType.R_PAREN) {
            //参数列表
            analyseFunctionParamList();
        }
        //)
        expect(TokenType.R_PAREN);
        //->
        expect(TokenType.ARROW);
        //ty
        var type = expectType();
        //blockStatement
        analyseBlockStatement();
    }

    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList() throws CompileError{
        //参数
        analyseFunctionParam();
        var next = peek();
        while (next.getTokenType() == TokenType.COMMA) {
            expect(TokenType.COMMA);
            analyseFunctionParam();
            next = peek();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     * @throws CompileError
     */
    private void analyseFunctionParam() throws CompileError{
        var next = peek();
        if (next.getTokenType() == TokenType.CONST_KW) {
            //const
            expect(TokenType.CONST_KW);
            //IDENT
            var ident = expect(TokenType.IDENT);
            //:
            expect(TokenType.COLON);
            //ty
            var type = expectType();
        } else if (next.getTokenType() == TokenType.IDENT){
            //IDENT
            var ident = expect(TokenType.IDENT);
            //:
            expect(TokenType.COLON);
            //ty
            var type = expectType();
        }
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     *
     * @throws CompileError
     */
    private void analyseDeclareStatement() throws CompileError {
        Token nextPeekToken = peek();
        if (nextPeekToken.getTokenType() == TokenType.LET_KW) {
            analyseLetDeclareStatement();
        } else if (nextPeekToken.getTokenType() == TokenType.CONST_KW) {
            analyseConstDeclareStatement();
        } else {
            throw new AnalyzeError(ErrorCode.InvalidInput, nextPeekToken.getStartPos());
        }
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private void analyseLetDeclareStatement() throws CompileError {
        //let
        expect(TokenType.LET_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        //:
        expect(TokenType.COLON);
        //ty
        var type = expectType();
        var dataType = getDataTypeFromToken(type);

        var next = peek();
        if (next.getTokenType() == TokenType.ASSIGN) {
            expect(TokenType.ASSIGN);
            analyseExpression();
        }
        //;
        expect(TokenType.SEMICOLON);

    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private void analyseConstDeclareStatement() throws CompileError {
        //const
        expect(TokenType.CONST_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        //:
        expect(TokenType.COLON);
        //ty
        var type = expectType();
        var dataType = getDataTypeFromToken(type);

        //=
        expect(TokenType.ASSIGN);
        //expr
        analyseExpression();
        //;
        expect(TokenType.SEMICOLON);
        //加入符号表
        symbolTable.addConstantSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos());
    }

    /**
     * block_stmt -> '{' stmt* '}'
     * @throws CompileError
     */
    private void analyseBlockStatement() throws CompileError{
        //{
        expect(TokenType.L_BRACE);
        var next = peek();
        while(next.getTokenType() != TokenType.R_BRACE) {
            analyseStatement();
            next = peek();
        }
        //}
        expect(TokenType.R_BRACE);
    }

    /**
     * 语句
     * stmt ->
     *       expr_stmt
     *     | decl_stmt
     *     | if_stmt
     *     | while_stmt
     *     | break_stmt
     *     | continue_stmt
     *     | return_stmt
     *     | block_stmt
     *     | empty_stmt
     * @throws CompileError
     */
    private void analyseStatement() throws CompileError {
        var next = peek();
        var tokenType  = next.getTokenType();
        //decl_stmt
        if (tokenType == TokenType.LET_KW) {
            analyseLetDeclareStatement();
        }
        else if (tokenType == TokenType.CONST_KW) {
            analyseConstDeclareStatement();
        }
        //if
        else if (tokenType == TokenType.IF_KW) {
            analyseIfStatement();
        }
        //while
        else if (tokenType == TokenType.WHILE_KW) {
            analyseWhileStatement();
        }
        //break
        else if (tokenType == TokenType.BREAK_KW) {
            analyseBreakStatement();
        }
        //continue
        else if (tokenType == TokenType.CONTINUE_KW) {
            analyseContinueStatement();
        }
        //return
        else if (tokenType == TokenType.RETURN_KW) {
            analyseReturnStatement();
        }
        //block
        else if (tokenType == TokenType.L_BRACE) {
            analyseBlockStatement();
        }
        //empty
        else if (tokenType == TokenType.SEMICOLON) {
            analyseEmptyStatement();
        }
        //expr_statement
        else {
            analyseExpressionStatement();
        }
    }

    /**
     * empty_stmt -> ';'
     */
    private void analyseEmptyStatement() throws CompileError{
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * return_stmt -> 'return' expr? ';'
     * @throws CompileError
     */
    private void analyseReturnStatement() throws CompileError{
        //return
        expect(TokenType.RETURN_KW);
        var next = peek();
        if (next.getTokenType() == TokenType.SEMICOLON) {
            //;
            expect(TokenType.SEMICOLON);
        }
        else {
            //expr
            analyseExpression();
            //;
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * continue_stmt -> 'continue' ';'
     * @throws CompileError
     */
    private void analyseContinueStatement() throws CompileError{
        //continue
        expect(TokenType.CONTINUE_KW);
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * break_stmt -> 'break' ';'
     * @throws CompileError
     */
    private void analyseBreakStatement() throws CompileError{
        //break
        expect(TokenType.BREAK_KW);
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     * @throws CompileError
     */
    private void analyseWhileStatement() throws CompileError{
        //while
        expect(TokenType.WHILE_KW);
        //expr
        analyseExpression();
        //block
        analyseBlockStatement();
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     * @throws CompileError
     */
    private void analyseIfStatement() throws CompileError{
        //if
        expect(TokenType.IF_KW);
        //expr
        analyseExpression();
        //block
        analyseBlockStatement();
        var next = peek();
        //else if | else
        while (next.getTokenType() == TokenType.ELSE_KW) {
            expect(TokenType.ELSE_KW);
            next = peek();

            //else if
            if (next.getTokenType() == TokenType.IF_KW) {
                expect(TokenType.IF_KW);
                analyseBlockStatement();
                next = peek();
            }
            //else
            else if (next.getTokenType() == TokenType.L_BRACE) {
                analyseBlockStatement();
                break;
            }
        }
    }

    /**
     * expr_stmt -> expr ';'
     */
    private void analyseExpressionStatement() throws CompileError{
        //expr
        analyseExpression();
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * // # 表达式
     * expr ->
     *       operator_expr
     *     | negate_expr
     *     | assign_expr
     *     | as_expr
     *     | call_expr
     *     | literal_expr
     *     | ident_expr
     *     | group_expr
     * @throws CompileError
     * 为了修改左递归，我们将expression修改一下
     * 将赋值语句从Expression中移除
     * assign_expr -> IDENT '=' additive_expr
     * expr -> assign_expr
     *        |additive_expr ('=='|'!='|'>'|'>='|'<'|'<=' additive_expr)?
     */
    private void analyseExpression() throws CompileError{
        var token = peek();
        if (token.getTokenType() == TokenType.IDENT) {
            //赋值语句吗
            next();
            token = peek();
            back();
            if (token.getTokenType() == TokenType.ASSIGN) {
                analyseAssignStatement();
            }
            //非赋值语句那就只能是加法表达式，因为赋值表达式和条件表达式都不能作为返回值
            else {
                analyseAdditiveExpression();
            }
        }
        else {
            analyseAdditiveExpression();
            token = peek();
            if (token.getTokenType() == TokenType.EQ || token.getTokenType() == TokenType.NEQ
             || token.getTokenType() == TokenType.GT || token.getTokenType() == TokenType.GE
             || token.getTokenType() == TokenType.LT || token.getTokenType() == TokenType.LE) {
                analyseAdditiveExpression();
            }
        }
    }

    /**
     * assign_expr -> IDENT '=' additive_expr
      * @throws CompileError
     */
    private void analyseAssignStatement() throws CompileError{
        //IDENT
        var ident = expect(TokenType.IDENT);
        //=
        expect(TokenType.ASSIGN);
        //additive_expr
        analyseAdditiveExpression();
    }

    /**
     * additive_expr -> mult_expr {'+'|'-' mult_expr}
     * @throws CompileError
     */
    private void analyseAdditiveExpression() throws CompileError{
        analyseMultExpression();
        while ((check(TokenType.PLUS)) || (check(TokenType.MINUS))) {
            var nameToken = next();
            analyseMultExpression();
        }
    }

    /**
     * mult_expr -> as_expr {'*'|'/' as_expr}
     * @throws CompileError
     */
    private void analyseMultExpression() throws CompileError{
        analyseAsExpression();
        while (check(TokenType.MUL) || check(TokenType.DIV)) {
            var nameToken = next();
            analyseAsExpression();
        }
    }

    /**
     * 类型转换表达式
     * as_expr -> single_expr {'as' ty}
     */
    private void analyseAsExpression() throws CompileError{
        analyseSingleExpression();
        while (check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            analyseSingleExpression();
        }
    }

    /**
     * 单目表达式
     * single_expr -> {'-'} primary_expr
     */
    private void analyseSingleExpression() throws CompileError{
        var token = peek();
        if (token.getTokenType() == TokenType.MINUS) {
            expect(TokenType.MINUS);
            analyseSingleExpression();
        }
        else {
            analysePrimaryExpression();
        }
    }

    /**
     * 最小表达式
     * primary_expr -> call_expr
     *               | literal_expr
     *               | ident_expr
     *               | group_expr
     */
    private void analysePrimaryExpression() throws CompileError{
        var token = peek();
        if (token.getTokenType() == TokenType.IDENT) {
            next();
            token = peek();
            back();
            if (token.getTokenType() == TokenType.L_PAREN) {
                analyseCallExpression();
            }
            else {
                analyseIdentExpression();
            }
        }
        else if (token.getTokenType() == TokenType.UINT_LITERAL
               ||token.getTokenType() == TokenType.DOUBLE_LITERAL
               ||token.getTokenType() == TokenType.STRING_LITERAL
               ||token.getTokenType() == TokenType.CHAR_LITERAL) {
            analyseLiteralExpression();
        }
        else if (token.getTokenType() == TokenType.L_PAREN) {
            expect(TokenType.L_PAREN);
            analyseExpression();
            expect(TokenType.R_PAREN);
        }
    }

    /**
     * 调用函数的表达式
     * call_param_list -> expr (',' expr)*
     * call_expr -> IDENT '(' call_param_list? ')'
     */
    private void analyseCallExpression() throws CompileError{
        //IDENT
        var ident = expect(TokenType.IDENT);
        //(
        expect(TokenType.L_PAREN);
        if (!check(TokenType.R_PAREN)) {
            //call_param_list
            analyseCallParamList();
        }
        //)
        expect(TokenType.R_PAREN);
    }

    /**
     * call_param_list -> expr (',' expr)*
     * @throws CompileError
     */
    private void analyseCallParamList() throws CompileError{
        analyseAdditiveExpression();
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseAdditiveExpression();
        }
    }

    /**
     * ident_expr -> IDENT
     */
    private void analyseIdentExpression() throws CompileError{
        //IDENT
        var ident = expect(TokenType.IDENT);
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private void analyseLiteralExpression() throws CompileError{
        var token = next();
        if (token.getTokenType() == TokenType.UINT_LITERAL) {
            //TODO
        }
        else if (token.getTokenType() == TokenType.DOUBLE_LITERAL) {
            //TODO
        }
        else if (token.getTokenType() == TokenType.STRING_LITERAL) {
            //TODO
        }else if (token.getTokenType() == TokenType.CHAR_LITERAL) {
            //TODO
        }
    }
}