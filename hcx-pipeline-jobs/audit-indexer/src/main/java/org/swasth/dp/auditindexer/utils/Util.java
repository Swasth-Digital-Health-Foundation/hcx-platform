package org.swasth.dp.auditindexer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static String loadAsString(final String path) {
        try {
//            File file = new File(path);
//
//            // Note:  Double backquote is to avoid compiler
//            // interpret words
//            // like \test as \t (ie. as a escape sequence)
//
//            // Creating an object of BufferedReader class
//            BufferedReader br = new BufferedReader(new FileReader(file));
//
//            // Declaring a string variable
//            String st = br.toString();
//            // Condition holds true till
//            // there is character in a string
//
//
//            System.out.println("Testing " + st);
//            return st;
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
