package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Maid;

public class LocalityFilter implements MaidFilter {
    private final String locality;

    public LocalityFilter(String locality) {
        this.locality = locality;
    }

    @Override
    public boolean matches(Maid maid) {
        return maid.getLocality() != null && maid.getLocality().equalsIgnoreCase(locality);
    }
}
