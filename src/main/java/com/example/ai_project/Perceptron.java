package com.example.ai_project;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class Perceptron {
    private double[] weights = new double [4];
    private double learningRate;
    public Perceptron(double learningRate) {
        Random rand = new Random();
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rand.nextDouble() * 0.1 - 0.05; //-0.05 to 0.05
        }
        this.learningRate = learningRate;
    }
    public int Predict(int tileType, int elevation, int distance){
        double sum;
        sum = weights[0] + weights[1]*tileType + weights[2]*elevation + weights[3]*distance;
        return sum >= 0 ? 1 : 0;
    }
    public void train(String path, int epochs) throws IOException {
        List<DataPoint> allData = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(new File(path));
             XSSFWorkbook workbook = new XSSFWorkbook(file)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int tileType = (int) row.getCell(0).getNumericCellValue();
                int elevation = (int) row.getCell(1).getNumericCellValue();
                int distance = (int) row.getCell(2).getNumericCellValue();
                int label = (int) row.getCell(3).getNumericCellValue();
                allData.add(new DataPoint(tileType, elevation, distance, label));
            }
        }
        Collections.shuffle(allData, new Random());

        int splitIndex = (int) (allData.size() * 0.8);
        List<DataPoint> trainData = allData.subList(0, splitIndex);
        List<DataPoint> testData = allData.subList(splitIndex, allData.size());


        for (int epoch = 0; epoch < epochs; epoch++) {
            for (DataPoint dp : trainData) {
                int prediction = Predict(dp.tileType, dp.elevation, dp.distance);
                int error = dp.label - prediction;

                weights[0] += learningRate * error;
                weights[1] += learningRate * error * dp.tileType;
                weights[2] += learningRate * error * dp.elevation;
                weights[3] += learningRate * error * dp.distance;
            }
        }


        int correct = 0;
        for (DataPoint dp : testData) {
            int prediction = Predict(dp.tileType, dp.elevation, dp.distance);
            if (prediction == dp.label) correct++;
        }

        double accuracy = correct * 100.0 / testData.size();
        System.out.printf("Test Accuracy: %.2f%% (%d/%d)\n", accuracy, correct, testData.size());
    }


    public double[] getWeights() {
        return weights;
    }

    private class DataPoint {
        public int tileType;
        public int elevation;
        public int distance;
        public int label;
        public DataPoint(int tileType, int elevation, int distance, int label) {
            this.tileType = tileType;
            this.elevation = elevation;
            this.distance = distance;
            this.label = label;
        }
    }


}