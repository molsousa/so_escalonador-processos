<div style="text-align: justify">

# Implementação de algoritmos de escalonamento de CPU

**Disciplina:** Sistemas Operacionais.

**Professor:** André Gustavo Maletzke.

**Estudante:** [Marcos Oliveira de Sousa](https://github.com/molsousa).

---

Este repositório apresenta um programa em Java que simula três algoritmos de escalonamento de CPU. Sendo esses: Round-Robin, Múltiplas Filas e Menor Prioridade Primeiro (MPP), o último sendo um algoritmo implementado pelo "grupo".

Todos os algoritmos foram todos implementados em Java, apenas extração das imagens (via dados CSV extraídos da execução) foram usados scripts Python.

---

## Descrição

### Round-Robin

Round-Robin é um algoritmo de escalonamento que utiliza fila para armazenar os processos que usarão a CPU. Quando um processo está executando, ele pode ou finalizar sua execução dentro do espaço de tempo do quantum, ou ser movido ao final da fila caso não seja finalizado. Não há tolerância caso sobre poucas unidades de tempo para finalizar. O algoritmo é imune a “starvation” levando em consideração que todos os processos serão executados em dado momento, mesmo que possa demorar de acordo com a demanda de processos. Para a implementação do algoritmo, foi utilizado quantum fixo de 4 unidades de tempo.

### Múltiplas Filas 

Múltiplas Filas é um algoritmo de escalonamento que cria várias filas de execução que podem ter políticas internas diferentes entre si a depender da prioridade do processo executado. Exemplo: um processo `io_bound` pode ou ter maior ou menor prioridade que um `interativo`, isso varia com a funcionalidade a qual o sistema operacional se preocupa a executar. Para a implementação, foi utilizado a mesma política interna para todas as filas (Round-Robin) com quantum diferentes para cada uma.

### Menor Prioridade Primeiro (MPP)

Menor Prioridade Primeiro é um algoritmo de escalonamento que utiliza duas filas de execução diferentes, uma para processos categorizados como `leves` e outra para processos categorizados como `pesados`, a ideia do algoritmo é otimizar o tempo de execução dos processos mais pesados com a consequência de penalizar os mais leves. A motivação de duas diferentes filas é em razão de tempo maior de execução de um processo pesado, essa fila utiliza um "quantum especial" que trabalha com um valor de quantum 4x maior que o quantum padrão dos processos mais leves. Fora criado também uma política para evitar inanição de processos leves em que, caso algum processo leve fique 30 unidades de tempo sem executar, o mesmo é colocado ao início da fila.

## Estrutura de pasta do projeto 

``` ASCII
├── python
│   ├── ganttmf.py
│   ├── ganttmpp.py
│   └── ganttrr.py
├── src
│   └── escalonador
│       ├── algoritmos
│       │   ├── MPP.java
│       │   ├── MultiplasFilas.java
│       │   └── RoundRobin.java
│       ├── arquivo
│       │   └── CarregarArquivoCSV.java
│       ├── utils
│       │   ├── Estado.java
│       │   ├── GanttExporter.java
│       │   └── Processo.java
│       └── Main.java
├── .gitignore
└── README.md
```

## Compilação e Execução dos algoritmos

### Requisitos

- Java 17+
- OpenCSV (baixe `opencsv-5.x.jar` e coloque em `lib/`)

### Compilar

``` sh
javac -cp "lib/opencsv-5.9.jar" -d bin $(find src -name "*.java")
``` 

### Executar

``` sh
java -cp "bin:lib/opencsv-5.9.jar" escalonador.Main
```

### Entrada

`entrada.csv` no diretório de execução.

### Saídas

Para cada algoritmo (rr, mf, mpp):
- gantt_<alg>.csv          → Gantt em blocos contínuos.
- gantt_<alg>_matriz.csv   → Gantt em matriz (pid × tempo).
- metricas_<alg>.csv       → Métricas por processo + médias + trocas.

---

</div>
