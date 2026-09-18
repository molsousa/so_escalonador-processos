/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.utils;

/**
 * Representa um processo do sistema simulado.<br>
 * Contém os dados estáticos lidos do arquivo CSV (PID, nome, tempo de chegada,
 * tempo total de CPU, prioridade, tipo, parâmetros de E/S, fila sugerida,
 * quantum sugerido e descrição) e os dados dinâmicos atualizados durante a
 * simulação (tempo de CPU restante, tempo de bloqueio restante, quantum
 * restante, estado atual).<br>
 * Também armazena campos de métricas coletadas pelo escalonador: tempo de
 * início na CPU, tempo de conclusão, tempo total em E/S e tempo de espera
 * acumulado.<br>
 *
 * @author molsousa
 */
public class Processo {

    private String pid;
    private String nomeProcesso;
    private int tempoChegada;
    private int tempoTotalCPU;
    private int prioridade;
    private String tipoProcesso;
    private boolean operacaoES;
    private float probabilidadeES;
    private float mediaES;
    private int duracaoES;
    private int filaSugerida;
    private int quantumSugerido;
    private String descricao;

    private int tempoCPURestante;
    private int tempoBloqueioRestante;
    private int quantumRestante;

    private Estado estadoProcesso;

    private int tempoInicioCPU = -1;
    private int tempoConclusao = -1;
    private int tempoTotalES = 0;
    private int tempoEspera = 0;

    public Processo() {

    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getNomeProcesso() {
        return nomeProcesso;
    }

    public void setNomeProcesso(String nomeProcesso) {
        this.nomeProcesso = nomeProcesso;
    }

    public int getTempoChegada() {
        return tempoChegada;
    }

    public void setTempoChegada(int tempoChegada) {
        this.tempoChegada = tempoChegada;
    }

    public int getTempoTotalCPU() {
        return tempoTotalCPU;
    }

    public void setTempoTotalCPU(int tempoTotalCPU) {
        this.tempoTotalCPU = tempoTotalCPU;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public String getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(String tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public boolean isOperacaoES() {
        return operacaoES;
    }

    public void setOperacaoES(boolean operacaoES) {
        this.operacaoES = operacaoES;
    }

    public float getProbabilidadeES() {
        return probabilidadeES;
    }

    public void setProbabilidadeES(float probabilidadeES) {
        this.probabilidadeES = probabilidadeES;
    }

    public float getMediaES() {
        return mediaES;
    }

    public void setMediaES(float mediaES) {
        this.mediaES = mediaES;
    }

    public int getDuracaoES() {
        return duracaoES;
    }

    public void setDuracaoES(int duracaoES) {
        this.duracaoES = duracaoES;
    }

    public int getFilaSugerida() {
        return filaSugerida;
    }

    public void setFilaSugerida(int filaSugerida) {
        this.filaSugerida = filaSugerida;
    }

    public int getQuantumSugerido() {
        return quantumSugerido;
    }

    public void setQuantumSugerido(int quantumSugerido) {
        this.quantumSugerido = quantumSugerido;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getTempoCPURestante() {
        return tempoCPURestante;
    }

    public void setTempoCPURestante(int tempoCPURestante) {
        this.tempoCPURestante = tempoCPURestante;
    }

    public int getTempoBloqueioRestante() {
        return tempoBloqueioRestante;
    }

    public void setTempoBloqueioRestante(int tempoBloqueioRestante) {
        this.tempoBloqueioRestante = tempoBloqueioRestante;
    }

    public int getQuantumRestante() {
        return quantumRestante;
    }

    public void setQuantumRestante(int quantumRestante) {
        this.quantumRestante = quantumRestante;
    }

    public Estado getEstadoProcesso() {
        return estadoProcesso;
    }

    public void setEstadoProcesso(Estado estadoProcesso) {
        this.estadoProcesso = estadoProcesso;
    }

    public int getTempoInicioCPU() {
        return tempoInicioCPU;
    }

    public void setTempoInicioCPU(int tempoInicioCPU) {
        this.tempoInicioCPU = tempoInicioCPU;
    }

    public int getTempoConclusao() {
        return tempoConclusao;
    }

    public void setTempoConclusao(int tempoConclusao) {
        this.tempoConclusao = tempoConclusao;
    }

    public int getTempoTotalES() {
        return tempoTotalES;
    }

    public void setTempoTotalES(int tempoTotalES) {
        this.tempoTotalES = tempoTotalES;
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public void setTempoEspera(int tempoEspera) {
        this.tempoEspera = tempoEspera;
    }

}
