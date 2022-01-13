package org.swasth.dp.search.beans;

import java.io.Serializable;
import java.util.Map;

public class SearchResponse implements Serializable {
    private int count;
    private Map<String,Object> entity_counts;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Map<String, Object> getEntity_counts() {
        return entity_counts;
    }

    public void setEntity_counts(Map<String, Object> entity_counts) {
        this.entity_counts = entity_counts;
    }
}
