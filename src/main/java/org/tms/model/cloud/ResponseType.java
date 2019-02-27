package org.tms.model.cloud;

import java.util.stream.Stream;

public enum ResponseType {
    CREATED("Created"),
    BAD_REQUEST("Bad Request"),
    ERROR("Error");

    private final String responseType;

    ResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }

    public static Stream<ResponseType> stream() {
        return Stream.of(ResponseType.values());
    }

    public static ResponseType fromValue(String period) {
        for (ResponseType responseType : ResponseType.values()) {
            if (responseType.getResponseType() == period) {
                return responseType;
            }
        }
        return ResponseType.ERROR;
    }
}
