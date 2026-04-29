package model;

public class TestProcess {
    public static void main(String[] args) {
        Process p = new Process("P1", 0, 10, 3);
        System.out.println("Created: " + p);
        
        p.execute(3);
        System.out.println("After 3 units, remaining: " + p.getRemainingTime());
        
        p.execute(7);
        System.out.println("After 7 more units, finished? " + p.isFinished());
    }
}