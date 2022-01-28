package org.swasth.apigateway.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Acl {

    private Set<String> paths = new HashSet<>();
    private List<String> regexPaths = new ArrayList<>();

}
