/*5s
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls;

import jurls.core.approximation.ApproxParameters;
import jurls.examples.approximation.RenderArrayFunction1D;
import jurls.examples.approximation.RenderFunction1D;
import jurls.examples.approximation.RenderParameterizedFunction1D;
import jurls.examples.menu.ApproximatorMenu;
import jurls.examples.menu.ObjectListMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author thorsten
 */
public class ApproximationDemo1D extends javax.swing.JFrame {

    private final ApproxParameters approxParameters = new ApproxParameters(0.01, 0.1);
    private final RenderParameterizedFunction1D renderParameterizedFunction = new RenderParameterizedFunction1D(Color.yellow);
    private int numIterations = 0;
    private int numIterationsPerLoop = 1;
    private final ApproximatorMenu approximatorMenu = new ApproximatorMenu(false);
    private final ObjectListMenu iterationsMenu = new ObjectListMenu(
            "No. Iterations", 0, 1, 50, 500, 1000, 5000
    );
    int components = 16;
    private final RenderFunction1D f;
    private Timer timer = new Timer(5, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < numIterationsPerLoop; ++i) {
                double x = Math.random();
                //System.out.println(x + " " + f.compute(x));
                renderParameterizedFunction.learn(x, f.compute(x));
                numIterations++;
            }
            functionRenderer1.repaint();

            jTextArea1.setText(
                    "Iterations : " + numIterations + "\n"
                    + "No. parameters : "
                    + renderParameterizedFunction.getParameterizedFunction().numberOfParameters()
            );
        }
    });

    /**
     * Creates new form ApproximationDemo
     */
    public ApproximationDemo1D() {
        initComponents();

        timer.setCoalesce(true);

        approxParameters.setAlpha(Double.parseDouble(alphaComboBox.getSelectedItem().toString()));
        approxParameters.setMomentum(Double.parseDouble(momentumComboBox.getSelectedItem().toString()));

        jMenuBar1.add(approximatorMenu);
        jMenuBar1.add(iterationsMenu);

        approximatorMenu.addActionListener((ActionEvent e) -> {
            renderParameterizedFunction.setParameterizedFunction(
                    approximatorMenu.getFunctionGenerator(
                            approxParameters
                    ).generate(1)
            );
            numIterations = 0;
        });
        approximatorMenu.notifyListeners();

        iterationsMenu.addActionListener((ActionEvent e) -> {
            numIterationsPerLoop = (int) iterationsMenu.getObject();
        });
        iterationsMenu.notifyListeners();

        double[] ys = new double[components];
        for (int i = 0; i < ys.length; ++i) {
            ys[i] = 2.0f * (Math.random() - 0.5);
        }
        f = new RenderArrayFunction1D(Color.blue, ys);

        functionRenderer1.renderFunctions.add(f);
        functionRenderer1.renderFunctions.add(renderParameterizedFunction);
        timer.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        functionButtonGroup = new javax.swing.ButtonGroup();
        hiddenButtonGroup = new javax.swing.ButtonGroup();
        outputButtonGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        functionRenderer1 = new jurls.examples.approximation.FunctionRenderer1D();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        alphaComboBox = new javax.swing.JComboBox();
        momentumComboBox = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 750));

        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout functionRenderer1Layout = new javax.swing.GroupLayout(functionRenderer1);
        functionRenderer1.setLayout(functionRenderer1Layout);
        functionRenderer1Layout.setHorizontalGroup(
            functionRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );
        functionRenderer1Layout.setVerticalGroup(
            functionRenderer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 349, Short.MAX_VALUE)
        );

        jPanel1.add(functionRenderer1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Demo", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Debug 1", jPanel2);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.GridLayout(0, 1));

        alphaComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.01", "0.001", "0.0001" }));
        alphaComboBox.setSelectedIndex(1);
        alphaComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphaComboBoxActionPerformed(evt);
            }
        });
        jPanel6.add(alphaComboBox);

        momentumComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.01", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "0.95", "0.99" }));
        momentumComboBox.setSelectedIndex(9);
        momentumComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                momentumComboBoxActionPerformed(evt);
            }
        });
        jPanel6.add(momentumComboBox);

        jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new java.awt.GridLayout(0, 1));

        jLabel3.setText("Learning Rate (Alpha)");
        jPanel7.add(jLabel3);

        jLabel4.setText("Momentum");
        jPanel7.add(jLabel4);

        jPanel5.add(jPanel7, java.awt.BorderLayout.WEST);

        jPanel4.add(jPanel5, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);
        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void alphaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphaComboBoxActionPerformed
        approxParameters.setAlpha(Double.parseDouble(alphaComboBox.getSelectedItem().toString()));
    }//GEN-LAST:event_alphaComboBoxActionPerformed

    private void momentumComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_momentumComboBoxActionPerformed
        approxParameters.setMomentum(Double.parseDouble(momentumComboBox.getSelectedItem().toString()));
    }//GEN-LAST:event_momentumComboBoxActionPerformed

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ApproximationDemo1D().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox alphaComboBox;
    private javax.swing.ButtonGroup functionButtonGroup;
    private jurls.examples.approximation.FunctionRenderer1D functionRenderer1;
    private javax.swing.ButtonGroup hiddenButtonGroup;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JComboBox momentumComboBox;
    private javax.swing.ButtonGroup outputButtonGroup;
    // End of variables declaration//GEN-END:variables
}
