package org.swasth.postgresql;

import org.swasth.common.exception.ClientException;

public interface IDatabaseService {

    void insert(String mid, String payload) throws ClientException;
}
