package co.edu.tecnologicaoriente.sigr.util;

public final class CsvUtil {
    private CsvUtil() {}

    public static String escapar(Object valor) {
        String s = valor == null ? "" : valor.toString();
        // Protección contra fórmulas en hojas de cálculo.
        String t = s.trim();
        if (!t.isEmpty() && (t.charAt(0) == '=' || t.charAt(0) == '+' || t.charAt(0) == '-' || t.charAt(0) == '@' || t.charAt(0) == '\t')) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
