package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Maid;

public class RatingFilter implements MaidFilter {
    private final double minRating;

    public RatingFilter(double minRating) {
        this.minRating = minRating;
    }

    @Override
    public boolean matches(Maid maid) {
        return maid.getRating() >= minRating;
    }
}
