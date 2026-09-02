/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.algoritmos;

import escalonador.utils.CarregarArquivoCSV;
import escalonador.utils.Estado;
import escalonador.utils.Processo;
import java.util.*;

/**
 *
 * @author molsousa
 */
public class RoundRobin {

    private final List<Processo> processos;
    private final Queue<Processo> fila_prontos;
    private final int QUANTUM = 4;
    private int tempoAtual;
    private boolean cpuOcupada;
    private Processo processoEmExecucao;

    private final Random random;

    /**
     * Classe para RoundRobin, inicializa com os processos retirados do arquivo
     * CSV. Atualiza o estado dos processos para NOVO.
     *
     * @param nomeArquivo Nome do arquivo CSV de entrada.
     */
    public RoundRobin(List<Processo> processos) {

        fila_prontos = new LinkedList<>();
        this.processos = processos;
        tempoAtual = 0;
        cpuOcupada = false;
        processoEmExecucao = null;

        random = new Random();

        for (Processo p : processos) {
            p.setEstadoProcesso(Estado.NOVO);
            p.setTempoCPURestante(p.getTempoTotalCPU());
            p.setTempoBloqueioRestante(0);
            p.setQuantumRestante(0);
        }
    }

    /**
     * Método para executar algoritmo RoundRobin.
     */
    public void executar() {
        while (true) {
            // 1 - Chegada de processos
            for (Processo p : processos) {
                if (p.getTempoChegada() == tempoAtual && p.getEstadoProcesso() == Estado.NOVO) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    fila_prontos.add(p);
                }
            }

            // 2 - Escalonar se CPU ociosa e houver prontos
            if (!cpuOcupada && !fila_prontos.isEmpty()) {
                processoEmExecucao = fila_prontos.poll();
                processoEmExecucao.setEstadoProcesso(Estado.EXECUTANDO);
                processoEmExecucao.setQuantumRestante(QUANTUM);
                cpuOcupada = true;
            }

            // 3 - Processar CPU
            if (cpuOcupada) {
                Processo p = processoEmExecucao;
                if (p.getTempoCPURestante() == 0) {
                    p.setEstadoProcesso(Estado.FINALIZADO);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (p.getQuantumRestante() == 0) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    fila_prontos.add(p);
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
                    // Se após decrementar CPU restante chegar a zero, finaliza já
                    if (p.getTempoCPURestante() == 0) {
                        p.setEstadoProcesso(Estado.FINALIZADO);
                        cpuOcupada = false;
                        processoEmExecucao = null;
                    }
                }
            }

            // 4 - Atualizar bloqueados
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                    p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - 1);
                    if (p.getTempoBloqueioRestante() == 0) {
                        p.setEstadoProcesso(Estado.PRONTO);
                        fila_prontos.add(p);
                    }
                }
            }

            // 5 - Exibir estado
            exibirEstado(tempoAtual, processos, fila_prontos);

            // 6 - Avançar tempo
            tempoAtual++;

            // 7 - Verificar se ainda existem processos não finalizados
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
    }

    /**
     * Método para exibir estados de execução na tela.
     *
     * @param tempoAtual
     * @param processos
     * @param fila_prontos
     */
    public void exibirEstado(int tempoAtual, List<Processo> processos, Queue<Processo> fila_prontos) {
        System.out.println("Tempo: " + tempoAtual);

        for (Processo processo : processos) {
            System.out.println(processo.getPid() + " | " + processo.getNomeProcesso() + " | " + processo.getEstadoProcesso()
                    + " | " + processo.getTempoCPURestante());
        }
        System.out.println();
        System.out.print("Fila de prontos: ");

        for (Processo p : fila_prontos) {
            System.out.print("|" + p.getPid() + "|->");
        }

        System.out.println("null\n");
    }

}
