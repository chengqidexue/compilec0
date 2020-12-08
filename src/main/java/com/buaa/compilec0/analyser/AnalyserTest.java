package com.buaa.compilec0.analyser;

import com.buaa.compilec0.tokenizer.StringIter;
import com.buaa.compilec0.tokenizer.Tokenizer;

import java.io.File;
import java.util.Scanner;

public class AnalyserTest {
    public static void main(String[] args) throws Exception{
        File input = new File("input.c0");
        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        var analyser = new Analyser(tokenizer);
        analyser.analyse();
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
