package me.mocha.backend.common.security.jwt;

public enum JwtType {
    ACCESS("access"),
    REFRESH("refresh");

    private String stringValue;

    JwtType(String stringValue) {
        this.stringValue = stringValue;
    }

    public String toString() {
        return stringValue;
    }
}
