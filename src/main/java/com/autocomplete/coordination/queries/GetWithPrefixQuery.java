package com.autocomplete.coordination.queries;

import io.atomix.copycat.Query;

public class GetWithPrefixQuery implements Query<String[]> {
    private final String prefix;

    public GetWithPrefixQuery(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
