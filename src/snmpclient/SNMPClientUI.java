package snmpclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SNMPClientUI extends JFrame {

    private final SNMPClient client;
    
    // Autenticação
    private JTextField userField;
    private JPasswordField passField;
    
    // Lista Dinâmica
    private JPanel oidsPanel;
    private List<JTextField> oidFieldsList;
    
    // Resultado
    private JTextArea resultArea;

    public SNMPClientUI(SNMPClient client) {
        this.client = client;
        this.oidFieldsList = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        setTitle("Cliente SNMP - Múltiplas Consultas");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Painel de Login
        JPanel authPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authPanel.setBorder(BorderFactory.createTitledBorder("Autenticação"));
        
        userField = new JTextField("admin", 10);
        passField = new JPasswordField("12345", 10);
        
        authPanel.add(new JLabel("Usuário:"));
        authPanel.add(userField);
        authPanel.add(new JLabel("Senha:"));
        authPanel.add(passField);

        // 2. Painel de OIDs (Lista Dinâmica)
        oidsPanel = new JPanel();
        oidsPanel.setLayout(new BoxLayout(oidsPanel, BoxLayout.Y_AXIS));

        // Adiciona um campo inicial padrão
        adicionarCampoOID("1.3.6.1.2.1.1.3.0");

        JScrollPane scrollOids = new JScrollPane(oidsPanel);
        scrollOids.setPreferredSize(new Dimension(550, 150));
        scrollOids.setBorder(BorderFactory.createTitledBorder("Lista de OIDs"));

        // Botões de Ação
        JButton btnAdicionar = new JButton("Adicionar OID (+)");
        btnAdicionar.addActionListener(e -> adicionarCampoOID(""));

        JButton btnConsultar = new JButton("CONSULTAR TODOS");
        btnConsultar.setFont(new Font("Arial", Font.BOLD, 14));
        btnConsultar.addActionListener(new ConsultarAction());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.add(btnAdicionar);
        actionPanel.add(btnConsultar);

        // 3. Painel de Resultado
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollResult = new JScrollPane(resultArea);
        scrollResult.setBorder(BorderFactory.createTitledBorder("Resultados"));

        // Montagem Final
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(authPanel, BorderLayout.NORTH);
        topContainer.add(scrollOids, BorderLayout.CENTER);
        topContainer.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(scrollResult, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private void adicionarCampoOID(String valorInicial) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campo = new JTextField(valorInicial, 30);
        JButton btnRemover = new JButton("X");
        
        btnRemover.setForeground(Color.RED);
        btnRemover.setMargin(new Insets(2, 5, 2, 5)); // Botão pequeno

        // Remove da tela e da lista lógica
        btnRemover.addActionListener(e -> {
            oidsPanel.remove(linha);
            oidFieldsList.remove(campo);
            oidsPanel.revalidate();
            oidsPanel.repaint();
        });

        linha.add(campo);
        linha.add(btnRemover);

        oidsPanel.add(linha);
        oidFieldsList.add(campo);
        
        oidsPanel.revalidate();
    }

    private class ConsultarAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resultArea.setText(""); // Limpa tela
            
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if (oidFieldsList.isEmpty()) {
                resultArea.append("Nenhum OID para consultar.\n");
                return;
            }

            resultArea.append("Iniciando consulta múltipla...\n\n");

            // Loop fazendo as requisições sequenciais
            for (JTextField campo : oidFieldsList) {
                String oid = campo.getText().trim();
                if (!oid.isEmpty()) {
                    try {
                        String resposta = client.get(oid, user, pass);
                        // Filtra o JSON só pra ficar bonito no log (opcional)
                        resultArea.append("OID: " + oid + "\n   -> " + resposta.trim() + "\n--------------------\n");
                    } catch (Exception ex) {
                        resultArea.append("OID: " + oid + "\n   -> ERRO: " + ex.getMessage() + "\n--------------------\n");
                    }
                }
            }
            resultArea.append("Consulta finalizada.");
        }
    }

    public static void main(String[] args) {
        // Construtor simplificado
        SNMPClient cliente = new SNMPClient("localhost", 16100);
        SwingUtilities.invokeLater(() -> new SNMPClientUI(cliente));
    }
}