package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.CategoriaResponse;
import it.francescozoni.finanzepersonali.dto.CreateCategoriaRequest;
import it.francescozoni.finanzepersonali.dto.UpdateCategoriaRequest;
import it.francescozoni.finanzepersonali.model.enums.AzioneEliminazioneCategoria;
import it.francescozoni.finanzepersonali.service.CategoriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    private static final AzioneEliminazioneCategoria AZIONE =
            AzioneEliminazioneCategoria.values()[0];

    @Mock
    private CategoriaService service;

    @InjectMocks
    private CategoriaController controller;

    @Mock
    private CategoriaResponse risposta1;
    @Mock
    private CategoriaResponse risposta2;
    @Mock
    private CreateCategoriaRequest createRequest;
    @Mock
    private UpdateCategoriaRequest updateRequest;

    @Nested
    @DisplayName("tutte()")
    class Tutte {

        @Test
        void restituisceLaListaDelService() {
            when(service.trovaTutte()).thenReturn(List.of(risposta1, risposta2));

            List<CategoriaResponse> result = controller.tutte();

            assertThat(result).containsExactly(risposta1, risposta2);
            verify(service).trovaTutte();
            verifyNoMoreInteractions(service);
        }

        @Test
        void restituisceListaVuota() {
            when(service.trovaTutte()).thenReturn(List.of());

            assertThat(controller.tutte()).isEmpty();
        }

        @Test
        void propagaLEccezioneDelService() {
            when(service.trovaTutte()).thenThrow(new IllegalStateException("errore"));

            assertThatThrownBy(() -> controller.tutte())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("errore");
        }
    }

    @Nested
    @DisplayName("tuttePiatte()")
    class TuttePiatte {

        @Test
        void restituisceLaListaPiattaDelService() {
            when(service.trovaTuttePiatte()).thenReturn(List.of(risposta1, risposta2));

            List<CategoriaResponse> result = controller.tuttePiatte();

            assertThat(result).containsExactly(risposta1, risposta2);
            verify(service).trovaTuttePiatte();
            verifyNoMoreInteractions(service);
        }
    }

    @Nested
    @DisplayName("una(id)")
    class Una {

        @Test
        void restituisceLaCategoriaDelService() {
            when(service.trova(1L)).thenReturn(risposta1);

            CategoriaResponse result = controller.una(1L);

            assertThat(result).isSameAs(risposta1);
            verify(service).trova(1L);
            verifyNoMoreInteractions(service);
        }

        @Test
        void propagaLEccezioneSeNonTrovata() {
            when(service.trova(99L)).thenThrow(new RuntimeException("non trovata"));

            assertThatThrownBy(() -> controller.una(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("non trovata");
        }
    }

    @Nested
    @DisplayName("crea(request)")
    class Crea {

        @Test
        void delegaAlServiceERestituisceLaRisposta() {
            when(service.crea(createRequest)).thenReturn(risposta1);

            CategoriaResponse result = controller.crea(createRequest);

            assertThat(result).isSameAs(risposta1);
            verify(service).crea(createRequest);
            verifyNoMoreInteractions(service);
        }

        @Test
        void propagaLEccezioneDelService() {
            when(service.crea(createRequest)).thenThrow(new IllegalArgumentException("duplicata"));

            assertThatThrownBy(() -> controller.crea(createRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("modifica(id, request)")
    class Modifica {

        @Test
        void delegaAlServiceConIdERequest() {
            when(service.modifica(5L, updateRequest)).thenReturn(risposta1);

            CategoriaResponse result = controller.modifica(5L, updateRequest);

            assertThat(result).isSameAs(risposta1);
            verify(service).modifica(5L, updateRequest);
            verifyNoMoreInteractions(service);
        }

        @Test
        void propagaLEccezioneDelService() {
            when(service.modifica(5L, updateRequest)).thenThrow(new RuntimeException("errore"));

            assertThatThrownBy(() -> controller.modifica(5L, updateRequest))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("elimina(id, azione, nuovaCategoriaPadreId)")
    class Elimina {

        @Test
        void delegaAlServiceConTuttiIParametri() {
            controller.elimina(3L, AZIONE, 7L);

            verify(service).elimina(3L, AZIONE, 7L);
            verifyNoMoreInteractions(service);
        }

        @Test
        void passaNullSeNuovoPadreAssente() {
            controller.elimina(3L, AZIONE, null);

            verify(service).elimina(3L, AZIONE, null);
            verifyNoMoreInteractions(service);
        }

        @Test
        void propagaLEccezioneDelService() {
            doThrow(new IllegalStateException("ha movimenti"))
                    .when(service).elimina(3L, AZIONE, null);

            assertThatThrownBy(() -> controller.elimina(3L, AZIONE, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("ha movimenti");
        }
    }
}