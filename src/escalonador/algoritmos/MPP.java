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
 * Algoritmo Menor Prioridade Primeiro (MPP).<br>
 * Começa por processos de menor prioridade.<br>
 * Objetivo: tratar processos mais pesados de io/cpu-bound e batch para
 * equivalência de processos.<br>
 * Vantagem: Inicia processos mais pesados e já acelera processos de E/S.<br>
 * Desvantagem: Processos mais leves executados depois. Risco de inanição de
 * processos de maior prioridade<br>
 * Novo processo categorizado como mais pesado vai ao início, mais leve vai ao
 * final. <br>
 * Usado duas filas, para processos pesados e leves. <br>
 * Caso um processo leve esperar mais que 30 unidades de tempo, ele é escolhido
 * a frente para não ocorrer inanição. <br>
 * Em E/S executa 1 unidade de CPU e só depois verifica de bloqueia, o
 * decremento do bloqueio ocorre no início do próximo tique. <br>
 *
 * @author molsousa
 */
public class MPP {

    private final List<Processo> processos;
    private final int quantumLeve;
    private final int quantumPesado;
    private final Deque<Processo> filaPesados = new LinkedList<>();
    private final Deque<Processo> filaLeves = new LinkedList<>();
    private Processo processoEmExecucao;
    private boolean cpuOcupada;
    private int tempoAtual;
    private final Random random = new Random();
    private final int limiteInanicao = 30;

    private final List<GanttExporter.Bloco> ganttBlocos = new ArrayList<>();
    private int trocasContexto = 0;
    private Processo ultimoExecutado = null;

    /**
     * Construtor para inicializar algoritmo.
     *
     * @param processos Inicializa atributo de lista de processos.
     * @param quantumPadrao Inicializa o quantum geral.
     * @param quantumEspecial Inicializa o quantum para processos de menor
     * prioridade.
     */
    public MPP(List<Processo> processos, int quantumPadrao,
            int quantumEspecial) {
        this.processos = processos;
        this.quantumLeve = quantumPadrao;
        this.quantumPesado = quantumEspecial;
        this.tempoAtual = 0;
        this.cpuOcupada = false;
        this.processoEmExecucao = null;

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

    private boolean isPesado(Processo p) {
        String t = p.getTipoProcesso();
        return "cpu_bound".equals(t) || "batch".equals(t) || "io_bound".equals(t);
    }

    private void adicionarNaFila(Processo p) {
        if (isPesado(p)) {
            filaPesados.addLast(p);
        } else {
            filaLeves.addLast(p);
        }
    }

    private Processo selecionarProximo() {
        // Processo leve que esperou demais passa na fila
        Iterator<Processo> it = filaLeves.iterator();
        while (it.hasNext()) {
            Processo p = it.next();
            if (p.getTempoEspera() >= limiteInanicao) {
                it.remove();
                return p;
            }
        }
        if (!filaPesados.isEmpty()) {
            return filaPesados.removeFirst();
        }
        if (!filaLeves.isEmpty()) {
            return filaLeves.removeFirst();
        }
        return null;
    }

    /**
     * Método principal para execução do algoritmo MPP.<br>
     * A cada tique: decrementa bloqueios, processa chegadas, incrementa o tempo
     * de espera dos prontos, seleciona o próximo processo (com aging sobre a
     * fila de leves) e executa uma unidade de CPU.<br>
     * A E/S é decidida após consumir a unidade de CPU do tique, evitando
     * desbloqueio no mesmo instante em que ocorre.<br>
     * Ao final, exporta Gantt e métricas.<br>
     */
    public void executar() {
        while (true) {
            // 1 - Bloqueados
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                    p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - 1);
                    p.setTempoTotalES(p.getTempoTotalES() + 1);
                    if (p.getTempoBloqueioRestante() <= 0) {
                        p.setEstadoProcesso(Estado.PRONTO);
                        adicionarNaFila(p);
                    }
                }
            }

            // 2 - Chegadas
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.NOVO && p.getTempoChegada() == tempoAtual) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    adicionarNaFila(p);
                }
            }

            // 3 - Espera dos prontos
            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.PRONTO) {
                    p.setTempoEspera(p.getTempoEspera() + 1);
                }
            }

            // 4 - Escalonar
            if (!cpuOcupada && (!filaPesados.isEmpty() || !filaLeves.isEmpty())) {
                Processo p = selecionarProximo();
                if (p != null) {
                    processoEmExecucao = p;
                    p.setEstadoProcesso(Estado.EXECUTANDO);
                    p.setTempoEspera(0);
                    if (p.getTempoInicioCPU() == -1) {
                        p.setTempoInicioCPU(tempoAtual);
                    }
                    p.setQuantumRestante(isPesado(p) ? quantumPesado : quantumLeve);
                    cpuOcupada = true;

                    if (ultimoExecutado != null && ultimoExecutado != p) {
                        trocasContexto++;
                    }
                    ultimoExecutado = p;
                }
            }

            Processo pTick = cpuOcupada ? processoEmExecucao : null;

            // 5 - Executar 1 unidade
            if (cpuOcupada) {
                Processo p = processoEmExecucao;
                if (p.getTempoCPURestante() == 0) {
                    p.setEstadoProcesso(Estado.FINALIZADO);
                    p.setTempoConclusao(tempoAtual);
                    cpuOcupada = false;
                    processoEmExecucao = null;
                } else if (p.getQuantumRestante() == 0) {
                    p.setEstadoProcesso(Estado.PRONTO);
                    adicionarNaFila(p);
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
                    } else if (random.nextDouble() < p.getProbabilidadeES()) {
                        p.setEstadoProcesso(Estado.BLOQUEADO);
                        p.setTempoBloqueioRestante(p.getDuracaoES());
                        cpuOcupada = false;
                        processoEmExecucao = null;
                    }
                }
            }

            // 6 - Gantt
            GanttExporter.registrarBloco(ganttBlocos, tempoAtual, pTick);

            exibirEstado();
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

        exportar("mpp");
    }

    private void exportar(String prefixo) {
        GanttExporter.exportarCSV("gantt_" + prefixo + ".csv", ganttBlocos);
        GanttExporter.exportarMatrizCSV("gantt_" + prefixo + "_matriz.csv", ganttBlocos, processos, tempoAtual);
        GanttExporter.exportarMetricasCSV("metricas_" + prefixo + ".csv", processos, trocasContexto);
    }

    /**
     * Exibe no console o estado de todos os processos, o conteúdo das filas de
     * pesados e leves e o tempo atual.<br>
     * Utilizado para depuração e acompanhamento passo a passo da simulação.<br>
     */
    public void exibirEstado() {
        System.out.println("Tempo: " + tempoAtual);
        for (Processo p : processos) {
            System.out.println(p.getPid() + " | " + p.getNomeProcesso() + " | " + p.getTipoProcesso() + " | "
                    + p.getEstadoProcesso() + " | CPU restante: " + p.getTempoCPURestante()
                    + " | Espera: " + p.getTempoEspera());
        }
        System.out.print("Fila pesados: ");
        for (Processo p : filaPesados) {
            System.out.print("|" + p.getPid() + "|->");
        }
        System.out.println("null");
        System.out.print("Fila leves: ");
        for (Processo p : filaLeves) {
            System.out.print("|" + p.getPid() + "|->");
        }
        System.out.println("null\n");
    }
}
