/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.algoritmos;

import escalonador.utils.Estado;
import escalonador.utils.Processo;
import java.util.*;

/**
 *
 * @author molsousa
 */
public class MultiplasFilas {

    private RoundRobin filaRR1;
    private RoundRobin filaRR2;
    private RoundRobin filaRR3;
    private int inan1;
    private int inan2;
    private int inan3;

    private Processo processoAtual;
    private RoundRobin filaAtualRR;
    private int tempoGlobal;
    List<Processo> processos;

    private final int limiteInan = 20;

    private final Random random = new Random();

    public MultiplasFilas(List<Processo> processos) {
        filaRR1 = new RoundRobin(new ArrayList<>(), 2);
        filaRR2 = new RoundRobin(new ArrayList<>(), 4);
        filaRR3 = new RoundRobin(new ArrayList<>(), 8);

        inan1 = 0;
        inan2 = 0;
        inan3 = 0;

        tempoGlobal = 0;

        processoAtual = null;
        filaAtualRR = null;

        this.processos = processos;

        for (Processo processo : processos) {
            processo.setEstadoProcesso(Estado.NOVO);
            processo.setTempoCPURestante(processo.getTempoTotalCPU());
            processo.setTempoBloqueioRestante(0);
            processo.setQuantumRestante(0);
        }

    }

    public RoundRobin obterFilaSugestao(int filaSugerida) {
        if (filaSugerida == 1) {
            return filaRR1;
        } else if (filaSugerida == 2) {
            return filaRR2;
        } else if (filaSugerida == 3) {
            return filaRR3;
        } else {
            return null;
        }
    }

    public int prioridadeFila(RoundRobin fila) {
        if (fila == filaRR1) {
            return 1;
        } else if (fila == filaRR2) {
            return 2;
        } else if (fila == filaRR3) {
            return 3;
        } else {
            return 99;
        }
    }

    public RoundRobin selecionarFilaAging() {
        if (filaRR3.temProcessosProntos() && inan3 >= limiteInan) {
            return filaRR3;
        } else if (filaRR2.temProcessosProntos() && inan2 >= limiteInan) {
            return filaRR2;
        } else if (filaRR1.temProcessosProntos() && inan1 >= limiteInan) {
            return filaRR1;
        }

        if (filaRR1.temProcessosProntos()) {
            return filaRR1;
        } else if (filaRR2.temProcessosProntos()) {
            return filaRR2;
        } else if (filaRR3.temProcessosProntos()) {
            return filaRR3;
        } else {
            return null;
        }
    }

    public void tratarChegada(int tempo) {
        for (Processo processo : processos) {
            if (processo.getEstadoProcesso() == Estado.NOVO && processo.getTempoChegada() == tempo) {
                RoundRobin filaDestino = obterFilaSugestao(processo.getFilaSugerida());

                if (filaDestino != null) {
                    filaDestino.adicionarProcesso(processo);

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

    public void executar() {
        boolean existeAtivo = true;

        while (existeAtivo) {

            // tratar chegada de processos
            tratarChegada(tempoGlobal);

            // se não há processos em execução, selecionar próxima fila
            if (processoAtual == null) {
                RoundRobin filaEscolhida = selecionarFilaAging();

                if (filaEscolhida != null) {
                    processoAtual = filaEscolhida.proximoProcesso();
                    filaAtualRR = filaEscolhida;

                    if (processoAtual != null) {
                        processoAtual.setEstadoProcesso(Estado.EXECUTANDO);
                        processoAtual.setQuantumRestante(filaEscolhida.getQuantum());
                        filaEscolhida.setProcessoEmExecucao(processoAtual);

                        if (filaEscolhida == filaRR1) {
                            inan1 = 0;
                        } else if (filaEscolhida == filaRR2) {
                            inan2 = 0;
                        } else if (filaEscolhida == filaRR3) {
                            inan3 = 0;
                        }
                    }
                } else {
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
                        tempoGlobal = proximoEvento;
                    } else {
                        break;
                    }
                }
            }

            if (processoAtual != null) {
                // Verificar término
                if (processoAtual.getTempoCPURestante() == 0) {
                    processoAtual.setEstadoProcesso(Estado.FINALIZADO);
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } // Verificar quantum expirado
                else if (processoAtual.getQuantumRestante() == 0) {
                    processoAtual.setEstadoProcesso(Estado.PRONTO);
                    filaAtualRR.adicionarProcesso(processoAtual);
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } // Verificar probabilidade de E/S
                else if (random.nextDouble() < processoAtual.getProbabilidadeES()) {
                    processoAtual.setEstadoProcesso(Estado.BLOQUEADO);
                    processoAtual.setTempoBloqueioRestante(processoAtual.getDuracaoES());
                    filaAtualRR.setProcessoEmExecucao(null);
                    processoAtual = null;
                    filaAtualRR = null;
                } // Caso contrário, executar 1 unidade de CPU
                else {
                    processoAtual.setTempoCPURestante(processoAtual.getTempoCPURestante() - 1);
                    processoAtual.setQuantumRestante(processoAtual.getQuantumRestante() - 1);
                    incrementaInanicao(filaAtualRR);
                }
            }

            for (Processo p : processos) {
                if (p.getEstadoProcesso() == Estado.BLOQUEADO) {
                    p.setTempoBloqueioRestante(p.getTempoBloqueioRestante() - 1);
                    if (p.getTempoBloqueioRestante() <= 0) {
                        // Quantum esgotado, volta pro final da fila
                        p.setEstadoProcesso(Estado.PRONTO);
                        RoundRobin filaOriginal = obterFilaSugestao(p.getFilaSugerida());
                        filaOriginal.adicionarProcesso(p);
                    }
                }
            }

            exibirEstado();
            // Avança relógio
            tempoGlobal++;

            existeAtivo = false;
            for (Processo p : processos) {
                if (p.getEstadoProcesso() != Estado.FINALIZADO) {
                    existeAtivo = true;
                    break;
                }
            }
        }
    }

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
