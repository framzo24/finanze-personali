package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.CreateMovimentoRequest;
import it.francescozoni.finanzepersonali.dto.MovimentoFilter;
import it.francescozoni.finanzepersonali.dto.MovimentoResponse;
import it.francescozoni.finanzepersonali.dto.MovimentiPageResponse;
import it.francescozoni.finanzepersonali.dto.UpdateMovimentoRequest;
import it.francescozoni.finanzepersonali.mapper.MovimentoMapper;
import it.francescozoni.finanzepersonali.model.Movimento; // adatta il package
import it.francescozoni.finanzepersonali.service.MovimentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentoControllerTest {

    @Mock
    private MovimentoService movimentoService;

    @Mock
    private MovimentoMapper movimentoMapper;

    @InjectMocks
    private MovimentoController controller;

    @Mock
    private Movimento movimento;
    @Mock
    private MovimentoResponse movimentoResponse;
    @Mock
    private MovimentiPageResponse pageResponse;
    @Mock
    private MovimentoFilter filter;
    @Mock
    private CreateMovimentoRequest createRequest;
    @Mock
    private UpdateMovimentoRequest updateRequest;

    @Nested
    @DisplayName("cerca(filter, pagina, dimensione)")
    class Cerca {

        @Test
        void delegaAlServiceERestituisceLaPagina() {
            when(movimentoService.cerca(filter, 2, 50)).thenReturn(pageResponse);

            MovimentiPageResponse result = controller.cerca(filter, 2, 50);

            assertThat(result).isSameAs(pageResponse);
            verify(movimentoService).cerca(filter, 2, 50);
            verifyNoMoreInteractions(movimentoService);
            verifyNoInteractions(movimentoMapper);
        }

        @Test
        void propagaLEccezioneDelService() {
            when(movimentoService.cerca(filter, 0, 20))
                    .thenThrow(new IllegalArgumentException("filtro non valido"));

            assertThatThrownBy(() -> controller.cerca(filter, 0, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filtro non valido");
        }
    }

    @Nested
    @DisplayName("trovaPagina(pagina, dimensione)")
    class TrovaPagina {

        @Test
        void delegaAlServiceERestituisceLaPagina() {
            when(movimentoService.trovaPaginati(1, 10)).thenReturn(pageResponse);

            MovimentiPageResponse result = controller.trovaPagina(1, 10);

            assertThat(result).isSameAs(pageResponse);
            verify(movimentoService).trovaPaginati(1, 10);
            verifyNoMoreInteractions(movimentoService);
            verifyNoInteractions(movimentoMapper);
        }
    }

    @Nested
    @DisplayName("trovaUno(id)")
    class TrovaUno {

        @Test
        void restituisceLaRispostaProdottaDalMapper() {
            when(movimentoService.trova(1L)).thenReturn(movimento);
            when(movimentoMapper.toResponse(movimento)).thenReturn(movimentoResponse);

            MovimentoResponse result = controller.trovaUno(1L);

            assertThat(result).isSameAs(movimentoResponse);
            verify(movimentoService).trova(1L);
            verify(movimentoMapper).toResponse(movimento);
            verifyNoMoreInteractions(movimentoService, movimentoMapper);
        }

        @Test
        void propagaLEccezioneEInvocaIlMapperSoloSeIlServiceRiesce() {
            when(movimentoService.trova(99L)).thenThrow(new RuntimeException("non trovato"));

            assertThatThrownBy(() -> controller.trovaUno(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("non trovato");

            verifyNoInteractions(movimentoMapper);
        }
    }

    @Nested
    @DisplayName("crea(input)")
    class Crea {

        @Test
        void creaEMappaLaRisposta() {
            when(movimentoService.aggiungi(createRequest)).thenReturn(movimento);
            when(movimentoMapper.toResponse(movimento)).thenReturn(movimentoResponse);

            MovimentoResponse result = controller.crea(createRequest);

            assertThat(result).isSameAs(movimentoResponse);
            verify(movimentoService).aggiungi(createRequest);
            verify(movimentoMapper).toResponse(movimento);
            verifyNoMoreInteractions(movimentoService, movimentoMapper);
        }

        @Test
        void propagaLEccezioneDelService() {
            when(movimentoService.aggiungi(createRequest))
                    .thenThrow(new IllegalStateException("categoria inesistente"));

            assertThatThrownBy(() -> controller.crea(createRequest))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(movimentoMapper);
        }
    }

    @Nested
    @DisplayName("modifica(id, input)")
    class Modifica {

        @Test
        void modificaEMappaLaRisposta() {
            when(movimentoService.modifica(5L, updateRequest)).thenReturn(movimento);
            when(movimentoMapper.toResponse(movimento)).thenReturn(movimentoResponse);

            MovimentoResponse result = controller.modifica(5L, updateRequest);

            assertThat(result).isSameAs(movimentoResponse);
            verify(movimentoService).modifica(5L, updateRequest);
            verify(movimentoMapper).toResponse(movimento);
            verifyNoMoreInteractions(movimentoService, movimentoMapper);
        }

        @Test
        void propagaLEccezioneDelService() {
            when(movimentoService.modifica(5L, updateRequest))
                    .thenThrow(new RuntimeException("errore"));

            assertThatThrownBy(() -> controller.modifica(5L, updateRequest))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(movimentoMapper);
        }
    }

    @Nested
    @DisplayName("elimina(id)")
    class Elimina {

        @Test
        void delegaAlService() {
            controller.elimina(3L);

            verify(movimentoService).elimina(3L);
            verifyNoMoreInteractions(movimentoService);
            verifyNoInteractions(movimentoMapper);
        }

        @Test
        void propagaLEccezioneDelService() {
            doThrow(new IllegalStateException("errore"))
                    .when(movimentoService).elimina(3L);

            assertThatThrownBy(() -> controller.elimina(3L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}