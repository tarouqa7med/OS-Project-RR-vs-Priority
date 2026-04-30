package gui;

import model.Process;
import model.GanttEvent;
import scheduler.RoundRobinScheduler;
import scheduler.PriorityScheduler;
import metrics.MetricsCalculator;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MainGUI extends JFrame {
    
    // Data
    private List<Process> processes;
    private Validator validator;
    
    // GUI Components
    private JTextField txtId, txtArrival, txtBurst, txtPriority, txtQuantum;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JPanel rrGanttPanel, priorityGanttPanel;
    private JTable rrResultsTable, priorityResultsTable;
    private DefaultTableModel rrTableModel, priorityTableModel;
    private JTextArea comparisonArea;
    private List<GanttEvent> lastRrEvents, lastPriorityEvents;
    private int lastRrMaxTime, lastPriorityMaxTime;
    
    // Size constants
    private static final int GANTT_HEIGHT = 130;
    private static final int METRICS_TABLE_HEIGHT = 180;
    private static final int COMPARISON_HEIGHT = 220;
    
    public MainGUI() {
        processes = new ArrayList<>();
        validator = new Validator();
        lastRrEvents = new ArrayList<>();
        lastPriorityEvents = new ArrayList<>();
        
        showSplash();
        
        setTitle("CPU Scheduling Simulator - Round Robin vs Priority");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 950);
        setLayout(new BorderLayout());
        
        createInputPanel();
        createCenterPanel();
        createBottomPanel();
        
        setVisible(true);
    }
    
    private void showSplash() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(25, 118, 210));
        
        JLabel label = new JLabel("CPU Scheduling Simulator", JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel sublabel = new JLabel("Round Robin vs Priority Scheduling", JLabel.CENTER);
        sublabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sublabel.setForeground(new Color(200, 200, 255));
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(new Color(25, 118, 210));
        textPanel.add(label, BorderLayout.CENTER);
        textPanel.add(sublabel, BorderLayout.SOUTH);
        
        content.add(textPanel, BorderLayout.CENTER);
        splash.setContentPane(content);
        splash.setSize(500, 250);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        splash.setVisible(false);
    }
    
    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Panel"));
        
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        fieldsPanel.add(new JLabel("ID:"));
        txtId = new JTextField(5);
        fieldsPanel.add(txtId);
        
        fieldsPanel.add(new JLabel("Arrival:"));
        txtArrival = new JTextField(5);
        fieldsPanel.add(txtArrival);
        
        fieldsPanel.add(new JLabel("Burst:"));
        txtBurst = new JTextField(5);
        fieldsPanel.add(txtBurst);
        
        fieldsPanel.add(new JLabel("Priority (1-10):"));
        txtPriority = new JTextField(5);
        fieldsPanel.add(txtPriority);
        
        JButton btnAdd = new JButton("Add Process");
        btnAdd.setBackground(new Color(34, 197, 94));
        fieldsPanel.add(btnAdd);
        
        fieldsPanel.add(new JLabel("Quantum:"));
        txtQuantum = new JTextField("3", 5);
        fieldsPanel.add(txtQuantum);
        
        JButton btnRun = new JButton("RUN SIMULATION");
        btnRun.setBackground(new Color(59, 130, 246));
        btnRun.setForeground(Color.WHITE);
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(btnRun);
        
        JButton btnClear = new JButton("Clear All");
        btnClear.setBackground(new Color(239, 68, 68));
        btnClear.setForeground(Color.WHITE);
        fieldsPanel.add(btnClear);
        
        JButton btnExport = new JButton("Export Results");
        btnExport.setBackground(new Color(139, 69, 19));
        btnExport.setForeground(Color.WHITE);
        fieldsPanel.add(btnExport);
        
        inputPanel.add(fieldsPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Arrival", "Burst", "Priority"};
        tableModel = new DefaultTableModel(columns, 0);
        processTable = new JTable(tableModel);
        processTable.setRowHeight(25);
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Added Processes"));
        tableScroll.setPreferredSize(new Dimension(800, 120));
        inputPanel.add(tableScroll, BorderLayout.CENTER);
        
        add(inputPanel, BorderLayout.NORTH);
        
        btnAdd.addActionListener(e -> addProcess());
        btnRun.addActionListener(e -> runSimulation());
        btnClear.addActionListener(e -> clearAll());
        btnExport.addActionListener(e -> exportResults());
    }
    
    private void createCenterPanel() {
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.6);
        mainSplitPane.setDividerLocation(550);
        
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round Robin Gantt Panel
        JPanel rrContainer = new JPanel(new BorderLayout());
        rrContainer.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN SCHEDULER"));
        rrGanttPanel = new JPanel(new BorderLayout());
        rrGanttPanel.setBackground(Color.WHITE);
        rrGanttPanel.setBorder(BorderFactory.createEtchedBorder());
        rrGanttPanel.setPreferredSize(new Dimension(0, GANTT_HEIGHT));
        
        JLabel rrEmptyLabel = new JLabel("No Gantt chart available. Run simulation first.", JLabel.CENTER);
        rrEmptyLabel.setForeground(Color.GRAY);
        rrGanttPanel.add(rrEmptyLabel, BorderLayout.CENTER);
        rrContainer.add(rrGanttPanel, BorderLayout.CENTER);
        topPanel.add(rrContainer);
        
        // Priority Gantt Panel
        JPanel priorityContainer = new JPanel(new BorderLayout());
        priorityContainer.setBorder(BorderFactory.createTitledBorder("PRIORITY SCHEDULER (Preemptive)"));
        priorityGanttPanel = new JPanel(new BorderLayout());
        priorityGanttPanel.setBackground(Color.WHITE);
        priorityGanttPanel.setBorder(BorderFactory.createEtchedBorder());
        priorityGanttPanel.setPreferredSize(new Dimension(0, GANTT_HEIGHT));
        
        JLabel priorityEmptyLabel = new JLabel("No Gantt chart available. Run simulation first.", JLabel.CENTER);
        priorityEmptyLabel.setForeground(Color.GRAY);
        priorityGanttPanel.add(priorityEmptyLabel, BorderLayout.CENTER);
        priorityContainer.add(priorityGanttPanel, BorderLayout.CENTER);
        topPanel.add(priorityContainer);
        
        mainSplitPane.setTopComponent(topPanel);
        
        // Metrics Tables
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round Robin Metrics
        JPanel rrMetricsContainer = new JPanel(new BorderLayout());
        rrMetricsContainer.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN METRICS"));
        
        String[] resultColumns = {"Process ID", "WT", "TAT", "RT"};
        rrTableModel = new DefaultTableModel(resultColumns, 0);
        rrResultsTable = new JTable(rrTableModel);
        rrResultsTable.setRowHeight(28);
        rrResultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 4; i++) {
            rrResultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane rrScroll = new JScrollPane(rrResultsTable);
        rrScroll.setPreferredSize(new Dimension(0, METRICS_TABLE_HEIGHT));
        rrMetricsContainer.add(rrScroll, BorderLayout.CENTER);
        bottomPanel.add(rrMetricsContainer);
        
        // Priority Metrics
        JPanel priorityMetricsContainer = new JPanel(new BorderLayout());
        priorityMetricsContainer.setBorder(BorderFactory.createTitledBorder("PRIORITY METRICS"));
        
        priorityTableModel = new DefaultTableModel(resultColumns, 0);
        priorityResultsTable = new JTable(priorityTableModel);
        priorityResultsTable.setRowHeight(28);
        priorityResultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        for (int i = 0; i < 4; i++) {
            priorityResultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane priorityScroll = new JScrollPane(priorityResultsTable);
        priorityScroll.setPreferredSize(new Dimension(0, METRICS_TABLE_HEIGHT));
        priorityMetricsContainer.add(priorityScroll, BorderLayout.CENTER);
        bottomPanel.add(priorityMetricsContainer);
        
        mainSplitPane.setBottomComponent(bottomPanel);
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("COMPARISON SUMMARY & ANALYSIS"));
        
        comparisonArea = new JTextArea();
        comparisonArea.setEditable(false);
        comparisonArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        comparisonArea.setBackground(new Color(245, 248, 250));
        comparisonArea.setMargin(new Insets(15, 15, 15, 15));
        
        JScrollPane scroll = new JScrollPane(comparisonArea);
        scroll.setPreferredSize(new Dimension(0, COMPARISON_HEIGHT));
        bottomPanel.add(scroll, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // ===== FIXED addProcess() METHOD =====
    private void addProcess() {
        try {
            String id = txtId.getText().trim();
            
            // Check for empty ID
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Process ID cannot be empty!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int arrival = Integer.parseInt(txtArrival.getText().trim());
            int burst = Integer.parseInt(txtBurst.getText().trim());
            int priority = Integer.parseInt(txtPriority.getText().trim());
            
            Process p = new Process(id, arrival, burst, priority);
            validator.clearErrors();  // Only clears error messages, NOT existingIds
            
            if (validator.validateProcess(p)) {
                processes.add(p);
                tableModel.addRow(new Object[]{id, arrival, burst, priority});
                txtId.setText("");
                txtArrival.setText("");
                txtBurst.setText("");
                txtPriority.setText("");
                JOptionPane.showMessageDialog(this, "Process " + id + " added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, validator.getFormattedErrors(), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for all fields", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatNumber(double value) {
        return String.format("%.4f", value);
    }
    
private void runSimulation() {
    if (processes.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please add at least one process first", 
            "No Processes", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int quantum;
    try {
        String quantumText = txtQuantum.getText().trim();
        
        // Check if quantum field is empty
        if (quantumText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Time quantum cannot be empty!", 
                "Invalid Quantum", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        quantum = Integer.parseInt(quantumText);
        
        // DIRECT VALIDATION - no relying on Validator for this
        if (quantum <= 0) {
            JOptionPane.showMessageDialog(this, "ERROR: Time quantum must be greater than zero (got: " + quantum + ")", 
                "Invalid Quantum", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Optional warning for large quantum
        if (quantum > 50) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "WARNING: Time quantum is very large (" + quantum + ").\nThis may affect performance. Continue anyway?",
                "Large Quantum Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter a valid integer for Time Quantum", 
            "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
    try {
        // Round Robin Simulation
        RoundRobinScheduler rrScheduler = new RoundRobinScheduler(quantum);
        rrScheduler.simulate(processes);
        
        lastRrEvents = rrScheduler.getGanttChart();
        lastRrMaxTime = calculateMaxTime(lastRrEvents);
        drawGanttChart(rrGanttPanel, lastRrEvents, lastRrMaxTime);
        
        rrTableModel.setRowCount(0);
        for (Process p : rrScheduler.getResults()) {
            rrTableModel.addRow(new Object[]{
                p.getId(),
                formatNumber(p.getWaitingTime()),
                formatNumber(p.getTurnaroundTime()),
                formatNumber(p.getResponseTime())
            });
        }
        
        double rrAvgWT = MetricsCalculator.calculateAverageWaitingTime(rrScheduler.getResults());
        double rrAvgTAT = MetricsCalculator.calculateAverageTurnaroundTime(rrScheduler.getResults());
        double rrAvgRT = MetricsCalculator.calculateAverageResponseTime(rrScheduler.getResults());
        
        // Priority Simulation
        PriorityScheduler priorityScheduler = new PriorityScheduler();
        priorityScheduler.simulate(processes);
        
        lastPriorityEvents = priorityScheduler.getGanttChart();
        lastPriorityMaxTime = calculateMaxTime(lastPriorityEvents);
        drawGanttChart(priorityGanttPanel, lastPriorityEvents, lastPriorityMaxTime);
        
        priorityTableModel.setRowCount(0);
        for (Process p : priorityScheduler.getResults()) {
            priorityTableModel.addRow(new Object[]{
                p.getId(),
                formatNumber(p.getWaitingTime()),
                formatNumber(p.getTurnaroundTime()),
                formatNumber(p.getResponseTime())
            });
        }
        
        double priorityAvgWT = MetricsCalculator.calculateAverageWaitingTime(priorityScheduler.getResults());
        double priorityAvgTAT = MetricsCalculator.calculateAverageTurnaroundTime(priorityScheduler.getResults());
        double priorityAvgRT = MetricsCalculator.calculateAverageResponseTime(priorityScheduler.getResults());
        
        // Build Beautiful Output
        buildCleanOutput(rrAvgWT, priorityAvgWT, rrAvgTAT, priorityAvgTAT, rrAvgRT, priorityAvgRT);
        
    } finally {
        setCursor(Cursor.getDefaultCursor());
    }
}
    
    private void buildCleanOutput(double rrWT, double prWT, double rrTAT, double prTAT, double rrRT, double prRT) {
        StringBuilder sb = new StringBuilder();
        
        // ===== TITLE =====
        sb.append("╔══════════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                       C O M P A R I S O N   A N A L Y S I S                         ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════════════╝\n\n");
        
        // ===== METRICS TABLE =====
        sb.append("┌─────────────────────────────────────┬─────────────────────┬─────────────────────┐\n");
        sb.append("│              METRIC                 │    ROUND ROBIN      │      PRIORITY       │\n");
        sb.append("├─────────────────────────────────────┼─────────────────────┼─────────────────────┤\n");
        sb.append(String.format("│ Average Waiting Time           │      %8.4f      │      %8.4f      │\n", rrWT, prWT));
        sb.append(String.format("│ Average Turnaround Time        │      %8.4f      │      %8.4f      │\n", rrTAT, prTAT));
        sb.append(String.format("│ Average Response Time          │      %8.4f      │      %8.4f      │\n", rrRT, prRT));
        sb.append("└─────────────────────────────────────┴─────────────────────┴─────────────────────┘\n\n");
        
        // ===== ANALYSIS SECTION =====
        sb.append("┌─────────────────────────────────────────────────────────────────────────────────┐\n");
        sb.append("│                               A N A L Y S I S                                    │\n");
        sb.append("└─────────────────────────────────────────────────────────────────────────────────┘\n\n");
        
        // Waiting Time Analysis
        if (rrWT < prWT) {
            double improvement = ((prWT - rrWT) / prWT) * 100;
            sb.append(String.format("[WINNER] Round Robin has LOWER waiting time (%.4f vs %.4f)  ->  %.2f%% BETTER\n", rrWT, prWT, improvement));
            sb.append("   - Round Robin gives each process equal CPU time, reducing queue waiting.\n");
            sb.append("   - Ideal for systems where fairness is more important than priority.\n");
        } else if (prWT < rrWT) {
            double improvement = ((rrWT - prWT) / rrWT) * 100;
            sb.append(String.format("[WINNER] Priority Scheduling has LOWER waiting time (%.4f vs %.4f)  ->  %.2f%% BETTER\n", prWT, rrWT, improvement));
            sb.append("   - High-priority processes are served immediately, reducing overall waiting.\n");
            sb.append("   - Ideal for real-time systems with urgent tasks.\n");
        } else {
            sb.append("[TIE] Both algorithms have EQUAL waiting time.\n");
        }
        
        sb.append("\n");
        
        // Response Time Analysis
        if (rrRT < prRT) {
            double improvement = ((prRT - rrRT) / prRT) * 100;
            sb.append(String.format("[WINNER] Round Robin has FASTER response time (%.4f vs %.4f)  ->  %.2f%% FASTER\n", rrRT, prRT, improvement));
            sb.append("   - Time-slicing ensures every process gets CPU access quickly.\n");
            sb.append("   - Better for interactive users and real-time responsiveness.\n");
        } else if (prRT < rrRT) {
            double improvement = ((rrRT - prRT) / rrRT) * 100;
            sb.append(String.format("[WINNER] Priority Scheduling has FASTER response time (%.4f vs %.4f)  ->  %.2f%% FASTER\n", prRT, rrRT, improvement));
        } else {
            sb.append("[TIE] Both algorithms have EQUAL response time.\n");
        }
        
        sb.append("\n");
        
        // ===== RECOMMENDATION SECTION =====
        sb.append("┌─────────────────────────────────────────────────────────────────────────────────┐\n");
        sb.append("│                            R E C O M M E N D A T I O N                           │\n");
        sb.append("└─────────────────────────────────────────────────────────────────────────────────┘\n\n");
        
        if (rrWT <= prWT && rrRT <= prRT) {
            sb.append("[RECOMMENDATION] Round Robin is recommended for this workload.\n");
            sb.append("   - Better or equal in both waiting time and response time.\n");
            sb.append("   - Ideal for interactive systems where fairness matters.\n");
            sb.append("   - No starvation risk.\n");
        } else if (prWT <= rrWT && prRT <= rrRT) {
            sb.append("[RECOMMENDATION] Priority Scheduling is recommended for this workload.\n");
            sb.append("   - Better or equal in both waiting time and response time.\n");
            sb.append("   - Ideal for real-time systems with urgent tasks.\n");
            sb.append("   - Warning: Risk of starvation for low-priority processes.\n");
        } else {
            sb.append("The choice depends on your system priorities:\n\n");
            sb.append("   [A] Use PRIORITY SCHEDULING if:\n");
            sb.append("        - You have URGENT tasks that must complete faster\n");
            sb.append("        - Waiting time is more important than response time\n");
            sb.append("        - You can accept some unfairness for high-priority tasks\n\n");
            sb.append("   [B] Use ROUND ROBIN if:\n");
            sb.append("        - You need FAIRNESS across all processes\n");
            sb.append("        - Response time matters for user experience\n");
            sb.append("        - You want to prevent starvation completely\n");
        }
        
        sb.append("\n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("\n");
        
        // ===== KEY INSIGHTS =====
        sb.append("KEY INSIGHTS:\n");
        sb.append("─────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("- Priority Scheduling favours high-priority processes -> lower waiting time\n");
        sb.append("- Round Robin provides better response time -> better for interactive users\n");
        sb.append("- Round Robin prevents starvation naturally (no process waits forever)\n");
        sb.append("- Priority Scheduling risks starvation for low-priority processes\n");
        sb.append("- Time quantum affects Round Robin: smaller = better response, larger = better throughput\n");
        sb.append("- For mixed workloads, consider combining both algorithms (Multi-level Feedback Queue)\n");
        
        sb.append("\n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                    CPU Scheduling Simulator - Comparison Complete                \n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
        
        comparisonArea.setText(sb.toString());
    }
    
    private int calculateMaxTime(List<GanttEvent> events) {
        int max = 0;
        for (GanttEvent e : events) {
            if (e.getEndTime() > max) max = e.getEndTime();
        }
        return max;
    }
    
    private void drawGanttChart(JPanel panel, List<GanttEvent> events, int maxTime) {
        panel.removeAll();
        
        if (events == null || events.isEmpty()) {
            JLabel emptyLabel = new JLabel("No Gantt chart available. Run simulation first.", JLabel.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            panel.add(emptyLabel, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
            return;
        }
        
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 60;
                int height = 70;
                int y = 15;
                
                if (maxTime <= 0) return;
                double scale = (double) width / maxTime;
                
                Color[] colors = {
                    new Color(66, 133, 244), new Color(234, 67, 53), new Color(52, 168, 83),
                    new Color(251, 188, 5), new Color(155, 66, 245), new Color(255, 87, 34),
                    new Color(46, 125, 50), new Color(194, 24, 91), new Color(96, 125, 139)
                };
                
                int colorIndex = 0;
                Map<String, Color> processColors = new HashMap<>();
                
                int x = 30;
                for (GanttEvent e : events) {
                    if (!processColors.containsKey(e.getProcessId())) {
                        processColors.put(e.getProcessId(), colors[colorIndex % colors.length]);
                        colorIndex++;
                    }
                    
                    int blockWidth = (int) ((e.getEndTime() - e.getStartTime()) * scale);
                    if (blockWidth < 3) blockWidth = 3;
                    
                    g2d.setColor(processColors.get(e.getProcessId()));
                    g2d.fillRoundRect(x, y, blockWidth, height, 8, 8);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRoundRect(x, y, blockWidth, height, 8, 8);
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    String text = e.getProcessId();
                    int textWidth = g2d.getFontMetrics().stringWidth(text);
                    if (blockWidth > textWidth + 4) {
                        g2d.drawString(text, x + (blockWidth - textWidth) / 2, y + height / 2 + 4);
                    }
                    
                    x += blockWidth;
                }
                
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                for (int t = 0; t <= maxTime; t += Math.max(1, maxTime / 8)) {
                    int markerX = 30 + (int) (t * scale);
                    if (markerX <= getWidth() - 10) {
                        g2d.drawLine(markerX, y + height, markerX, y + height + 5);
                        g2d.drawString(String.valueOf(t), markerX - 5, y + height + 18);
                    }
                }
            }
        };
        
        chartPanel.setPreferredSize(new Dimension(panel.getWidth(), GANTT_HEIGHT - 30));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
    
    private void exportResults() {
        if (comparisonArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results to export. Run simulation first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Results");
        fileChooser.setSelectedFile(new java.io.File("scheduler_results.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter fw = new java.io.FileWriter(fileChooser.getSelectedFile());
                fw.write("========================================\n");
                fw.write("CPU SCHEDULING SIMULATOR - RESULTS\n");
                fw.write("========================================\n\n");
                fw.write(comparisonArea.getText());
                fw.write("\n\n--- ROUND ROBIN GANTT CHART ---\n");
                for (GanttEvent e : lastRrEvents) {
                    fw.write(e.toString() + " ");
                }
                fw.write("\n\n--- PRIORITY GANTT CHART ---\n");
                for (GanttEvent e : lastPriorityEvents) {
                    fw.write(e.toString() + " ");
                }
                fw.close();
                JOptionPane.showMessageDialog(this, "Results exported successfully!", 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ===== FIXED clearAll() METHOD =====
    private void clearAll() {
        processes.clear();
        validator.reset();  // IMPORTANT: Reset validator's internal state
        tableModel.setRowCount(0);
        rrTableModel.setRowCount(0);
        priorityTableModel.setRowCount(0);
        comparisonArea.setText("");
        
        rrGanttPanel.removeAll();
        priorityGanttPanel.removeAll();
        
        JLabel rrEmptyLabel = new JLabel("No Gantt chart available. Run simulation first.", JLabel.CENTER);
        rrEmptyLabel.setForeground(Color.GRAY);
        rrGanttPanel.add(rrEmptyLabel, BorderLayout.CENTER);
        
        JLabel priorityEmptyLabel = new JLabel("No Gantt chart available. Run simulation first.", JLabel.CENTER);
        priorityEmptyLabel.setForeground(Color.GRAY);
        priorityGanttPanel.add(priorityEmptyLabel, BorderLayout.CENTER);
        
        rrGanttPanel.revalidate();
        rrGanttPanel.repaint();
        priorityGanttPanel.revalidate();
        priorityGanttPanel.repaint();
        
        txtId.setText("");
        txtArrival.setText("");
        txtBurst.setText("");
        txtPriority.setText("");
        txtQuantum.setText("3");
        
        JOptionPane.showMessageDialog(this, "All data cleared!", "Reset", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}