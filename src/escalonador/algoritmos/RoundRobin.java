/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.algoritmos;

import escalonador.utils.Estado;
import escalonador.utils.GanttExporter;
import escalonador.utils.Processo;
import java.util.*;

/**
 * Algoritmo Round-Robin (RR) com quantum fixo.<br>
 * Cada processo recebe a CPU por até um quantum de tempo; se não finalizar nem
 * solicitar E/S, retorna ao final da fila de prontos.<br>
 * Objetivo: garantir justiça entre processos e tempo de resposta previsível,
 * evitando que um processo longo monopolize a CPU.<br>
 * Vantagem: simplicidade, baixo risco de inanição e bom tempo de resposta para
 * processos interativos.<br>
 * Desvantagem: quantum muito pequeno aumenta o número de trocas de contexto;
 * quantum muito grande aproxima o comportamento de FCFS. Não diferencia
 * prioridades nem tipos de processo.<br>
 * Ao final da execução, exporta o Gantt (compacto e matriz) e o CSV de métricas
 * com tempo de retorno, espera, resposta e trocas de contexto.<br>
 *
 * @author molsousa
 */
public class RoundRobin {

    private final List<Processo> processos;
    private final Queue<Processo> filaProntos;
    private final int quantum;
    private int tempoAtual;
    private boolean cpuOcupada;
    private Processo processoEmExecucao;

    private final Random random;

    private final List<GanttExporter.Bloco> ganttBlocos = new ArrayList<>();
    private int trocasContexto = 0;
    private Processo ultimoExecutado = null;

    /**
     * Classe para RoundRobin, inicializa com os processos recebidos no
     * construtor.<br>
     * Atualiza o estado dos processos para NOVO.<br>
     *
     * @param processos Lista de processos a serem executados no RoundRobin.
     * @param quantum Quantum máximo em unidades de tempo.
     */
    public RoundRobin(List<Processo> processos, int quantum) {
        filaProntos = new LinkedList<>();
        this.processos = processos;
        tempoAtual = 0;
        cpuOcupada = false;
        processoEmExecucao = null;
        this.quantum = quantum;
        random = new Random();

        for (Processo p : processos) {
            p.setEstadoProcesso(Estado.NOVO);
            p.setTempoCPURestante(p.getTempoTotalCPU());
            p.setTempoBloqueioRestante(0);
            p.setQuantumRestante(0);
            p.setTempoInicioCPU(-1);
            p.setTempoConclusao(-1);
            p.setTempoTotalES(0);
            p.setTempoEspera(0);
        }
    }

    /**
     * Método principal para execução do algoritmo Round-Robin.<br>
     * Processos que chegam no instante atual entram no final da fila de
     * prontos.<br>
     * Se a CPU estiver ociosa e houver prontos, o primeiro da fila recebe um
     * quantum fixo e passa a executar.<br>
     * Ao final de cada tique, o processo pode finalizar, sofrer E/S ou devolver
     * a CPU ao final da fila por esgotamento de quantum.<br>
     * Processos bloqueados têm seu tempo de E/S decrementado a cada unidade de
     * tempo e retornam à fila de prontos quando desbloqueiam.<br>
     * Ao final, exporta Gantt e métricas.<br>
     */
    public void executar() {
        while (true) {
            // 1 - Chegadas
            for (Processo p : processos) {
                if (p.getTempoChegada() == tempoAtual && p.getEstadoProcesso() == Estado.NOVO) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    filaProntos.add(p);
                }
            }

            // 2 - Incrementa espera dos PRONTOS
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.PRONTO) {
                    p.setTempoEspera(p.getTempoEspera() + 1);
                }
            }

            // 3 - Escalonar
            if (!cpuOcupada && !filaProntos.isEmpty()) {
                processoEmExecucao = filaProntos.poll();
                processoEmExecucao.setEstadoProcesso(Estado.EXECUTANDO);
                processoEmExecucao.setQuantumRestante(quantum);
                if (processoEmExecucao.getTempoInicioCPU() == -1) {
                    processoEmExecucao.setTempoInicioCPU(tempoAtual);
                }
                if (ultimoExecutado != null && ultimoExecutado != processoEmExecucao) {
                    trocasContexto++;
                }
                ultimoExecutado = processoEmExecucao;
                cpuOcupada = true;
            }

            // Captura quem rodou neste tique (antes de modificar estado)
            Processo pTick = cpuOcupada ? processoEmExecucao : null;

            // 4 - Processar CPU (1 unidade)
            if (cpuOcupada) {
                Processo p = processoEmExecucao;
                if (p.getTempoCPURestante() == 0) {
                    p.setEstadoProcesso(Estado.FINALIZADO);
                    p.setTempoConclusao(tempoAtual);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (p.getQuantumRestante() == 0) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    filaProntos.add(p);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (random.nextDouble() < p.getProbabilidadeES()) {
                    p.setEstadoProcesso(Estado.BLOQUEADO);
                    p.setTempoBloqueioRestante(p.getDuracaoES());
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else {
                    p.setTempoCPURestante(p.getTempoCPURestante() - 1);
                    p.setQuantumRestante(p.getQuantumRestante() - 1);
                    if (p.getTempoCPURestante() == 0) {
                        p.setEstadoProcesso(Estado.FINALIZADO);
                        p.setTempoConclusao(tempoAtual);
                        cpuOcupada = false;
                        processoEmExecucao = null;
                    }
                }
            }

            // 5 - Atualizar bloqueados (conta tempo de E/S)
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                    p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - 1);
                    p.setTempoTotalES(p.getTempoTotalES() + 1);
                    if (p.getTempoBloqueioRestante() == 0) {
                        p.setEstadoProcesso(Estado.PRONTO);
                        filaProntos.add(p);
                    }
                }
            }

            // 6 - Gantt do tique
            GanttExporter.registrarBloco(ganttBlocos, tempoAtual, pTick);

            // 7 - Exibir
            exibirEstado(tempoAtual, processos, filaProntos);

            tempoAtual++;

            boolean existeAtivo = false;
            for (Processo p : processos) {
                if (p.getEstadoProcesso() != Estado.FINALIZADO) {
                    existeAtivo = true;
                    break;
                }
            }
            if (!existeAtivo) {
                break;
            }
        }

        exportar("rr");
    }

    private void exportar(String prefixo) {
        GanttExporter.exportarCSV("gantt_" + prefixo + ".csv", ganttBlocos);
        GanttExporter.exportarMatrizCSV("gantt_" + prefixo + "_matriz.csv", ganttBlocos, processos, tempoAtual);
        GanttExporter.exportarMetricasCSV("metricas_" + prefixo + ".csv", processos, trocasContexto);
    }

    /**
     * Exibe no console o estado de todos os processos e a fila de prontos no
     * instante atual.<br>
     * Utilizado para depuração e para acompanhamento passo a passo da
     * simulação.<br>
     *
     * @param tempoAtual Tempo atual de CPU.
     * @param processos Todos os processos da simulação.
     * @param fila_prontos Fila de processos prontos para executar.
     */
    public void exibirEstado(int tempoAtual, List<Processo> processos, Queue<Processo> fila_prontos) {
        System.out.println("Tempo: " + tempoAtual);
        for (Processo p : processos) {
            System.out.println(p.getPid() + " | " + p.getNomeProcesso() + " | " + p.getEstadoProcesso()
                    + " | " + p.getTempoCPURestante());
        }
        System.out.println();
        System.out.print("Fila de prontos: ");
        for (Processo p : fila_prontos) {
            System.out.print("|" + p.getPid() + "|->");
        }
        System.out.println("null\n");
    }

    /**
     * Verifica se há processos prontos ou em execução no algoritmo.<br>
     * Usado pelo Múltiplas Filas para decidir se a fila ainda tem trabalho a
     * realizar.<br>
     *
     * @return true se houver processos na fila de prontos ou em execução.
     */
    public boolean temProcessosProntos() {
        return (!filaProntos.isEmpty() || cpuOcupada);
    }

    /**
     * Adiciona um processo ao final da fila de prontos, ajustando seu estado
     * para PRONTO.<br>
     * Utilizado pelo Múltiplas Filas para reinserir processos devolvidos à
     * fila.<br>
     *
     * @param processo Processo a ser adicionado.
     */
    public void adicionarProcesso(Processo processo) {
        processo.setEstadoProcesso(Estado.PRONTO);
        filaProntos.add(processo);
    }

    /**
     * Remove e retorna o próximo processo da fila de prontos.<br>
     * Retorna null se a fila estiver vazia.<br>
     *
     * @return Próximo processo a executar, ou null.
     */
    public Processo proximoProcesso() {
        return filaProntos.poll();
    }

    /**
     * Retorna o quantum fixo configurado para esta instância.<br>
     * Usado pelo Múltiplas Filas para saber o quantum de cada fila.<br>
     *
     * @return Quantum em unidades de tempo.
     */
    public int getQuantum() {
        return quantum;
    }

    /**
     * Retorna o processo atualmente em execução.<br>
     *
     * @return Processo em execução, ou null se a CPU estiver ociosa.
     */
    public Processo getProcessoEmExecucao() {
        return processoEmExecucao;
    }

    /**
     * Define o processo em execução e ajusta o estado interno da CPU.<br>
     * Ao passar null, marca a CPU como ociosa.<br>
     *
     * @param processo Processo a executar, ou null para liberar a CPU.
     */
    public void setProcessoEmExecucao(Processo processo) {
        this.processoEmExecucao = processo;
        cpuOcupada = (processo != null);
    }

    /**
     * Exibe no console apenas os PIDs dos processos presentes na fila de
     * prontos.<br>
     * Usado pelo Múltiplas Filas para impressão compacta do estado das
     * filas.<br>
     */
    public void exibirFila() {
        for (Processo p : filaProntos) {
            System.out.print("|" + p.getPid() + "|->");
        }
        System.out.println("null");
    }
}
