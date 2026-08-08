export interface CategoriaResponse {
  id: number;

  nome: string;

  icona: string;

  colore: string;

  categoriaPadreId?: number | null;

  categoriaPadreNome?: string | null;

  sottoCategorie: CategoriaResponse[];
}
