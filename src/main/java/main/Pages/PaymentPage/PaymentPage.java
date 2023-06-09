package main.Pages.PaymentPage;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import main.Bill.*;
import main.Barang.*;
import main.Client.*;
import main.Laporan.Laporan;
import main.Laporan.LaporanFixedBill;
import main.Observer.*;
import main.Transaksi.DetailTransaksi;
import main.Transaksi.ElemenDetailTransaksi;  

public class PaymentPage extends JPanel implements ActionListener, Subscriber {
    private JButton saveButton;
    private JButton addDiscFromPointButton;
    private JButton process;
    private JLabel pengantar;
    private JLabel total;
    final int WIDTH = 700, HEIGHT = 400;

    private BillManager billmanager;
    private ClientManager clientManager;
    private Inventory inventory;
    private FixedBillManager fixedbillmanager;
    private Bill currentbill;
    private Integer clientId;

    public PaymentPage (BillManager billmanager, Bill currentbill, Inventory inventory, FixedBillManager fixedbillmanager, ClientManager clientManager) {
        // Pass the bill object to the attributes
        this.billmanager = billmanager;
        this.clientManager = clientManager;
        this.inventory = inventory;
        this.fixedbillmanager = fixedbillmanager;
        this.currentbill = currentbill;
        this.billmanager.observer.subscribe(this);
        this.fixedbillmanager.observer.subscribe(this);
        
        // Create the table with some sample data
        List<Bill> listbill = this.billmanager.getListBill();
        int index = listbill.indexOf(currentbill);
        if (index != -1) {
            clientId = this.currentbill.getIdCustomer();
            if (this.currentbill.getIdCustomer() == null) {
                clientManager.generateCustomer();
                this.currentbill.setIdCustomer(clientManager.getLastClientID());
                clientId = clientManager.getLastClientID();
            }
            this.currentbill.recalculateNominal();

            // Get the clientid
            System.out.println(clientId);
            // Jika aktif dan VIP, maka flat disc 10%
            if (clientManager.getClientActivity(clientId) && clientManager.getClientType(clientId) == 1) {
                this.currentbill.setNominal(this.currentbill.getNominal() * 0.9);
            }

            ArrayList<Object[]> data = new ArrayList<>();

            DetailTransaksi details = this.currentbill.getDetailTransaksi();
            for (int i = 0; i < details.getElement().size(); i++) {
                Object[] row = new Object[4];
                row[0] = details.getElement().get(i).getJumlahBarang();
                row[1] = details.getElement().get(i).getIdBarang();
                row[2] = details.getElement().get(i).getNamaBarang();
                row[3] = details.getElement().get(i).getSubTotal();
                data.add(row);
            }

            JTable table = new JTable(data.toArray(new Object[data.size()][]), new String[] {"Qty", "ID Barang", "Nama Barang", "Sub Total"});

            // Create a panel to hold the buttons
            JPanel buttonPanel = new JPanel();
            if (clientManager.getClientActivity(clientId) && (clientManager.getClientType(clientId) == 1 || clientManager.getClientType(clientId) == 0)) {
                JLabel points = new JLabel("Points : " + clientManager.getClientPoint(clientId));
                points.setFont(new Font("Arial", ALLBITS, 10));
                buttonPanel.add(points);
                addDiscFromPointButton = new JButton("Add Discount");
                buttonPanel.add(this.addDiscFromPointButton);
                addDiscFromPointButton.addActionListener(this);
            }
            saveButton = new JButton("Save");
            buttonPanel.add(this.saveButton);
            saveButton.addActionListener(this);

            // Create a panel to hold the table
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
            
            // Create a panel to hold both the button panel and the table panel
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            mainPanel.add(buttonPanel, gbc);
        
            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.pengantar = new JLabel("Daftar Barang");
            this.pengantar.setFont(new Font("Arial", ALLBITS, 18));
            mainPanel.add(this.pengantar, gbc);

            gbc.gridy++;
            mainPanel.add(tablePanel, gbc);

            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.total = new JLabel("Total : " + this.currentbill.getNominal().toString());
            this.total.setFont(new Font("Arial", ALLBITS, 20));
            mainPanel.add(this.total, gbc);

            gbc.gridy++;
            this.process = new JButton("Process");
            process.addActionListener(this);
            mainPanel.add(this.process, gbc);
            
            // Set up the frame
            setSize(WIDTH, HEIGHT);
            setVisible(true);
            
            // Add the main panel to the content pane
            add(mainPanel);
        }
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == this.saveButton) {
            System.out.println("Button 2 clicked!");
            ArrayList<ElemenDetailTransaksi> elemen = this.currentbill.getDetailTransaksi().getElement();
            Thread pdfThread = new Thread(() -> {
                try {
                    Laporan laporan = new LaporanFixedBill(elemen, "Fixed_bill");
                    laporan.generatePDF();
                } catch (IOException exception) {
                    System.out.println(exception.getMessage());
                }
            });
            pdfThread.start();
            Thread popupThread = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "PDF berhasil di-download!");
                    });
                } catch (InterruptedException exception) {
                    System.out.println(exception.getMessage());
                }
            });
            popupThread.start();
        } else if (e.getSource() == this.process) {
            int index = this.billmanager.getListBill().indexOf(currentbill);
            if (index != -1) {
                // Membuat fixed bill
                FixedBill fixed = new FixedBill(this.currentbill.getIdCustomer(), this.currentbill.getDetailTransaksi());
                this.fixedbillmanager.addFixedBill(fixed);
                // Mengurangi nilai barang dari inventory
                for (ElemenDetailTransaksi elemen : this.currentbill.getDetailTransaksi().getElement()) {
                    this.inventory.changeStock(elemen.getIdBarang(), -1 * elemen.getJumlahBarang());
                }
                // Tambah poin sejumlah perhitungan nominal transaksi
                clientManager.changeClientPoint(clientId, clientManager.getClientPoint(clientId) + this.billmanager.getListBill().get(index).getNominal() * 0.01);
                // Delete dari bill
                this.billmanager.deleteBill(this.currentbill);
                JOptionPane.showMessageDialog(null, "Transaksi berhasil diproses.");
            } else {
                JOptionPane.showMessageDialog(null, "Transaksi ini sudah pernah diproses sebelumnya.");
            }
        }
    }

    public void update() {
        //
    }
}