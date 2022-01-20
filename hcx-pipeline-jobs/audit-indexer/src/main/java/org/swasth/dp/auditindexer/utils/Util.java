package org.swasth.dp.auditindexer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static String loadAsString(final String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
