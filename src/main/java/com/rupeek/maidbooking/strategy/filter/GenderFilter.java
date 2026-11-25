package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;

public class GenderFilter implements MaidFilter {
    private final Gender gender;

    public GenderFilter(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean matches(Maid maid) {
        return maid.getGender() == gender;
    }
}
