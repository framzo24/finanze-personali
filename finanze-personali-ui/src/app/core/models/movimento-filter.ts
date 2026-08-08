export interface MovimentoFilter {

  descrizione?: string;

  categoriaId?: number;

  tipo?: string;

  dataDa?: string;

  dataA?: string;

  importoMin?: number;

  importoMax?: number;

}
