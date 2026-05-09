package scheduler;

import model.Process;
import model.GanttEvent;
import java.util.*;

public class RoundRobinScheduler {
    private int quantum;
    private List<Process> results;
    private List<GanttEvent> ganttChart;
    
    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
        this.results = new ArrayList<>();
        this.ganttChart = new ArrayList<>();
    }
    
    public void simulate(List<Process> processes) {
        results.clear();
        ganttChart.clear();
        
        // Create copies
        List<Process> allProcesses = new ArrayList<>();
        for (Process p : processes) {
            allProcesses.add(p.copy());
        }
        
        // Sort by arrival time
        allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completed = 0;
        int total = allProcesses.size();
        int index = 0; // to track which processes have been added
        
        while (completed < total) {
            // Add all processes that have arrived by currentTime
            while (index < total && allProcesses.get(index).getArrivalTime() <= currentTime) {
                Process p = allProcesses.get(index);
                if (!p.isFinished()) {
                    readyQueue.add(p);
                }
                index++;
            }
            
            // If queue is empty, jump to next arrival time
            if (readyQueue.isEmpty()) {
                if (index < total) {
                    currentTime = allProcesses.get(index).getArrivalTime();
                    continue;
                } else {
                    break;
                }
            }
            
            // Get next process from queue
            Process current = readyQueue.poll();
            
            // Record response time on first run
            if (current.isFirstRun()) {
                current.setResponseTime(currentTime - current.getArrivalTime());
                current.setFirstRun(false);
            }
            
            // Calculate run time
            int runTime = Math.min(quantum, current.getRemainingTime());
            int startTime = currentTime;
            int endTime = currentTime + runTime;
            
            // Record Gantt event
            ganttChart.add(new GanttEvent(current.getId(), startTime, endTime));
            
            // Execute
            current.execute(runTime);
            currentTime = endTime;
            
            // Check if finished
            if (current.isFinished()) {
                current.setCompletionTime(currentTime);
                current.calculateTurnaroundTime();
                current.calculateWaitingTime();
                completed++;
                results.add(current);
            } else {
                // CRITICAL: Before adding back to queue, add any new arrivals
                while (index < total && allProcesses.get(index).getArrivalTime() <= currentTime) {
                    Process p = allProcesses.get(index);
                    if (!p.isFinished()) {
                        readyQueue.add(p);
                    }
                    index++;
                }
                // Add current process back to END of queue
                readyQueue.add(current);
            }
        }
        
        results.sort(Comparator.comparing(Process::getId));
    }
    
    public List<Process> getResults() { 
        return new ArrayList<>(results); 
    }
    
    public List<GanttEvent> getGanttChart() { 
        return new ArrayList<>(ganttChart); 
    }
}