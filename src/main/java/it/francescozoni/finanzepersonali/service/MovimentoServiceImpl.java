package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.exception.MovimentoNotFoundException;
import it.francescozoni.finanzepersonali.model.TipoMovimento;
import it.francescozoni.finanzepersonali.repository.MovimentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class MovimentoServiceImpl implements MovimentoService {

    private final MovimentoRepository repository;


    public MovimentoServiceImpl(MovimentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Movimento aggiungi(Movimento movimento) {
        return repository.save(movimento);
    }


    @Override
    public Movimento modifica(Movimento movimento) {
        if (!repository.existsById(movimento.getId())) {
            throw new MovimentoNotFoundException(movimento.getId());
        }
        return repository.update(movimento);
    }

    @Override
    public void elimina(Long id) {
        if (!repository.existsById(id)) {
            throw new MovimentoNotFoundException(id);
        }
        repository.delete(id);
    }

    @Override
    public Movimento trova(Long id) {
        return repository.findById(id).orElseThrow(() -> new MovimentoNotFoundException(id));
    }

    @Override
    public List<Movimento> trovaTutti() {
        return repository.findAll()
                .stream()
                .sorted(Comparator.comparing(Movimento::getData).reversed())
                .toList();
    }

    @Override
    public BigDecimal saldo() {
        return totaleEntrate().subtract(totaleUscite());
    }

    @Override
    public BigDecimal totaleEntrate() {
        return repository.findAll()
                .stream()
                .filter(m -> m.getTipo() == TipoMovimento.ENTRATA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal totaleUscite() {
        return repository.findAll()
                .stream()
                .filter(m -> m.getTipo() == TipoMovimento.USCITA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}