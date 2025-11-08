package snmpclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SNMPClientUI extends JFrame {

    private final SNMPClient client;
    private JTextField oidField;
    private JTextArea resultArea;

    // ✅ Construtor que recebe o cliente
    public SNMPClientUI(SNMPClient client) {
        this.client = client;
        initUI();
    }

    // ✅ Interface gráfica
    private void initUI() {
        setTitle("Cliente SNMP v3");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel oidLabel = new JLabel("Digite o OID:");
        oidField = new JTextField();

        JButton consultarButton = new JButton("Consultar");
        consultarButton.addActionListener(new ConsultarAction());

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(oidLabel, BorderLayout.WEST);
        topPanel.add(oidField, BorderLayout.CENTER);
        topPanel.add(consultarButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        setVisible(true);
    }

    // ✅ Botão de consulta SNMP
    private class ConsultarAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String oid = oidField.getText().trim();

            if (oid.isEmpty()) {
                JOptionPane.showMessageDialog(
                        SNMPClientUI.this,
                        "Por favor, insira um OID para consulta.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                String resposta = client.get(oid);
                resultArea.append("OID: " + oid + "\nValor: " + resposta + "\n\n");
            } catch (Exception ex) {
                resultArea.append("Erro ao consultar OID: " + oid + "\n" + ex.getMessage() + "\n\n");
            }
        }
    }

    // 🚀 Método main para rodar o cliente gráfico
    public static void main(String[] args) {
        // Configura o cliente SNMP apontando para o servidor
        SNMPClient cliente = new SNMPClient("localhost", 16100, "admin", "12345");

        // Abre a interface gráfica
        SwingUtilities.invokeLater(() -> new SNMPClientUI(cliente));
    }
}
