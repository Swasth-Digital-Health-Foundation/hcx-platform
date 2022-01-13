package org.swasth.dp.search.utils;

public class PipelineException extends Exception{

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String s, Exception ex) {
        super(s,ex);
    }
}
