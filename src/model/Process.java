package model;

public class Process {
    // Fields
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int remainingTime;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private int responseTime;
    private boolean firstRun;
    
    // Constructor
    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.firstRun = true;
    }
    
    // Getters
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getRemainingTime() { return remainingTime; }
    public int getCompletionTime() { return completionTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getResponseTime() { return responseTime; }
    public boolean isFirstRun() { return firstRun; }
    
    // Setters
    public void setCompletionTime(int time) { this.completionTime = time; }
    public void setResponseTime(int time) { this.responseTime = time; }
    public void setFirstRun(boolean value) { this.firstRun = value; }
    
    // Core methods
    public void execute(int timeUnits) { 
        remainingTime -= timeUnits; 
    }
    
    public boolean isFinished() { 
        return remainingTime == 0; 
    }
    
    public void calculateTurnaroundTime() { 
        turnaroundTime = completionTime - arrivalTime; 
    }
    
    public void calculateWaitingTime() { 
        waitingTime = turnaroundTime - burstTime; 
    }
    
    public Process copy() {
        Process copy = new Process(id, arrivalTime, burstTime, priority);
        copy.remainingTime = this.remainingTime;
        copy.firstRun = this.firstRun;
        return copy;
    }
    
    @Override
    public String toString() {
        return id + " [AT=" + arrivalTime + ", BT=" + burstTime + ", Prio=" + priority + "]";
    }
}