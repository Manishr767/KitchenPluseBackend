package com.manish.kitchenpulse.catalog;

public record UnitResponse(Long id, String name, String symbol) {

    static UnitResponse from(Unit u) {
        return new UnitResponse(u.getId(), u.getName(), u.getSymbol());
    }
}
