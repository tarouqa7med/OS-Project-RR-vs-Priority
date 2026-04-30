package util;

import model.Process;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Validator {
    private List<String> errors;
    private Set<String> existingIds;
    
    public Validator() {
        errors = new ArrayList<>();
        existingIds = new HashSet<>();
    }
    
    public boolean validateProcess(Process process) {
        boolean isValid = true;
        
        // Check ID - NOT EMPTY
        if (process.getId() == null || process.getId().trim().isEmpty()) {
            errors.add("ERROR: Process ID cannot be empty");
            isValid = false;
        } 
        // Check ID - DUPLICATE
        else if (existingIds.contains(process.getId())) {
            errors.add("ERROR: Process ID '" + process.getId() + "' already exists! Please use unique IDs.");
            isValid = false;
        } 
        else {
            existingIds.add(process.getId());
        }
        
        // Check Arrival Time
        if (process.getArrivalTime() < 0) {
            errors.add("ERROR: Process " + process.getId() + ": Arrival time cannot be negative");
            isValid = false;
        }
        
        // Check Burst Time
        if (process.getBurstTime() <= 0) {
            errors.add("ERROR: Process " + process.getId() + ": Burst time must be greater than 0");
            isValid = false;
        }
        
        // Check Priority (1-10)
        if (process.getPriority() < 1 || process.getPriority() > 10) {
            errors.add("ERROR: Process " + process.getId() + ": Priority must be between 1 and 10");
            isValid = false;
        }
        
        return isValid;
    }
    
    public boolean validateQuantum(int quantum) {
        // Clear previous quantum errors
        List<String> newErrors = new ArrayList<>();
        for (String e : errors) {
            if (!e.contains("Time quantum")) {
                newErrors.add(e);
            }
        }
        errors = newErrors;
        
        // FIXED: Reject zero or negative quantum
        if (quantum <= 0) {
            errors.add("ERROR: Time quantum must be greater than zero (got: " + quantum + ")");
            return false;
        }
        if (quantum > 50) {
            errors.add("WARNING: Time quantum is very large (" + quantum + "). Consider 1-10 for better results.");
        }
        return true;
    }
    
    public boolean validateProcesses(List<Process> processes) {
        errors.clear();
        
        if (processes == null || processes.isEmpty()) {
            errors.add("ERROR: No processes to simulate. Add at least one process.");
            return false;
        }
        
        boolean allValid = true;
        for (Process p : processes) {
            if (!validateProcess(p)) {
                allValid = false;
            }
        }
        return allValid;
    }
    
    public List<String> getErrors() { 
        return new ArrayList<>(errors); 
    }
    
    public boolean hasErrors() { 
        return !errors.isEmpty(); 
    }
    
    public void clearErrors() { 
        errors.clear(); 
    }
    
    public void reset() {
        errors.clear();
        existingIds.clear();
    }
    
    public String getFormattedErrors() {
        if (errors.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append("• ").append(error).append("\n");
        }
        return sb.toString();
    }
}