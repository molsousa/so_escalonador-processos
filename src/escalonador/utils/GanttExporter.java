/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utilitário para registro e exportação do diagrama de Gantt e das métricas
 * obrigatórias do trabalho.<br>
 * Mantém uma lista de blocos contínuos de execução (inicio, fim, processo),
 * onde processo == null representa CPU ociosa.<br>
 * Oferece três formatos de exportação: CSV compacto (blocos contínuos), CSV em
 * matriz (pid x tempo, ideal para visualização em Excel com formatação
 * condicional) e CSV de métricas por processo com médias e total de trocas de
 * contexto.<br>
 * Objetivo: ser agnóstico de algoritmo, permitindo que Round-Robin, Múltiplas
 * Filas e MPP reutilizem o mesmo código de coleta e exportação.<br>
 * Vantagem: centraliza a lógica de escrita de CSV, garante codificação UTF-8
 * (para acentos em nomes de processos) e evita duplicação entre os três
 * escalonadores.<br>
 * Desvantagem: como é estático, mantém o estado do Gantt nas listas do
 * chamador; não há proteção contra uso concorrente.<br>
 *
 * @author molsousa
 */
public class GanttExporter {

    public static class Bloco {

        public int inicio;
        public int fim;
        public Processo processo;

        public Bloco(int inicio, int fim, Processo processo) {
            this.inicio = inicio;
            this.fim = fim;
            this.processo = processo;
        }
    }

    /**
     * Registra uma unidade de tempo no Gantt.<br>
     * Se o bloco anterior terminar exatamente no instante atual e for do mesmo
     * processo, o bloco é estendido em vez de criar um novo.<br>
     *
     * @param blocos Lista acumuladora de blocos do Gantt.
     * @param t Instante de tempo atual.
     * @param p Processo em execução; null representa CPU ociosa.
     */
    public static void registrarBloco(List<Bloco> blocos, int t, Processo p) {
        registrarIntervalo(blocos, t, t + 1, p);
    }

    /**
     * Registra um intervalo contínuo [inicio, fim) no Gantt.<br>
     * Usado principalmente para representar períodos de CPU ociosa, em que
     * vários tiques são saltados de uma só vez.<br>
     *
     * @param blocos Lista acumuladora de blocos do Gantt.
     * @param inicio Instante inicial do intervalo (inclusivo).
     * @param fim Instante final do intervalo (exclusivo).
     * @param p Processo em execução; null representa CPU ociosa.
     */
    public static void registrarIntervalo(List<Bloco> blocos, int inicio, int fim, Processo p) {
        if (inicio >= fim) {
            return;
        }
        if (!blocos.isEmpty()) {
            Bloco ultimo = blocos.get(blocos.size() - 1);
            if (ultimo.fim == inicio && ultimo.processo == p) {
                ultimo.fim = fim;
                return;
            }
        }
        blocos.add(new Bloco(inicio, fim, p));
    }

    /**
     * Exporta o Gantt no formato compacto (blocos contínuos) para um arquivo
     * CSV em UTF-8.<br>
     * Colunas: tempo_inicio, tempo_fim, duracao, pid, nome_processo, tipo.<br>
     *
     * @param caminho Caminho do arquivo CSV de saída.
     * @param blocos Lista de blocos do Gantt.
     */
    public static void exportarCSV(String caminho, List<Bloco> blocos) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(caminho), StandardCharsets.UTF_8))) {
            pw.println("tempo_inicio,tempo_fim,duracao,pid,nome_processo,tipo");
            for (Bloco b : blocos) {
                String pid = "-", nome = "-", tipo = "-";
                if (b.processo != null) {
                    pid = b.processo.getPid();
                    nome = b.processo.getNomeProcesso();
                    tipo = b.processo.getTipoProcesso();
                }
                pw.printf("%d,%d,%d,%s,%s,%s%n",
                        b.inicio, b.fim, (b.fim - b.inicio), pid, nome, tipo);
            }
            System.out.println("Gantt exportado: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao exportar Gantt: " + e.getMessage());
        }
    }

    /**
     * Exporta o Gantt em formato de matriz (pid x tempo).<br>
     * Cada célula recebe "X" quando o processo executa naquele instante,
     * ficando vazia caso contrário. Ideal para visualização no Excel com
     * formatação condicional.<br>
     *
     * @param caminho Caminho do arquivo CSV de saída.
     * @param blocos Lista de blocos do Gantt.
     * @param processos Lista de processos (para preservar a ordem das linhas).
     * @param tempoMax Instante máximo a ser considerado nas colunas.
     */
    public static void exportarMatrizCSV(String caminho, List<Bloco> blocos,
            List<Processo> processos, int tempoMax) {
        Map<String, Set<Integer>> exec = new LinkedHashMap<>();
        for (Processo p : processos) {
            exec.put(p.getPid(), new TreeSet<>());
        }
        for (Bloco b : blocos) {
            if (b.processo == null) {
                continue;
            }
            Set<Integer> s = exec.get(b.processo.getPid());
            if (s != null) {
                for (int t = b.inicio; t < b.fim; t++) {
                    s.add(t);
                }
            }
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(caminho), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder("pid,nome,tipo");
            for (int t = 0; t < tempoMax; t++) {
                sb.append(",").append(t);
            }
            pw.println(sb);
            for (Processo p : processos) {
                sb = new StringBuilder();
                sb.append(p.getPid()).append(",")
                        .append(p.getNomeProcesso()).append(",")
                        .append(p.getTipoProcesso());
                Set<Integer> s = exec.get(p.getPid());
                for (int t = 0; t < tempoMax; t++) {
                    sb.append(",").append(s.contains(t) ? "X" : "");
                }
                pw.println(sb);
            }
            System.out.println("Gantt (matriz) exportado: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao exportar Gantt matriz: " + e.getMessage());
        }
    }

    /**
     * Exporta as métricas obrigatórias por processo e as médias gerais.<br>
     * Colunas: pid, nome, tipo, chegada, inicio_cpu, conclusao, retorno,
     * espera, resposta.<br>
     * Ao final, acrescenta uma linha MEDIA com as médias de retorno, espera e
     * resposta, e uma linha TROCAS_CONTEXTO_TOTAL com o total de trocas de
     * contexto do algoritmo.<br>
     *
     * @param caminho Caminho do arquivo CSV de saída.
     * @param processos Lista de processos já finalizados.
     * @param trocasContexto Total de trocas de contexto contabilizadas.
     */
    public static void exportarMetricasCSV(String caminho, List<Processo> processos, int trocasContexto) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(caminho), StandardCharsets.UTF_8))) {
            pw.println("pid,nome,tipo,chegada,inicio_cpu,conclusao,retorno,espera,resposta");
            double somaRet = 0, somaEsp = 0, somaResp = 0;
            for (Processo p : processos) {
                int retorno = p.getTempoConclusao() - p.getTempoChegada();
                int espera = retorno - p.getTempoTotalCPU() - p.getTempoTotalES();
                int resposta = p.getTempoInicioCPU() - p.getTempoChegada();
                somaRet += retorno;
                somaEsp += espera;
                somaResp += resposta;
                pw.printf("%s,%s,%s,%d,%d,%d,%d,%d,%d%n",
                        p.getPid(), p.getNomeProcesso(), p.getTipoProcesso(),
                        p.getTempoChegada(), p.getTempoInicioCPU(), p.getTempoConclusao(),
                        retorno, espera, resposta);
            }
            int n = processos.size();
            pw.printf("MEDIA,-,-,-,-,-,%.2f,%.2f,%.2f%n",
                    somaRet / n, somaEsp / n, somaResp / n);
            pw.printf("TROCAS_CONTEXTO_TOTAL,%d,,,,,,,%n", trocasContexto);
            System.out.println("Métricas exportadas: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao exportar métricas: " + e.getMessage());
        }
    }
}
