/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escalonador.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author molsousa
 */
public class CarregarArquivoCSV {

    public static List<Processo> carregarArquivoCSV(String nomeArquivo) {
        try {
            FileReader fileReader = new FileReader(nomeArquivo);
            CSVReader csvReader = new CSVReader(fileReader);

            List<Processo> dadosCSV = new ArrayList<>();

            String[] str;

            while ((str = csvReader.readNext()) != null) {
                if (str[2].equals("tempo_chegada")) {
                    continue;
                }

                Processo dadoCSV = new Processo();

                dadoCSV.setPid(str[0]);
                dadoCSV.setNomeProcesso(str[1]);
                dadoCSV.setTempoChegada(Integer.parseInt(str[2]));
                dadoCSV.setTempoTotalCPU(Integer.parseInt(str[3]));
                dadoCSV.setPrioridade(Integer.parseInt(str[4]));
                dadoCSV.setTipoProcesso(str[5]);
                dadoCSV.setOperacaoES(Boolean.parseBoolean(str[6]));
                dadoCSV.setProbabilidadeES(Float.parseFloat(str[7]));
                dadoCSV.setMediaES(Float.parseFloat(str[8]));
                dadoCSV.setDuracaoES(Integer.parseInt(str[9]));
                dadoCSV.setFilaSugerida(Integer.parseInt(str[10]));
                dadoCSV.setQuantumSugerido(Integer.parseInt(str[11]));
                dadoCSV.setDescricao(str[12]);

                dadosCSV.add(dadoCSV);
            }

            return dadosCSV;

        } catch (CsvValidationException | IOException | NumberFormatException e) {
            System.err.println(e);
            return null;
        }
    }
}
