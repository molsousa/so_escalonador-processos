/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.algoritmos;

import escalonador.utils.Estado;
import escalonador.utils.Processo;
import java.util.*;

/**
 * Algoritmo Menor Prioridade Primeiro (MPP).<br>
 * Começa por processos de menor prioridade.<br>
 * Objetivo: tratar processos mais pesados de io/cpu-bound e batch para
 * equivalência de processos.<br>
 * Vantagem: Inicia processos mais pesados e já acelera processos de E/S.<br>
 * Desvantagem: Processos mais leves executados depois. Risco de inanição de
 * processos de maior prioridade<br>
 * Novo processo categorizado como mais pesado vai ao início, mais leve vai ao
 * final. Logo deve fazer uso de LinkedList. <br>
 *
 * @author molsousa
 */
public class MPP {

    private List<Processo> processos;
    private int quantum;
    private int quantumEspecial;
    private Processo processoEmExecucao;
    private boolean cpuOcupada;
    private int tempoAtual;
    private List<Processo> listaProntos;

    private final Random random;

    /**
     * Construtor para inicializar algoritmo.
     *
     * @param processos Inicializa atributo de lista de processos.
     * @param quantum Inicializa o quantum geral.
     * @param quantumEspecial Inicializa o quantum para processos de menor
     * prioridade.
     */
    public MPP(List<Processo> processos, int quantum, int quantumEspecial) {
        this.processos = processos;
        this.quantum = quantum;
        this.quantumEspecial = quantumEspecial;
        this.processoEmExecucao = null;
        this.cpuOcupada = false;
        this.tempoAtual = 0;

        random = new Random();

        listaProntos = new ArrayList<>();

        for (Processo processo : processos) {
            processo.setEstadoProcesso(Estado.NOVO);
            processo.setTempoCPURestante(processo.getTempoTotalCPU());
            processo.setTempoBloqueioRestante(0);
            processo.setQuantumRestante(0);
        }
    }

    /**
     * Método principal para execução do algoritmo.<br>
     * Parecida com a lógica de execução do Round Robin.<br>
     * Processos de cpu_bound, io_bound e batch são colocados no topo da lista
     * de processos prontos.<br>
     * Outros processos que não estejam nessas categorias são colocados ao final
     * da lista, isto é, de prioridade média ou alta.<br>
     */
    public void executar() {
        while (true) {
            for (Processo processo : processos) {
                if (processo.getTempoChegada() == tempoAtual && processo.getEstadoProcesso() == Estado.NOVO) {
                    processo.setEstadoProcesso(Estado.PRONTO);

                    if (processo.getTipoProcesso().equals("cpu_bound") || processo.getTipoProcesso().equals("batch")
                            || processo.getTipoProcesso().equals("io_bound")) {
                        listaProntos.add(0, processo);
                    } else {
                        listaProntos.add(processo);
                    }
                }
            }

            if (!cpuOcupada && !listaProntos.isEmpty()) {
                processoEmExecucao = listaProntos.remove(0);
                processoEmExecucao.setEstadoProcesso(Estado.EXECUTANDO);

                if (processoEmExecucao.getTipoProcesso().equals("cpu_bound") || processoEmExecucao.getTipoProcesso().equals("batch")
                        || processoEmExecucao.getTipoProcesso().equals("io_bound")) {
                    processoEmExecucao.setQuantumRestante(quantumEspecial);
                } else {
                    processoEmExecucao.setQuantumRestante(quantum);
                }

                cpuOcupada = true;
            }

            if (cpuOcupada) {
                Processo processo = processoEmExecucao;

                if (processo.getTempoCPURestante() == 0) {
                    processo.setEstadoProcesso(Estado.FINALIZADO);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (processo.getQuantumRestante() == 0) {
                    processo.setEstadoProcesso(Estado.PRONTO);
                    listaProntos.add(processo);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (random.nextDouble() < processo.getProbabilidadeES()) {
                    processo.setEstadoProcesso(Estado.BLOQUEADO);
                    processo.setTempoBloqueioRestante(processo.getDuracaoES());
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else {
                    processo.setTempoCPURestante(processo.getTempoCPURestante() - 1);
                    processo.setQuantumRestante(processo.getQuantumRestante() - 1);
                    if (processo.getTempoCPURestante() == 0) {
                        processo.setEstadoProcesso(Estado.FINALIZADO);
                        cpuOcupada = false;
                        processoEmExecucao = null;
                    }
                }
            }

            for (Processo processo : processos) {
                if (processo.getEstadoProcesso() == Estado.BLOQUEADO) {
                    processo.setTempoBloqueioRestante(processo.getTempoBloqueioRestante() - 1);
                    if (processo.getTempoBloqueioRestante() == 0) {
                        processo.setEstadoProcesso(Estado.PRONTO);
                        if (processo.getTipoProcesso().equals("cpu_bound") || processo.getTipoProcesso().equals("batch")
                                || processo.getTipoProcesso().equals("io_bound")) {
                            listaProntos.add(0, processo);
                        } else {
                            listaProntos.add(processo);
                        }
                    }
                }
            }

            exibirEstado(tempoAtual, processos, listaProntos);
            tempoAtual++;

            boolean existeAtivo = false;
            for (Processo processo : processos) {
                if (processo.getEstadoProcesso() != Estado.FINALIZADO) {
                    existeAtivo = true;
                    break;
                }
            }

            if (!existeAtivo) {
                break;
            }
        }
    }

    /**
     * Método para exibir estados de execução na tela.
     *
     * @param tempoAtual Tempo atual de CPU.
     * @param processos Todos os processos.
     * @param listaProntos Fila de processos prontos para executar.
     */
    public void exibirEstado(int tempoAtual, List<Processo> processos, List<Processo> listaProntos) {
        System.out.println("Tempo: " + tempoAtual);

        for (Processo processo : processos) {
            System.out.println(processo.getPid() + " | " + processo.getNomeProcesso() + " | " + processo.getTipoProcesso() + " | "
                    + processo.getEstadoProcesso() + " | " + processo.getTempoCPURestante());
        }
        System.out.println();
        System.out.print("Lista de prontos: ");

        for (Processo processo : listaProntos) {
            System.out.print("|" + processo.getPid() + "|->");
        }

        System.out.println("null\n");
    }
}
