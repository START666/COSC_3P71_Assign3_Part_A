/**
 * Created by Xuhao Chen on 2016/12/16.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ACO {

    public static boolean debug = true;

    private int tagName = -1;
    private int tagDimension = -1;
    private int tagEdgeWeightSection = -1;
    private int tagDisplayDataSection = -1;

    private String name;
    private ArrayList<String> file;
    private Ant[] ants;
    private int antNum;
    private int cityNum;
    private int MAX_GEN;
    private double[][] pheromoneTable;
    private int[][] distanceTable;
    private int bestLength;
    private Queue<Integer> bestLengthTracker;
    private int[] bestTour;
    private int[] x;
    private int[] y;

    private double a;
    private double b;
    private double r;

    public ACO(int m, int g, double a, double b, double r) {
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        this.a = a;
        this.b = b;
        this.r = r;
        bestLengthTracker = new LinkedList<>();
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
        distanceTable = new int[cityNum][cityNum];
        for(int i = tagEdgeWeightSection+1;i<tagDisplayDataSection;i++){
            line = file.get(i);
            String[] nums = line.split("\\D+");
            for(int j=0;j<cityNum;j++){
                distanceTable[i-tagEdgeWeightSection-1][j] = Integer.parseInt(nums[j+1]);
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
        pheromoneTable = new double[cityNum][cityNum];
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                pheromoneTable[i][j] = 0.1f;
            }
        }
        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum + 1];
        for (int i = 0; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distanceTable, a, b);
        }
    }

    private void solve() {
        for (int g = 0; g < MAX_GEN; g++) {  //generation
            for (int i = 0; i < antNum; i++) {  //ants
                for (int j = 1; j < cityNum; j++) {  //move ant to next city
                    ants[i].selectNextCity(pheromoneTable);
                }
                ants[i].getTabuList().add(ants[i].getFirstCity());
                if (ants[i].getTourLength() < bestLength) {   //if find better solution
                    bestLength = ants[i].getTourLength();
                    for (int k = 0; k < cityNum + 1; k++) {
                        bestTour[k] = ants[i].getTabuList().get(k);
                    }
                }
                for (int j = 0; j < cityNum; j++) {
                    ants[i].getDelta()[ants[i].getTabuList().get(j)][ants[i].getTabuList().get(j + 1)] = (1.0 / ants[i].getTourLength());
                    ants[i].getDelta()[ants[i].getTabuList().get(j + 1)][ants[i].getTabuList().get(j)] = (1.0 / ants[i].getTourLength());
                }
            }
            updatePheromone();
            for (int i = 0; i < antNum; i++) {
                ants[i].init(distanceTable, a, b);
            }
            if(debug) System.out.println("Generation "+g+": "+bestLength);
            bestLengthTracker.offer(bestLength);
        }
        printBestResult();

    }

    private void display(){
        Frame display = new Frame(550,600,x,y,bestTour);
    }

    private void writeFile(){
        try{
            File file = new File("output.txt");
            if(!file.exists()) file.createNewFile();  //if does not exist

            FileWriter fileWriter = new FileWriter(file.getName(),false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String data="";
            int gen=0;

            data += "antNum" + "\t" + Integer.toString(antNum) + "\n";
            data += "MAX_GEN" + "\t" + Integer.toString(MAX_GEN) + "\n";
            data += "alpha" + "\t" + Double.toString(a) + "\n";
            data += "beta" + "\t" + Double.toString(b) + "\n";
            data += "rho" + "\t" + Double.toString(r) + "\n";

            data += "Generation" + "\t" + "length" + "\n";
            while(!bestLengthTracker.isEmpty()){
                data += (Integer.toString(gen)+"\t"+Integer.toString(bestLengthTracker.poll()));
                data += "\n";
                gen++;
            }

            bufferedWriter.write(data);
            bufferedWriter.close();
            System.out.println("File saved.");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updatePheromone() {
        for (int i = 0; i < cityNum; i++)
            for (int j = 0; j < cityNum; j++)
                pheromoneTable[i][j] = pheromoneTable[i][j] * (1 - r);
        for (int i = 0; i < cityNum; i++) {
            for (int j = 0; j < cityNum; j++) {
                for (int k = 0; k < antNum; k++) {
                    pheromoneTable[i][j] += ants[k].getDelta()[i][j];
                }
            }
        }
    }

    private void printBestResult() {
        System.out.println("The best length found is: " + bestLength);
        System.out.println("The best tour found is: ");
        String output="";
        for (int i = 0; i < cityNum + 1; i++) {
            String cityTag = Integer.toString(bestTour[i]+1);
            output += cityTag;
            if(i != cityNum) output += " >> ";
        }
        System.out.println(output);
    }

    public static void main(String[] args) throws IOException {
        ACO aco;

        if(debug){
            aco = new ACO(50, 100, 1.0, 5.0, 0.5);
            aco.init("ACO/bays29.tsp");
        }
        else{
            int m,g;
            double a,b,r;
            Scanner scanner = new Scanner(System.in);

            System.out.println("Please input number of ants:");
            m = scanner.nextInt();
            System.out.println("Please input maximum generations:");
            g = scanner.nextInt();
            System.out.println("Please input parameters");
            System.out.println("alpha:");
            a = scanner.nextDouble();
            System.out.println("beta:");
            b = scanner.nextDouble();
            System.out.println("rho:");
            r = scanner.nextDouble();
            aco = new ACO(m,g,a,b,r);

            System.out.println("Use bays29.tsp as data file?(y/n)(1/0)");
            while(!scanner.hasNext());
            String f = scanner.next();
            if(f.equals("y") || f.equals("Y") || f.equals("1"))  aco.init("ACO/bays29.tsp");
            else {
                System.out.println("Please choose the data file:");
                aco.init(fileChooser());
            }
        }
        aco.solve();
        aco.display();
        aco.writeFile();
    }

}


