package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Maid;

public interface MaidFilter {
    boolean matches(Maid maid);
}
