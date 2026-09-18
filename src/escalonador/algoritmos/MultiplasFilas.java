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
 * Algoritmo de Múltiplas Filas com retroalimentação e aging.<br>
 * Organiza os processos em três filas Round-Robin com prioridades distintas:
 * Fila 1 (tempo real e interativo, quantum 2) tem maior prioridade; Fila 2
 * (io_bound e misto, quantum 4); Fila 3 (cpu_bound e batch, quantum 8) tem
 * menor prioridade.<br>
 * Objetivo: favorecer processos interativos e sensíveis ao tempo de resposta,
 * ao mesmo tempo em que acomoda processos longos em filas de menor
 * prioridade.<br>
 * Vantagem: tempo de resposta baixo para processos leves e boa adaptação a
 * cargas mistas.<br>
 * Desvantagem: risco de inanição das filas de menor prioridade. Para mitigar,
 * foi adotado um contador de inanição por fila: se uma fila inferior aguardar
 * mais que 20 unidades de tempo, ela é escolhida na próxima decisão, mesmo com
 * filas superiores com processos prontos.<br>
 * Há preempção entre filas: quando um processo mais prioritário chega, o
 * processo em execução é devolvido à sua fila.<br>
 * Ao final da execução, exporta o Gantt (compacto e matriz) e o CSV de métricas
 * com tempo de retorno, espera, resposta e trocas de contexto.<br>
 *
 * @author molsousa
 */
public class MultiplasFilas {

    private final RoundRobin filaRR1;
    private final RoundRobin filaRR2;
    private final RoundRobin filaRR3;
    private int inan1, inan2, inan3;

    private Processo processoAtual;
    private RoundRobin filaAtualRR;
    private int tempoGlobal;
    List<Processo> processos;

    private final int limiteInan = 20;
    private final Random random = new Random();

    // Gantt / métricas
    private final List<GanttExporter.Bloco> ganttBlocos = new ArrayList<>();
    private int trocasContexto = 0;
    private Processo ultimoExecutado = null;

    public MultiplasFilas(List<Processo> processos) {
        filaRR1 = new RoundRobin(new ArrayList<>(), 2);
        filaRR2 = new RoundRobin(new ArrayList<>(), 4);
        filaRR3 = new RoundRobin(new ArrayList<>(), 8);

        inan1 = inan2 = inan3 = 0;
        tempoGlobal = 0;
        processoAtual = null;
        filaAtualRR = null;
        this.processos = processos;

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
     * Retorna a instância de RoundRobin correspondente ao número de fila
     * sugerido pelo processo.<br>
     * 1 = fila de tempo real/interativo; 2 = io_bound/misto; 3 =
     * cpu_bound/batch.<br>
     *
     * @param filaSugerida Número da fila sugerida (1, 2 ou 3).
     * @return Instância de RoundRobin da fila, ou null se inválido.
     */
    public RoundRobin obterFilaSugestao(int filaSugerida) {
        switch (filaSugerida) {
            case 1:
                return filaRR1;
            case 2:
                return filaRR2;
            case 3:
                return filaRR3;
            default:
                return null;
        }
    }

    /**
     * Retorna o valor numérico de prioridade de uma fila.<br>
     * Quanto menor o valor, maior a prioridade: Fila 1 = 1, Fila 2 = 2, Fila 3
     * = 3.<br>
     *
     * @param fila Instância de RoundRobin representando a fila.
     * @return Prioridade numérica da fila; 99 se desconhecida.
     */
    public int prioridadeFila(RoundRobin fila) {
        if (fila == filaRR1) {
            return 1;
        }
        if (fila == filaRR2) {
            return 2;
        }
        if (fila == filaRR3) {
            return 3;
        }
        return 99;
    }

    /**
     * Seleciona qual fila deve executar na próxima decisão de
     * escalonamento.<br>
     * Aplica aging: se uma fila de menor prioridade acumulou inanição acima do
     * limite, é escolhida primeiro.<br>
     * Caso contrário, respeita a ordem de prioridade (Fila 1 > Fila 2 > Fila
     * 3).<br>
     *
     * @return Fila escolhida, ou null se nenhuma tiver processos prontos.
     */
    public RoundRobin selecionarFilaAging() {
        if (filaRR3.temProcessosProntos() && inan3 >= limiteInan) {
            return filaRR3;
        }
        if (filaRR2.temProcessosProntos() && inan2 >= limiteInan) {
            return filaRR2;
        }
        if (filaRR1.temProcessosProntos() && inan1 >= limiteInan) {
            return filaRR1;
        }

        if (filaRR1.temProcessosProntos()) {
            return filaRR1;
        }
        if (filaRR2.temProcessosProntos()) {
            return filaRR2;
        }
        if (filaRR3.temProcessosProntos()) {
            return filaRR3;
        }
        return null;
    }

    /**
     * Trata a chegada de processos no instante atual.<br>
     * Cada processo novo é direcionado para a fila indicada por
     * fila_sugerida.<br>
     * Se o processo recém-chegado for de fila mais prioritária que a fila em
     * execução, ocorre preempção: o processo em execução é devolvido à sua
     * fila.<br>
     *
     * @param tempo Instante atual da simulação.
     */
    public void tratarChegada(int tempo) {
        for (Processo p : processos) {
            if (p.getEstadoProcesso() == Estado.NOVO && p.getTempoChegada() == tempo) {
                RoundRobin filaDestino = obterFilaSugestao(p.getFilaSugerida());
                if (filaDestino != null) {
                    filaDestino.adicionarProcesso(p);
                    if (processoAtual != null && prioridadeFila(filaDestino) < prioridadeFila(filaAtualRR)) {
                        processoAtual.setEstadoProcesso(Estado.PRONTO);
                        filaAtualRR.adicionarProcesso(processoAtual);
                        filaAtualRR.setProcessoEmExecucao(null);
                        processoAtual = null;
                        filaAtualRR = null;
                    }
                }
            }
        }
    }

    /**
     * Incrementa os contadores de inanição das filas que não estão em execução
     * e que possuem processos prontos.<br>
     * Usado pela estratégia de aging para evitar que filas de menor prioridade
     * fiquem indefinidamente sem executar.<br>
     *
     * @param filaEmExecucao Fila que está ocupando a CPU no momento.
     */
    public void incrementaInanicao(RoundRobin filaEmExecucao) {
        if (filaEmExecucao != filaRR1 && filaRR1.temProcessosProntos()) {
            inan1++;
        }
        if (filaEmExecucao != filaRR2 && filaRR2.temProcessosProntos()) {
            inan2++;
        }
        if (filaEmExecucao != filaRR3 && filaRR3.temProcessosProntos()) {
            inan3++;
        }
    }

    /**
     * Método principal para execução do algoritmo de Múltiplas Filas.<br>
     * Trata chegadas, escolhe a fila a executar (com aging), consome um tique
     * de CPU, atualiza bloqueados e registra o Gantt.<br>
     * Quando não há processos prontos nem em execução, avança o relógio
     * diretamente para o próximo evento (chegada ou fim de E/S), registrando o
     * intervalo ocioso no Gantt.<br>
     * Ao final, exporta Gantt e métricas.<br>
     */
    public void executar() {
        boolean existeAtivo = true;

        while (existeAtivo) {
            tratarChegada(tempoGlobal);

            if (processoAtual == null) {
                RoundRobin filaEscolhida = selecionarFilaAging();

                if (filaEscolhida != null) {
                    processoAtual = filaEscolhida.proximoProcesso();
                    filaAtualRR = filaEscolhida;

                    if (processoAtual != null) {
                        processoAtual.setEstadoProcesso(Estado.EXECUTANDO);
                        processoAtual.setQuantumRestante(filaEscolhida.getQuantum());
                        filaEscolhida.setProcessoEmExecucao(processoAtual);

                        if (processoAtual.getTempoInicioCPU() == -1) {
                            processoAtual.setTempoInicioCPU(tempoGlobal);
                        }
                        if (ultimoExecutado != null && ultimoExecutado != processoAtual) {
                            trocasContexto++;
                        }
                        ultimoExecutado = processoAtual;

                        if (filaEscolhida == filaRR1) {
                            inan1 = 0;
                        } else if (filaEscolhida == filaRR2) {
                            inan2 = 0;
                        } else if (filaEscolhida == filaRR3) {
                            inan3 = 0;
                        }
                    }
                } else {
                    // Ocioso — salta para o próximo evento
                    int proximoEvento = Integer.MAX_VALUE;
                    for (Processo p : processos) {
                        if (p.getEstadoProcesso() == Estado.NOVO && p.getTempoChegada() > tempoGlobal) {
                            proximoEvento = Math.min(proximoEvento, p.getTempoChegada());
                        }
                        if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                            proximoEvento = Math.min(proximoEvento, tempoGlobal + p.getTempoBloqueioRestante());
                        }
                    }
                    if (proximoEvento != Integer.MAX_VALUE) {
                        int tempoAntigo = tempoGlobal;
                        // Registra o intervalo ocioso no Gantt
                        GanttExporter.registrarIntervalo(ganttBlocos, tempoAntigo, proximoEvento, null);

                        int avanco = proximoEvento - tempoAntigo;
                        for (Processo p : processos) {
                            if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                                p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - avanco);
                                p.setTempoTotalES(p.getTempoTotalES() + avanco);
                                if (p.getTempoBloqueioRestante() <= 0) {
                                    p.setEstadoProcesso(Estado.PRONTO);
                                    RoundRobin filaOriginal = obterFilaSugestao(p.getFilaSugerida());
                                    if (filaOriginal != null) {
                                        filaOriginal.adicionarProcesso(p);
                                    }
                                }
                            }
                        }
                        tempoGlobal = proximoEvento;
                        continue;
                    } else {
                        break;
                    }
                }
            }

            // Incrementa espera dos prontos
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.PRONTO) {
                    p.setTempoEspera(p.getTempoEspera() + 1);
                }
            }

            incrementaInanicao(filaAtualRR);

            Processo pTick = processoAtual;

            if (processoAtual != null) {
                if (processoAtual.getTempoCPURestante() == 0) {
                    processoAtual.setEstadoProcesso(Estado.FINALIZADO);
                    processoAtual.setTempoConclusao(tempoGlobal);
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } else if (processoAtual.getQuantumRestante() == 0) {
                    processoAtual.setEstadoProcesso(Estado.PRONTO);
                    filaAtualRR.adicionarProcesso(processoAtual);
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } else if (random.nextDouble() < processoAtual.getProbabilidadeES()) {
                    processoAtual.setEstadoProcesso(Estado.BLOQUEADO);
                    processoAtual.setTempoBloqueioRestante(processoAtual.getDuracaoES());
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } else {
                    processoAtual.setTempoCPURestante(processoAtual.getTempoCPURestante() - 1);
                    processoAtual.setQuantumRestante(processoAtual.getQuantumRestante() - 1);
                    if (processoAtual.getTempoCPURestante() == 0) {
                        processoAtual.setEstadoProcesso(Estado.FINALIZADO);
                        processoAtual.setTempoConclusao(tempoGlobal);
                        filaAtualRR.setProcessoEmExecucao(null);
                        processoAtual = null;
                        filaAtualRR = null;
                    }
                }
            }

            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                    p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - 1);
                    p.setTempoTotalES(p.getTempoTotalES() + 1);
                    if (p.getTempoBloqueioRestante() <= 0) {
                        p.setEstadoProcesso(Estado.PRONTO);
                        RoundRobin filaOriginal = obterFilaSugestao(p.getFilaSugerida());
                        if (filaOriginal != null) {
                            filaOriginal.adicionarProcesso(p);
                        }
                    }
                }
            }

            GanttExporter.registrarBloco(ganttBlocos, tempoGlobal, pTick);

            exibirEstado();
            tempoGlobal++;

            existeAtivo = false;
            for (Processo p : processos) {
                if (p.getEstadoProcesso() != Estado.FINALIZADO) {
                    existeAtivo = true;
                    break;
                }
            }
        }

        exportar("mf");
    }

    private void exportar(String prefixo) {
        GanttExporter.exportarCSV("gantt_" + prefixo + ".csv", ganttBlocos);
        GanttExporter.exportarMatrizCSV("gantt_" + prefixo + "_matriz.csv", ganttBlocos, processos, tempoGlobal);
        GanttExporter.exportarMetricasCSV("metricas_" + prefixo + ".csv", processos, trocasContexto);
    }

    /**
     * Exibe no console o estado de todos os processos, o conteúdo das três
     * filas e o tempo global atual.<br>
     * Utilizado para depuração e acompanhamento passo a passo da simulação.<br>
     */
    public void exibirEstado() {
        System.out.println("Tempo Global: " + tempoGlobal);
        for (Processo p : processos) {
            System.out.println(p.getPid() + " | " + p.getNomeProcesso() + " | " + p.getEstadoProcesso()
                    + " | CPU restante: " + p.getTempoCPURestante()
                    + " | Quantum restante: " + p.getQuantumRestante()
                    + " | Bloqueio restante: " + p.getTempoBloqueioRestante());
        }
        System.out.print("Fila 1: ");
        filaRR1.exibirFila();
        System.out.print("Fila 2: ");
        filaRR2.exibirFila();
        System.out.print("Fila 3: ");
        filaRR3.exibirFila();
        System.out.println("-----------------------------------");
    }
}
