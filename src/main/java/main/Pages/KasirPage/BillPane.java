package main.Pages.KasirPage;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Pages.KasirPage.ButtonActionListener;
import main.Pages.PaymentPage.*;
import main.Bill.*;
import main.Barang.*;
import main.Client.ClientManager;
import main.Transaksi.*;

public class BillPane extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JTextArea textArea;
    private DetailTransaksi details;
    private JComboBox<String> clientComboBox;
    private ClientManager clientManager;
    private Inventory inventory;
    private BillManager billManager;
    private FixedBillManager fixedBillManager;
    private JTabbedPane tabbedPane;
    private Integer id;

    public BillPane(DetailTransaksi details, Bill bill, Inventory inventory, CustomerTuple customerTuple, ClientManager clientManager, BillManager billManager, FixedBillManager fixedBillManager, JTabbedPane tabbedPane) {
        details = bill.getDetailTransaksi();
        this.inventory = inventory;
        this.clientManager = clientManager;
        this.billManager = billManager;
        this.fixedBillManager = fixedBillManager;
        this.tabbedPane = tabbedPane;
        setLayout(new GridBagLayout());

        // create table and add to scroll pane
        table = new JTable();
        tableModel = new DefaultTableModel();
        table.setModel(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollPane, gbc);

        // add columns to table
        tableModel.addColumn("idBarang");
        tableModel.addColumn("Nama Barang");
        tableModel.addColumn("Jumlah Barang");
        tableModel.addColumn("Subtotal");

        for (int i = 0; i < details.getElement().size(); i++) {
            System.out.println(details.getElement().get(i).getIdBarang());
            Object[] row = new Object[4];
            row[0] = details.getElement().get(i).getIdBarang();
            row[1] = details.getElement().get(i).getNamaBarang();
            row[2] = details.getElement().get(i).getJumlahBarang();
            row[3] = details.getElement().get(i).getSubTotal();
            tableModel.addRow(row);
        }

        // add label for total
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }

        totalLabel = new JLabel("Total harga: " + total);
        gbc.gridy = 2;
        gbc.weightx = 0.0; // reset horizontal weight
        gbc.weighty = 0.2; 
        gbc.fill = GridBagConstraints.NONE; // reset fill
        add(totalLabel, gbc);
                
        // String[] clientNames = {"John (123)", "Jane (456)", "Mark (789)"};
        String[] clientNames = clientManager.getNonCustomerName();
        List<String> originalClientList = Arrays.asList(clientNames);

        JComboBox<String> clientComboBox = new JComboBox<>(clientNames);
        clientComboBox.setEditable(true);
        JTextField clientTextField = (JTextField) clientComboBox.getEditor().getEditorComponent();
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(new JLabel("Nama Client: "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 10);
        add(clientComboBox, gbc);
                
        clientComboBox.addItemListener(new ItemListener()
            {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // get selected item from combobox
                    String selectedClient = clientComboBox.getSelectedItem().toString();

                    if (selectedClient == null || clientComboBox.getSelectedItem().toString().isEmpty()) {
                        customerTuple.setCustomerId(originalClientList.size());
                        customerTuple.setCustomerName("");
                    } else {
                        // parse name and id from selectedClient
                        Pattern pattern = Pattern.compile("^(.*) \\((\\d+)\\)$");
                        Matcher matcher = pattern.matcher(selectedClient);
                        if (matcher.find()) {
                            String name = matcher.group(1);
                            id = Integer.parseInt(matcher.group(2));
                            // do something with name and id
                            System.out.println("Name: " + name);
                            System.out.println("ID: " + id);
                            customerTuple.setCustomerName(name);
                            customerTuple.setCustomerId(id);
                        } else {
                            System.err.println("Invalid selected client format: " + selectedClient);
                        }
                    }
                }
            }
        });
        
        clientComboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // filter the list of client names based on user input
                String input = clientTextField.getText().toLowerCase();
                for (int i = 0; i < clientComboBox.getItemCount(); i++) {
                    String name = clientComboBox.getItemAt(i).toString();
                    if (!name.toLowerCase().contains(input)) {
                        clientComboBox.removeItemAt(i);
                        i--;
                    }
                }
                // compare filtered items with the original list and add missing items
                for (String name : originalClientList) {
                    if (name.toLowerCase().contains(input) && !clientComboBox.getEditor().getItem().equals(name)) {
                        boolean found = false;
                        for (int i = 0; i < clientComboBox.getItemCount(); i++) {
                            if (clientComboBox.getItemAt(i).equals(name)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            clientComboBox.addItem(name);
                        }
                    }
                }
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        
        JButton payButton = new JButton("Bayar");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(payButton, gbc);

        // add action listener to buttons
        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JButton) {
                    JButton button = (JButton) e.getSource();
                    if (button.getText().equals("Save Bill")) {
                        System.out.println("Save");
                    } else if (button.getText().equals("Bayar")) {
                        if (!isStockAvailable(bill, inventory)) {
                            JOptionPane.showMessageDialog(null, "Jumlah stok di inventory berubah, Jumlah stok kini tidak mencukupi jumlah pembelian.");
                        } else {
                            bill.setIdCustomer(id);
                            JPanel newPanel = new PaymentPage(billManager, bill, inventory, fixedBillManager, clientManager);
                            GridBagConstraints gbc = new GridBagConstraints();
                            gbc.gridx = 0;
                            gbc.gridy = 0;
                            gbc.fill = GridBagConstraints.HORIZONTAL;
                            // create a close button and add it to the tab
                            JButton closeButton = new JButton("X");
                            // closeButton.setFont(new Font("Arial", Font.PLAIN, 5));
                            // closeButton.setFont(new Font("Arial", Font.PLAIN, 5));
                            closeButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // remove the tab
                                    tabbedPane.removeTabAt(tabbedPane.indexOfComponent(newPanel));
                                }
                            });
                            // closeButton.setPreferredSize(new Dimension(30,20));
                            JPanel tabPanel = new JPanel(new BorderLayout(3,3));
                            JLabel tabTitle = new JLabel("Payment");
                            tabPanel.add(tabTitle, BorderLayout.WEST);
                            tabPanel.add(closeButton, BorderLayout.EAST);
    
                            // add the panel to the tabbedPane
                            tabbedPane.addTab("Payment", newPanel);
                            tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, tabPanel);
                            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);    
                        }
                    }
                }
            }
        });
    }

    public void updateBill(Bill bill) {
        textArea.setText(bill.toString() + details.toString());
    }

    public void updateBillDetails(DetailTransaksi details) {
        this.details = details;
    
        // clear table model
        tableModel.setRowCount(0);
    
        // add new rows to table model
        for (int i = 0; i < details.getElement().size(); i++) {
            if (details.getElement().get(i).getJumlahBarang() != 0) {
                Object[] row = new Object[4];
                row[0] = details.getElement().get(i).getIdBarang();
                row[1] = details.getElement().get(i).getNamaBarang();
                row[2] = details.getElement().get(i).getJumlahBarang();
                row[3] = details.getElement().get(i).getSubTotal();
                tableModel.addRow(row);
            }
        }
    
        // update total label
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }
        totalLabel.setText("Total harga: " + total);
    }

    private boolean isStockAvailable (Bill bill, Inventory inventory) {
        ArrayList<ElemenDetailTransaksi> arrElemenDT = bill.getDetailTransaksi().getElement();

        for (ElemenDetailTransaksi elemenDT : arrElemenDT) {
            Integer idBarang = elemenDT.getIdBarang();
            Integer paymentQty = elemenDT.getJumlahBarang();
            Integer stock = inventory.getBarangByID(idBarang).getStock();
            if (paymentQty > stock) {
                return false;
            }
        }

        return true;
    }
}

