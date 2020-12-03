package com.buaa.compilec0.tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TokenizerTest {
    public static void main(String[] args) throws Exception{
        File input = new File("input.c0");
        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        var tokens = new ArrayList<Token>();

        try {
            while (true) {
                var token = tokenizer.nextToken();
                if (token.getTokenType().equals(TokenType.EOF)) {
                    break;
                }
                tokens.add(token);
            }
        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            System.err.println(e);
            System.exit(0);
            return;
        }
        for (Token token : tokens) {
            System.out.println(token.toString());
        }
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}