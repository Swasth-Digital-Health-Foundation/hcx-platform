package org.swasth.auditindexer;

import org.junit.Test;
import org.swasth.auditindexer.function.AuditIndexer;

import java.util.*;

import static org.junit.Assert.*;


public class AudiIndexerTest {


    @Test
    public void auditIndexerTest() throws Exception {
        AuditIndexer auditIndexer = new AuditIndexer("localhost",9200,"test-index","test-index-alias");
        long ets = System.currentTimeMillis();
        String indexName = auditIndexer.getIndexName(ets,"test-index");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        cal.setTime(new Date(ets));
        assertEquals("test-index" + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.WEEK_OF_YEAR),indexName);
    }

}
