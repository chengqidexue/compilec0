package com.buaa.compilec0;

import com.buaa.compilec0.analyser.Analyser;
import com.buaa.compilec0.error.AnalyzeError;
import com.buaa.compilec0.error.TokenizeError;
import com.buaa.compilec0.tokenizer.StringIter;
import com.buaa.compilec0.tokenizer.Tokenizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Scanner;

@SpringBootApplication
public class Compilec0Application {

    public static void main(String[] args) {

        System.out.println(args.length);
//        var inputFileName = args[0];
//        var outputFileName = args[2];

        var inputFileName = "input.c0";
        var outputFileName = "output.o0";

        printInputFile(inputFileName);
        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        PrintStream output;
        if (outputFileName.equals("-")) {
            output = System.out;
        } else {
            try {
                output = new PrintStream(new FileOutputStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        try {
            var analyser = new Analyser(tokenizer);
            analyser.analyse();
        } catch (Exception e) {
            System.out.println("编译错误");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }

    /**
     * 不可明说的方法
     * 如果编译出错，就打印一下输入文件
     * @param inputFileName
     */
    private static void printInputFile(String inputFileName) {
        File inputFile = new File(inputFileName);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(inputFile));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine = null;
            while ((nextLine = bufferedReader.readLine()) != null) {
                System.out.println(nextLine);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}