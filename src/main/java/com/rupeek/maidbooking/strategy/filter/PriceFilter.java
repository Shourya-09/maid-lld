package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Maid;

public class PriceFilter implements MaidFilter {
    private final double maxHourlyRate;

    public PriceFilter(double maxHourlyRate) {
        this.maxHourlyRate = maxHourlyRate;
    }

    @Override
    public boolean matches(Maid maid) {
        return maid.getHourlyRate() <= maxHourlyRate;
    }
}
