import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Xuhao Chen on 2016/12/16.
 */
public class Ant implements Cloneable{
    private ArrayList<Integer> tabuList;
    private ArrayList<Integer> allowedCities;
    private double[][] delta;
    private int[][] distance;
    private double alpha;
    private double beta;

    private int tourLength;
    private int cityNum;
    private int firstCity;
    private int currentCity;

    public Ant(int num){
        cityNum = num;
        tourLength=0;
    }

    public void init(int[][] distance, double a, double b){
        alpha = a;
        beta = b;
        allowedCities = new ArrayList<>();
        tabuList = new ArrayList<>();
        this.distance = distance;

        delta = new double[cityNum][cityNum];
        for(int i=0;i<cityNum;i++){
            Integer n = i;
            allowedCities.add(n);
            for(int j=0;j<cityNum;j++){
                delta[i][j] = 0.0;
            }
        }
        Random random = new Random(System.currentTimeMillis());
        firstCity = random.nextInt(cityNum);
        for(Integer i : allowedCities){
            if(i.equals(firstCity)){
                allowedCities.remove(i);
                break;
            }
        }
        tabuList.add(firstCity);
        currentCity = firstCity;

    }

    public void selectNextCity(double[][] pheromone){
        double[] p = new double[cityNum];
        double sum = 0.0;
        for (Integer i : allowedCities) {
            sum += Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta);
        }
        for(int i=0;i<cityNum;i++){
            boolean found = false;
            for(Integer j : allowedCities){
                if(j.equals(i)){
                    p[i] = (Math.pow(pheromone[currentCity][i], alpha) * Math.pow(1.0 / distance[currentCity][i], beta)) / sum;
                    found = true;
                    break;
                }
            }
            if(!found){
                p[i] = 0.0;
            }
        }

        Random random = new Random();
        double selectP = random.nextFloat();
        int selectCity = 0;
        double tSum = 0.0;
        for (int i = 0; i < cityNum; i++) {
            tSum += p[i];
            if (tSum >= selectP) {
                selectCity = i;
                break;
            }
        }

        for (Integer i : allowedCities) {
            if (i.equals(selectCity)) {
                allowedCities.remove(i);
                break;
            }
        }
        tabuList.add(selectCity);
        currentCity = selectCity;
    }

    private int calculateTourLength() {
        int length = 0;
        for (int i = 0; i < cityNum; i++) {
            length += distance[this.tabuList.get(i)][this.tabuList.get(i + 1)];
        }
        return length;
    }

    public int getTourLength() {
        tourLength = calculateTourLength();
        return tourLength;
    }

    public ArrayList<Integer> getTabuList() {
        return this.tabuList;
    }

    public void setTabuList(ArrayList<Integer> tabuList) {
        this.tabuList = tabuList;
    }

    public double[][] getDelta() {
        return this.delta;
    }

    public void setDelta(double[][] delta) {
        this.delta = delta;
    }

    public int getFirstCity() {
        return this.firstCity;
    }

    public void setFirstCity(int firstCity) {
        this.firstCity = firstCity;
    }
}
