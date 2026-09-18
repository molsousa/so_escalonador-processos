/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador;

import escalonador.algoritmos.MPP;
import escalonador.algoritmos.MultiplasFilas;
import escalonador.algoritmos.RoundRobin;
import escalonador.arquivo.CarregarArquivoCSV;
import escalonador.utils.Processo;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Classe principal do simulador de escalonamento de processos.<br>
 * Responsável por carregar o arquivo CSV de entrada e disparar a execução dos
 * três algoritmos implementados: Round-Robin, Múltiplas Filas e MPP.<br>
 * Cada algoritmo recebe uma cópia independente da lista de processos, para
 * evitar que o estado alterado por um interfira nos demais.<br>
 * Ao final da execução de cada algoritmo, são gerados arquivos CSV contendo o
 * diagrama de Gantt, compacto e em matriz, e as métricas obrigatórias (tempo de
 * retorno, espera, resposta e trocas de contexto).<br>
 * Objetivo: comparar o desempenho dos três algoritmos sob a mesma carga de
 * processos, permitindo análise de tempo médio de espera, resposta, retorno e
 * risco de inanição.<br>
 * Vantagem: centraliza a execução e a geração de artefatos em um único ponto,
 * facilitando a reprodução dos experimentos.<br>
 * Desvantagem: executa os algoritmos de forma sequencial, o que pode tornar a
 * execução longa com muitos processos.<br>
 *
 * @author molsousa
 */
public class Main {

    /**
     * Método principal do simulador.<br>
     * Carrega o arquivo CSV de entrada, cria cópias independentes da lista de
     * processos para cada algoritmo e executa, em sequência, o Round-Robin, o
     * Múltiplas Filas e o MPP.<br>
     * Ao final, cada algoritmo gera seus próprios arquivos CSV de Gantt
     * (compacto e matriz) e de métricas.<br>
     * A separação em cópias evita que o estado alterado por um algoritmo
     * interfira na execução dos demais.<br>
     *
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {

        // Carregar arquivo
        String arquivo = "entrada.csv";
        List<Processo> base = CarregarArquivoCSV.carregarArquivoCSV(arquivo);

        if (base == null || base.isEmpty()) {
            System.err.println("Nenhum processo carregado. Verifique o arquivo: " + arquivo);
            return;
        }

        System.out.println("Processos carregados: " + base.size());

        // Cada algoritmo recebe uma cópia.
        RoundRobin rr = new RoundRobin(copiar(base), 4);
        MultiplasFilas mf = new MultiplasFilas(copiar(base));
        MPP mpp = new MPP(copiar(base), 4, 16);

        System.out.println("\n################ ROUND ROBIN ################");
        rr.executar();

        System.out.println("\n################ MÚLTIPLAS FILAS ################");
        mf.executar();

        System.out.println("\n################ MPP ################");
        mpp.executar();

        System.out.println("\nArquivos gerados:");
        System.out.println("  gantt_rr.csv / gantt_rr_matriz.csv / metricas_rr.csv");
        System.out.println("  gantt_mf.csv / gantt_mf_matriz.csv / metricas_mf.csv");
        System.out.println("  gantt_mpp.csv / gantt_mpp_matriz.csv / metricas_mpp.csv");
    }

    /**
     * Cópia com reset de estado, evita que um algoritmo contamine o outro.
     */
    private static List<Processo> copiar(List<Processo> origem) {
        List<Processo> copia = new java.util.ArrayList<>();
        for (Processo p : origem) {
            Processo n = new Processo();
            n.setPid(p.getPid());
            n.setNomeProcesso(p.getNomeProcesso());
            n.setTempoChegada(p.getTempoChegada());
            n.setTempoTotalCPU(p.getTempoTotalCPU());
            n.setPrioridade(p.getPrioridade());
            n.setTipoProcesso(p.getTipoProcesso());
            n.setOperacaoES(p.isOperacaoES());
            n.setProbabilidadeES(p.getProbabilidadeES());
            n.setMediaES(p.getMediaES());
            n.setDuracaoES(p.getDuracaoES());
            n.setFilaSugerida(p.getFilaSugerida());
            n.setQuantumSugerido(p.getQuantumSugerido());
            n.setDescricao(p.getDescricao());
            copia.add(n);
        }
        return copia;
    }
}
