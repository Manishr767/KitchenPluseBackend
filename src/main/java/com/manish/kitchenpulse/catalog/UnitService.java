package com.manish.kitchenpulse.catalog;

import java.util.List;

import com.manish.kitchenpulse.common.ConflictException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UnitService {

    private final UnitRepository units;

    UnitService(UnitRepository units) {
        this.units = units;
    }

    public List<UnitResponse> list() {
        return units.findAllByOrderByNameAsc().stream().map(UnitResponse::from).toList();
    }

    @Transactional
    public UnitResponse create(CreateUnitRequest request) {
        String name = request.name().trim();
        String symbol = request.symbol().trim();
        if (units.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Unit '" + name + "' already exists");
        }
        if (units.existsBySymbolIgnoreCase(symbol)) {
            throw new ConflictException("Unit symbol '" + symbol + "' already exists");
        }
        return UnitResponse.from(units.saveAndFlush(new Unit(name, symbol)));
    }
}
