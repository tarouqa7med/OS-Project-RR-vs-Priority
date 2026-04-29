package scheduler;

import model.Process;
import model.GanttEvent;
import java.util.*;
import java.util.stream.Collectors;

public class PriorityScheduler {
    private List<Process> results;
    private List<GanttEvent> ganttChart;
    
    public PriorityScheduler() {
        this.results = new ArrayList<>();
        this.ganttChart = new ArrayList<>();
    }
    
    public void simulate(List<Process> processes) {
        results.clear();
        ganttChart.clear();
        
        // Create copies of all processes
        List<Process> remainingProcesses = new ArrayList<>();
        for (Process p : processes) {
            remainingProcesses.add(p.copy());
        }
        
        int currentTime = 0;
        int completed = 0;
        Process currentProcess = null;
        int startTime = 0;
        int totalProcesses = remainingProcesses.size();
        
        while (completed < totalProcesses) {
            // Create a final copy for lambda (FIX #1)
            final int timeNow = currentTime;
            
            // Get ready processes (arrived and not finished)
            List<Process> ready = remainingProcesses.stream()
                .filter(p -> p.getArrivalTime() <= timeNow && !p.isFinished())
                .collect(Collectors.toList());
            
            // If no ready process, jump to next arrival
            if (ready.isEmpty()) {
                Process nextProcess = null;
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : remainingProcesses) {
                    if (!p.isFinished() && p.getArrivalTime() < nextArrival) {
                        nextArrival = p.getArrivalTime();
                        nextProcess = p;
                    }
                }
                if (nextProcess != null) {
                    currentTime = nextArrival;
                }
                continue;
            }
            
            // Select highest priority (lower number = higher priority)
            Process next = ready.stream()
                .min(Comparator.comparingInt(Process::getPriority))
                .orElse(null);
            
            // Handle context switch
            if (currentProcess == null || next != currentProcess) {
                if (currentProcess != null && startTime < currentTime) {
                    ganttChart.add(new GanttEvent(currentProcess.getId(), startTime, currentTime));
                }
                currentProcess = next;
                startTime = currentTime;
                
                // Record response time if first run
                if (currentProcess.isFirstRun()) {
                    currentProcess.setResponseTime(currentTime - currentProcess.getArrivalTime());
                    currentProcess.setFirstRun(false);
                }
            }
            
            // Calculate time until next arrival (FIX #2 - no lambda with changing variable)
            int timeToFinish = currentProcess.getRemainingTime();
            int nextArrivalTime = Integer.MAX_VALUE;
            
            // Find next arrival time (manual loop instead of lambda)
            for (Process p : remainingProcesses) {
                if (p.getArrivalTime() > currentTime && !p.isFinished()) {
                    if (p.getArrivalTime() < nextArrivalTime) {
                        nextArrivalTime = p.getArrivalTime();
                    }
                }
            }
            
            // Check if a higher priority process will arrive before we finish
            int preemptTime = nextArrivalTime;
            boolean willBePreempted = false;
            
            for (Process p : remainingProcesses) {
                if (p.getArrivalTime() > currentTime && 
                    p.getArrivalTime() < currentTime + timeToFinish &&
                    !p.isFinished()) {
                    if (p.getPriority() < currentProcess.getPriority()) {
                        if (p.getArrivalTime() < preemptTime) {
                            preemptTime = p.getArrivalTime();
                            willBePreempted = true;
                        }
                    }
                }
            }
            
            // Determine how long to run
            int runTime;
            if (willBePreempted) {
                runTime = preemptTime - currentTime;
            } else {
                runTime = timeToFinish;
            }
            
            // Execute the process
            currentProcess.execute(runTime);
            currentTime += runTime;
            
            // Check if finished
            if (currentProcess.isFinished()) {
                ganttChart.add(new GanttEvent(currentProcess.getId(), startTime, currentTime));
                currentProcess.setCompletionTime(currentTime);
                currentProcess.calculateTurnaroundTime();
                currentProcess.calculateWaitingTime();
                completed++;
                results.add(currentProcess);
                currentProcess = null;
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
    
    public String getGanttChartString() {
        StringBuilder sb = new StringBuilder();
        for (GanttEvent e : ganttChart) {
            sb.append(e.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}