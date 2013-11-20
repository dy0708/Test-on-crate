package org.cratedb.sql;

import org.elasticsearch.rest.RestStatus;

public class TypeUnknownException extends CrateException {
    private final String type;

    public TypeUnknownException(String type, Throwable e) {
        super("Unkown type", e);
        this.type = type;
    }

    public TypeUnknownException(String type) {
        super("Unknown type");
        this.type = type;
    }

    @Override
    public int errorCode() {
        return 4044;
    }

    @Override
    public RestStatus status() {
        return RestStatus.BAD_REQUEST;
    }

    @Override
    public Object[] args() {
        return new Object[]{type};
    }
}