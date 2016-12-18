/**
 * Created by Xuhao Chen on 2016/12/16.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class ACO {

    private Ant[] ants;
    private int antNum;
    private int cityNum;
    private int MAX_GEN;
    private float[][] pheromone;
    private int[][] distance;
    private int bestLength;
    private int[] bestTour;
    private int[] x;
    private int[] y;

    private int tagName = -1;
    private int tagDimension = -1;
    private int tagEdgeWeightSection = -1;
    private int tagDisplayDataSection = -1;

    private String name;
    private ArrayList<String> file;

    private float alpha;
    private float beta;
    private float rho;

    public ACO() {

    }

    public ACO(int m, int g, float a, float b, float r) {
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        alpha = a;
        beta = b;
        rho = r;
    }

    private static String fileChooser(){
        String filePath;
        JFileChooser fileChooser = new JFileChooser();
        try{
            fileChooser.showOpenDialog(null);
        }catch (HeadlessException hE){
            System.err.println("Open File Dialog Error");
            hE.printStackTrace();
        }
        File tmp = fileChooser.getSelectedFile();

        filePath = tmp.getAbsolutePath();
        System.out.println("Opening file: " + filePath);
        return filePath;
    }

    private void getTagsLine(String line, int lineNum){
        if(line.contains("NAME")) tagName = lineNum;
        else if(line.contains("DIMENSION")) tagDimension = lineNum;
        else if(line.contains("EDGE_WEIGHT_SECTION")) tagEdgeWeightSection = lineNum;
        else if(line.contains("DISPLAY_DATA_SECTION")) tagDisplayDataSection = lineNum;
    }

    private void readFile(String path){

        file = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            int lineNum = 0;
            do{
                line = br.readLine();

                getTagsLine(line,lineNum);

                file.add(line);
                lineNum++;
            }while(!line.contains("EOF"));

        }catch (FileNotFoundException fnfE){
            fnfE.printStackTrace();
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }

    }

    private void processFile(){
        String line;

        //name
        line = file.get(tagName);
        name = line.substring(6,line.length());

        //dimension
        line = file.get(tagDimension);
        cityNum = Integer.parseInt(line.substring(11,line.length()));

        //edgeWeightSection
        distance = new int[cityNum][cityNum];
        for(int i = tagEdgeWeightSection+1;i<tagDisplayDataSection;i++){
            line = file.get(i);
            String[] nums = line.split("\\D+");
            for(int j=0;j<cityNum;j++){
                distance[i-tagEdgeWeightSection-1][j] = Integer.parseInt(nums[j+1]);
            }
        }

        //displayDataSection
        x = new int[cityNum];
        y = new int[cityNum];
        for(int i=tagDisplayDataSection+1;i<tagDisplayDataSection+1+cityNum;i++){
            line = file.get(i);
            String[] nums = line.split("\\D++");
            int cityTag = Integer.parseInt(nums[1]);
            x[cityTag-1] = Integer.parseInt(nums[2]);
            y[cityTag-1] = Integer.parseInt(nums[4]);
        }

    }

    private void init(String path) throws IOException {
        readFile(path);
        processFile();
        pheromone = new float[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromone[i][j] = 0.1f;
            }
        }
        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum + 1];
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distance, alpha, beta);
        }
    }

    private void solve() {
        for (int g = 0; g < MAX_GEN; g++) {
            for (int i = 0; i < antNum; i++) {
                for (int j = 1; j < cityNum; j++) {
                    ants[i].selectNextCity(pheromone);
                }
                ants[i].getTabuList().add(ants[i].getFirstCity());
                if (ants[i].getTourLength() < bestLength) {
                    bestLength = ants[i].getTourLength();
                    for (int k = 0; k < cityNum + 1; k++) {
                        bestTour[k] = ants[i].getTabuList().get(k);
                    }
                }
                for (int j = 0; j < cityNum; j++) {
                    ants[i].getDelta()[ants[i].getTabuList().get(j)][ants[i].getTabuList().get(j + 1)] = (float) (1.0 / ants[i].getTourLength());
                    ants[i].getDelta()[ants[i].getTabuList().get(j + 1)][ants[i].getTabuList().get(j)] = (float) (1.0 / ants[i].getTourLength());
                }
            }
            updatePheromone();
            for (int i = 0; i < antNum; i++) {
                ants[i].init(distance, alpha, beta);
            }
        }
        printBestResult();
    }

    private void updatePheromone() {
        for (int i = 0; i < cityNum; i++)
            for (int j = 0; j < cityNum; j++)
                pheromone[i][j] = pheromone[i][j] * (1 - rho);
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                for (int k = 0; k < antNum; k++) {
                    pheromone[i][j] += ants[k].getDelta()[i][j];
                }
            }
        }
    }

    private void printBestResult() {
        System.out.println("The best length found is: " + bestLength);
        System.out.println("The best tour found is: ");
        for (int i = 0; i < cityNum + 1; i++) {
            System.out.println(bestTour[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        ACO aco = new ACO(10, 100, 1.f, 5.f, 0.5f);
        aco.init(fileChooser());
        aco.solve();
    }

}


