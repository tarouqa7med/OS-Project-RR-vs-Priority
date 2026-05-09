package scheduler;

import model.Process;
import model.GanttEvent;
import java.util.*;

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
        
        // Create copies
        List<Process> allProcesses = new ArrayList<>();
        for (Process p : processes) {
            allProcesses.add(p.copy());
        }
        
        int currentTime = 0;
        int completed = 0;
        int total = allProcesses.size();
        
        // Min-heap for ready queue based on priority (lower number = higher priority)
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
            Comparator.comparingInt(Process::getPriority)
                .thenComparingInt(Process::getArrivalTime)
        );
        
        // Sort by arrival time to easily add new processes
        allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        int processIndex = 0;
        Process currentProcess = null;
        int currentStartTime = 0;
        
        while (completed < total) {
            // Add all processes that have arrived by currentTime to ready queue
            while (processIndex < total && allProcesses.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = allProcesses.get(processIndex);
                if (!p.isFinished()) {
                    readyQueue.add(p);
                }
                processIndex++;
            }
            
            // If no process in ready queue, jump to next arrival
            if (readyQueue.isEmpty()) {
                if (processIndex < total) {
                    currentTime = allProcesses.get(processIndex).getArrivalTime();
                    continue;
                } else {
                    break;
                }
            }
            
            // Get the highest priority process
            Process nextProcess = readyQueue.peek();
            
            // If this is a different process than current, handle preemption
            if (currentProcess == null || nextProcess != currentProcess) {
                // Record previous process's Gantt segment
                if (currentProcess != null && currentStartTime < currentTime) {
                    ganttChart.add(new GanttEvent(currentProcess.getId(), currentStartTime, currentTime));
                    // Put the preempted process back in ready queue
                    if (!currentProcess.isFinished()) {
                        readyQueue.add(currentProcess);
                    }
                }
                
                currentProcess = readyQueue.poll();
                currentStartTime = currentTime;
                
                // Record response time on first run
                if (currentProcess.isFirstRun()) {
                    currentProcess.setResponseTime(currentTime - currentProcess.getArrivalTime());
                    currentProcess.setFirstRun(false);
                }
            }
            
            // Calculate how long this process can run
            int timeToFinish = currentProcess.getRemainingTime();
            
            // Find next arrival time
            int nextArrival = Integer.MAX_VALUE;
            if (processIndex < total) {
                nextArrival = allProcesses.get(processIndex).getArrivalTime();
            }
            
            // Check if a higher priority process will arrive before we finish
            int runTime;
            if (nextArrival < currentTime + timeToFinish) {
                // A new process will arrive before we finish
                runTime = nextArrival - currentTime;
            } else {
                runTime = timeToFinish;
            }
            
            // Execute the process
            currentProcess.execute(runTime);
            currentTime += runTime;
            
            // Check if process finished
            if (currentProcess.isFinished()) {
                ganttChart.add(new GanttEvent(currentProcess.getId(), currentStartTime, currentTime));
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
}