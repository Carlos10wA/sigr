// Actualización periódica del estado de pedidos sin recargar la página (detalles y listado).
(function () {
  var INTERVALO_MS = 10000;
  var enCurso = false;
  var pausa = false;

  function idsVisibles() {
    var ids = [];
    document.querySelectorAll("[data-estado-pedido]").forEach(function (el) {
      ids.push(el.getAttribute("data-estado-pedido"));
    });
    return ids;
  }

  function cerrar() {
    pausa = true;
    clearInterval(timer);
  }

  function sondear() {
    if (pausa) return;
    if (document.hidden) return; // Evita solicitudes cuando la página no está visible.
    if (enCurso) return; // Evita peticiones superpuestas.
    var ids = idsVisibles();
    if (ids.length === 0) return;
    enCurso = true;
    Promise.all(ids.map(function (id) {
      return fetch("/pedidos/" + id + "/estado", { headers: { "Accept": "application/json" } })
        .then(function (resp) { return resp.ok ? resp.json() : null; })
        .catch(function () { return null; }); // Si falla la conexión, conserva el estado visible.
    })).then(function (resultados) {
      resultados.forEach(function (data) {
        if (!data || !data.nombre) return;
        document.querySelectorAll('[data-estado-pedido="' + data.id + '"]').forEach(function (el) {
          if (el.textContent !== data.nombre) {
            el.textContent = data.nombre;
          }
        });
      });
      enCurso = false;
    }, function () { enCurso = false; });
  }

  var timer = setInterval(sondear, INTERVALO_MS);
  window.addEventListener("beforeunload", cerrar);
})();
