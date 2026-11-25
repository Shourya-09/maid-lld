package com.rupeek.maidbooking.repository;

import com.rupeek.maidbooking.model.maid.Maid;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMaidRepository implements MaidRepository {
    private final Map<String, Maid> store = new ConcurrentHashMap<>();

    @Override
    public Maid save(Maid maid) {
        store.put(maid.getId(), maid);
        return maid;
    }

    @Override
    public Optional<Maid> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Maid> findAll() {
        return new ArrayList<>(store.values());
    }
}
