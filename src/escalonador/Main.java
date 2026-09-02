/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package escalonador;

import escalonador.algoritmos.RoundRobin;
import escalonador.utils.CarregarArquivoCSV;
import escalonador.utils.Processo;
import java.io.FileNotFoundException;
import java.util.List;

/**
 *
 * @author molsousa
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        List<Processo> processos = CarregarArquivoCSV.carregarArquivoCSV("entrada.csv");

        RoundRobin rr = new RoundRobin(processos);

        rr.executar();
    }

}
