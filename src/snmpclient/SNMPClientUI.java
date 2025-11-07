package snmpclient;

import javax.swing.*;
import java.awt.*;

public class SNMPClientUI extends JFrame {
    private final SNMPClient client;

    public SNMPClientUI(SNMPClient client) {
        this.client = client;
        configurarUI();
    }

    private void configurarUI() {
        setTitle("SNMP Monitor Pro");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel painelTopo = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtUser = new JTextField("admin");
        JTextField txtPass = new JTextField("12345");
        JTextField txtOID = new JTextField("1.3.6.1.2.1.1.3.0");
        JTextArea txtResultado = new JTextArea();
        txtResultado.setEditable(false);

        painelTopo.add(new JLabel("Usuário SNMPv3:"));
        painelTopo.add(txtUser);
        painelTopo.add(new JLabel("Senha:"));
        painelTopo.add(txtPass);
        painelTopo.add(new JLabel("OID:"));
        painelTopo.add(txtOID);

        JButton btnConsultar = new JButton("Consultar");
        painelTopo.add(btnConsultar);

        add(painelTopo, BorderLayout.NORTH);
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnConsultar.addActionListener(e -> {
            String resposta = client.consultar(
                    txtUser.getText(),
                    txtPass.getText(),
                    txtOID.getText()
            );
            txtResultado.setText(resposta);
        });

        setVisible(true);
    }
}
