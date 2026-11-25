package com.rupeek.maidbooking.strategy.filter;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.ServiceType;

public class SkillFilter implements MaidFilter {
    private final ServiceType skill;

    public SkillFilter(ServiceType skill) {
        this.skill = skill;
    }

    @Override
    public boolean matches(Maid maid) {
        return maid.getSkills() != null && maid.getSkills().contains(skill);
    }
}
