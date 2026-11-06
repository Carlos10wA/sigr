package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class PedidoForm {

    @Valid
    private List<PedidoLineaForm> lineas = new ArrayList<>();

    public List<PedidoLineaForm> getLineas() { return lineas; }
    public void setLineas(List<PedidoLineaForm> lineas) { this.lineas = lineas; }
}
