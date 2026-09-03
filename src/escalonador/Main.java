/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package escalonador;

import escalonador.algoritmos.MPP;
import escalonador.algoritmos.MultiplasFilas;
import escalonador.algoritmos.RoundRobin;
import escalonador.arquivo.CarregarArquivoCSV;
import escalonador.utils.Processo;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author molsousa
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        /*
        int i;
        do {
            System.out.print("Insira o nome do arquivo: ");

            List<Processo> processos = CarregarArquivoCSV.carregarArquivoCSV(scanner.next());

            RoundRobin rr = new RoundRobin(processos, 4);
            MultiplasFilas mf = new MultiplasFilas(processos);

            System.out.println("---------------------");
            System.out.println("1 - Round Robin");
            System.out.println("2 - Múltiplas Filas");
            System.out.println("0 - Sair");
            System.out.println("---------------------");
            System.out.print("Selecione uma opção: ");

            i = scanner.nextInt();
            switch (i) {
                case 1:
                    rr.executar();
                    break;
                case 2:
                    mf.executar();
                    break;
                case 0:
                    System.out.println("Obrigado!!");
                    break;
                default:
                    System.out.println("Opção incorreta!!");
                    break;
            }
        } while (i != 0);
*/
        
        List<Processo> processos = CarregarArquivoCSV.carregarArquivoCSV("entrada.csv");
        
        MPP mpp = new MPP(processos, 4, 16);
        
        mpp.executar();
        /*
        
        RoundRobin rr = new RoundRobin(processos, 4);
        
        rr.executar();
        
        MultiplasFilas mf = new MultiplasFilas(processos);
        
        mf.executar();
        */

    }

}
