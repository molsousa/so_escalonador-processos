/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.arquivo;

import com.opencsv.CSVReader;
import escalonador.utils.Processo;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Responsável por carregar a lista de processos a partir de um arquivo CSV.<br>
 * O mapeamento das colunas é feito dinamicamente pela leitura do cabeçalho, de
 * modo que a ordem das colunas no arquivo pode ser alterada sem quebrar o
 * parser.<br>
 * Utiliza try-with-resources para garantir o fechamento do arquivo e lê o
 * conteúdo em UTF-8.<br>
 * Objetivo: transformar cada linha do CSV em um objeto Processo, pronto para
 * ser executado pelos algoritmos de escalonamento.<br>
 *
 * @author molsousa
 */
public class CarregarArquivoCSV {

    /**
     * Carrega a lista de processos a partir de um arquivo CSV.<br>
     * O mapeamento das colunas é feito dinamicamente pela leitura do cabeçalho,
     * permitindo que a ordem das colunas seja alterada sem quebrar o
     * parser.<br>
     * Linhas vazias são ignoradas; valores booleanos aceitos nos formatos "1",
     * "true", "sim" e "s".<br>
     * Em caso de erro, retorna uma lista vazia e registra a mensagem no fluxo
     * de erro padrão.<br>
     *
     * @param nomeArquivo Caminho do arquivo CSV de entrada.
     * @return Lista de processos carregados; vazia em caso de falha.
     */
    public static List<Processo> carregarArquivoCSV(String nomeArquivo) {
        List<Processo> dadosCSV = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new FileInputStream(nomeArquivo), StandardCharsets.UTF_8))) {

            String[] header = reader.readNext();
            if (header == null) {
                return dadosCSV;
            }

            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                idx.put(header[i].trim(), i);
            }

            String[] str;
            while ((str = reader.readNext()) != null) {
                if (str.length == 0 || str[0].trim().isEmpty()) {
                    continue;
                }

                Processo p = new Processo();
                p.setPid(get(str, idx, "pid"));
                p.setNomeProcesso(get(str, idx, "nome_processo"));
                p.setTempoChegada(Integer.parseInt(get(str, idx, "tempo_chegada")));
                p.setTempoTotalCPU(Integer.parseInt(get(str, idx, "tempo_cpu_total")));
                p.setPrioridade(Integer.parseInt(get(str, idx, "prioridade")));
                p.setTipoProcesso(get(str, idx, "tipo_processo"));
                p.setOperacaoES(parseBoolean(get(str, idx, "operacao_es")));
                p.setProbabilidadeES(Float.parseFloat(get(str, idx, "probabilidade_es")));
                p.setMediaES(Float.parseFloat(get(str, idx, "media_es")));
                p.setDuracaoES(Integer.parseInt(get(str, idx, "duracao_es")));
                p.setFilaSugerida(Integer.parseInt(get(str, idx, "fila_sugerida")));
                p.setQuantumSugerido(Integer.parseInt(get(str, idx, "quantum_sugerido")));
                p.setDescricao(get(str, idx, "descricao"));

                dadosCSV.add(p);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSV: " + e.getMessage());
        }
        return dadosCSV;
    }

    private static String get(String[] str, Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        if (i == null || i >= str.length) {
            return "";
        }
        return str[i].trim();
    }

    private static boolean parseBoolean(String s) {
        if (s == null) {
            return false;
        }
        s = s.trim().toLowerCase();
        return s.equals("1") || s.equals("true") || s.equals("sim") || s.equals("s");
    }
}
