package org.swasth.audit.indexer.utils;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.swasth.auditindexer.utils.ElasticSearchUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElasticSearchUtilTest {


    private ElasticSearchUtil elasticSearchUtil;

    private ElasticSearchUtil inValidElasticsearchUtil;


    @BeforeAll
    void setup() throws Exception {
        elasticSearchUtil = new ElasticSearchUtil("localhost", 9200);
        inValidElasticsearchUtil = new ElasticSearchUtil("elastichost", 9333);
    }

    @Test
    public void HealthsuccessTest(){
        boolean isValid =  elasticSearchUtil.isHealthy();
        assertTrue(isValid);

    }

    @Test
    public void HealthFailTest(){
        boolean isValid = inValidElasticsearchUtil.isHealthy();
        assertTrue(isValid);
    }


}
