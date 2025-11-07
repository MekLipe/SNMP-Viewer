package snmpserver;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MIBData {

    private final Map<String, String> mib = new HashMap<>();

    public MIBData() {
        atualizarDados();
    }

    public void atualizarDados() {
        mib.put("1.3.6.1.2.1.1.3.0", LocalTime.now().toString()); // sysUpTime
        mib.put("1.3.6.1.4.1.2021.4.6.0", String.valueOf(512 + (int)(Math.random() * 512))); // Memória disponível
        mib.put("1.3.6.1.4.1.2021.10.1.3.1", String.valueOf((int)(Math.random() * 100))); // CPU Load
    }

    public String getValor(String oid) {
        return mib.getOrDefault(oid, "OID não encontrado");
    }

    public Map<String, String> getMibCompleta() {
        return mib;
    }
}
