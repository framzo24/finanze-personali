package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Movimento;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryMovimentoRepository implements MovimentoRepository {

    private final List<Movimento> movimenti = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Movimento save(Movimento movimento) {
        movimento.setId(sequence.getAndIncrement());
        movimenti.add(movimento);
        return movimento;
    }

    @Override
    public Movimento update(Movimento movimento) {
        delete(movimento.getId());
        movimenti.add(movimento);
        return movimento;
    }

    @Override
    public boolean delete(Long id) {
        return movimenti.removeIf(m -> m.getId().equals(id));
    }

    @Override
    public Optional<Movimento> findById(Long id) {
        return movimenti.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Movimento> findAll() {
        return List.copyOf(movimenti);
    }

    @Override
    public boolean existsById(Long id) {
        return movimenti.stream()
                .anyMatch(m -> m.getId().equals(id));
    }
}
