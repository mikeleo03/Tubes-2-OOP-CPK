package main.Pages.KasirPage;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.*;
import javax.swing.*;

import main.Transaksi.DetailTransaksi;
import main.Barang.*;
import main.Bill.*;

public class PelangganPanel extends JPanel implements ActionListener {
    private JButton closeButton;
    private BillPane billPane;
    private ItemsDisplay itemsDisplay;
    private Inventory testInventory;
    private KasirPage kasirPage;
    private int pelangganId;
    private Bill bill;
    private CustomerTuple customerTuple;

    public PelangganPanel(int pelangganId, KasirPage kasirPage, Bill bill, CustomerTuple customerTuple) {
        this.pelangganId = pelangganId;
        this.kasirPage = kasirPage;
        this.customerTuple = customerTuple;
        this.bill = bill;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        // Membuat button untuk menutup panel
        closeButton = new JButton("Tutup");
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Menghapus panel dan state terkait dari stateMap
                String panelName = kasirPage.getPanelMap().get(PelangganPanel.this);
                kasirPage.getStateMap().remove(panelName);
                kasirPage.getPanelMap().remove(PelangganPanel.this);
                kasirPage.getBillMap().remove(panelName);
                kasirPage.getTabbedPane().remove(PelangganPanel.this);
            }
        });
        add(closeButton);

        gbc = new GridBagConstraints();

        // Menambahkan BillPane ke panel
        DetailTransaksi details = new DetailTransaksi();
        this.billPane = new BillPane(details, this.bill, this.customerTuple);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(this.billPane, gbc);
    
        this.testInventory = new Inventory();
        Barang b = new Barang("Test", 10, 5.0, 2.0, "Food", "../img/test.jpg");
        for (int i =0; i<28;i++) {
            this.testInventory.addBarang(b);
        }

        // Menambahkan ItemsDisplay ke panel
        ItemsDisplay itemsDisplay = new ItemsDisplay(this.testInventory, bill, this);

        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        JScrollPane itemDisplayScroll = new JScrollPane(itemsDisplay);
        // itemDisplayScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        itemDisplayScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        itemDisplayScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(itemDisplayScroll, gbc);

        //Menambahkan panel ke dalam tabbedPane
        kasirPage.getTabbedPane().addTab(kasirPage.getPanelMap().get(this), this);
        kasirPage.getTabbedPane().setSelectedComponent(this);
    }

    public void updateBillPane() {
        this.billPane.updateBillDetails(this.bill.getDetailTransaksi());
        this.billPane.revalidate();
        this.billPane.repaint();
        this.revalidate();
        this.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.getClass() == QtyButton.class) {
            updateBillPane();
        }
    }
    
}