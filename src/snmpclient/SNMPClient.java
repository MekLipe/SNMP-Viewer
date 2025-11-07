package snmpclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SNMPClient {

    private final String servidor;

    public SNMPClient(String servidor) {
        this.servidor = servidor;
    }

    public String consultar(String user, String pass, String oid) {
        try {
            String urlStr = servidor + "?user=" + user + "&pass=" + pass + "&oid=" + oid;
            URL url = new URL(urlStr);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");

            int codigo = conexao.getResponseCode();
            if (codigo != 200) {
                return "Erro: " + codigo;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = in.readLine()) != null) {
                resposta.append(linha);
            }
            in.close();

            return resposta.toString();

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}
