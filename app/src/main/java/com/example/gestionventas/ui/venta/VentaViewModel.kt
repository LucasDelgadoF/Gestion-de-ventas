package com.example.gestionventas.ui.venta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionventas.data.local.AppDatabase
import com.example.gestionventas.data.local.HistorialEntity
import com.example.gestionventas.data.local.VentaEntity
import com.example.gestionventas.data.repository.VentaRepository
import com.example.gestionventas.model.Catalogo
import com.example.gestionventas.model.ComisionCalculator
import com.example.gestionventas.model.EstadoVenta
import com.example.gestionventas.model.Perfil
import com.example.gestionventas.model.ResumenComisiones
import com.example.gestionventas.model.Usuario
import com.example.gestionventas.model.VentaUiState
import com.example.gestionventas.model.VentaValidador
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtros de la lista (para el perfil Encargado). */
data class FiltrosUi(
    val busqueda: String = "",        // código de autorización
    val estado: String? = null,
    val vendedor: String? = null,
    val fecha: String = ""            // texto contenido en dd/MM/aaaa
)

@OptIn(ExperimentalCoroutinesApi::class)
class VentaViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VentaRepository(AppDatabase.getDatabase(app))

    // ---------------- Sesión ----------------
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun iniciarSesion(u: Usuario) {
        _usuario.value = u
        _filtros.value = FiltrosUi()
        viewModelScope.launch { repository.sembrarDatosDePrueba() }
    }

    fun cerrarSesion() {
        _usuario.value = null
        nuevaVenta()
    }

    // ---------------- Mensajes (snackbar) ----------------
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    fun limpiarMensaje() { _mensaje.value = null }

    // ---------------- Lista de ventas (reactiva: Room -> Flow -> UI) ----------------
    private val _filtros = MutableStateFlow(FiltrosUi())
    val filtros: StateFlow<FiltrosUi> = _filtros

    /** Ventas visibles para el perfil, SIN filtros (base para indicadores y comisiones). */
    private val ventasBase: Flow<List<VentaEntity>> = _usuario.flatMapLatest { u ->
        if (u == null) {
            flowOf(emptyList())
        } else when (u.perfil) {
            Perfil.VENDEDOR -> repository.ventasDeVendedor(u.username)
            // El encargado solo ve ventas ya enviadas (no borradores de los vendedores)
            Perfil.ENCARGADO -> repository.todasLasVentas().map { l -> l.filter { it.estado != EstadoVenta.BORRADOR } }
        }
    }

    val ventas: StateFlow<List<VentaEntity>> = combine(ventasBase, _filtros) { lista, f ->
        lista.filter { v ->
            (f.busqueda.isBlank() || v.codigoAutorizacion.contains(f.busqueda.trim(), ignoreCase = true)) &&
                (f.estado == null || v.estado == f.estado) &&
                (f.vendedor == null || v.vendedor == f.vendedor) &&
                (f.fecha.isBlank() || v.fechaOperacion.contains(f.fecha.trim()))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendedoresDisponibles: StateFlow<List<String>> = ventasBase
        .map { l -> l.map { it.vendedor }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumen: StateFlow<ResumenComisiones> = ventasBase
        .map { ComisionCalculator.calcular(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenComisiones())

    fun onBusquedaChange(v: String) = _filtros.update { it.copy(busqueda = v) }
    fun onFiltroEstado(e: String?) = _filtros.update { it.copy(estado = e) }
    fun onFiltroVendedor(v: String?) = _filtros.update { it.copy(vendedor = v) }
    fun onFiltroFecha(f: String) = _filtros.update { it.copy(fecha = f) }
    fun limpiarFiltros() { _filtros.value = FiltrosUi() }

    // ---------------- Detalle / revisión ----------------
    private val _ventaSeleccionadaId = MutableStateFlow(0L)

    val ventaSeleccionada: StateFlow<VentaEntity?> = _ventaSeleccionadaId.flatMapLatest { id ->
        if (id == 0L) flowOf(null) else repository.observarVenta(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val historial: StateFlow<List<HistorialEntity>> = _ventaSeleccionadaId.flatMapLatest { id ->
        if (id == 0L) flowOf(emptyList()) else repository.observarHistorial(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarVenta(id: Long) { _ventaSeleccionadaId.value = id }

    /**
     * Acciones del encargado: validar / observar (solicitar corrección) / rechazar.
     * Reglas: solo el perfil Encargado, nunca sobre ventas propias, observar y rechazar exigen motivo.
     */
    fun revisar(venta: VentaEntity, nuevoEstado: String, observacion: String, onError: (String) -> Unit) {
        val u = _usuario.value
        if (u == null || u.perfil != Perfil.ENCARGADO) { onError("Solo el encargado puede revisar ventas"); return }
        if (venta.vendedor == u.username) { onError("No puede validar sus propias ventas"); return }
        if (venta.estado !in EstadoVenta.PARA_REVISAR) { onError("La venta no está pendiente de revisión"); return }
        if ((nuevoEstado == EstadoVenta.OBSERVADA || nuevoEstado == EstadoVenta.RECHAZADA) && observacion.isBlank()) {
            onError("Debe registrar una observación o motivo"); return
        }
        val accion = when (nuevoEstado) {
            EstadoVenta.VALIDADA -> "Validación de la venta"
            EstadoVenta.OBSERVADA -> "Solicitud de corrección"
            else -> "Rechazo de la venta"
        }
        viewModelScope.launch {
            repository.actualizarVenta(
                anterior = venta,
                nueva = venta.copy(estado = nuevoEstado, observacionRevision = observacion.trim(), revisadoPor = u.username),
                usuario = u.username, accion = accion, observacion = observacion.trim()
            )
            _mensaje.value = "Venta ${venta.codigoAutorizacion}: $nuevoEstado"
        }
    }

    /** Simula la derivación de una venta validada a su prestador ficticio. */
    fun gestionarConPrestador(venta: VentaEntity) {
        val u = _usuario.value ?: return
        if (u.perfil != Perfil.ENCARGADO || venta.estado != EstadoVenta.VALIDADA) return
        viewModelScope.launch {
            repository.actualizarVenta(
                venta, venta.copy(estado = EstadoVenta.GESTIONADA), u.username,
                "Envío simulado a ${venta.prestador}"
            )
            _mensaje.value = "Venta derivada (simulado) a ${venta.prestador}"
        }
    }

    // ---------------- Formulario ----------------
    private val _estado = MutableStateFlow(VentaUiState())
    val estado: StateFlow<VentaUiState> = _estado

    fun nuevaVenta() { _estado.value = VentaUiState() }

    fun cargarVenta(id: Long) {
        viewModelScope.launch {
            val v = repository.obtenerVenta(id) ?: return@launch
            val u = _usuario.value
            _estado.value = VentaUiState(
                id = v.id,
                tipoServicio = v.tipoServicio,
                fechaOperacion = v.fechaOperacion,
                cliente = v.cliente,
                monto = formatearMonto(v.monto),
                codigoAutorizacion = v.codigoAutorizacion,
                respaldoUri = v.respaldoUri,
                estado = v.estado,
                observaciones = v.observaciones,
                observacionRevision = v.observacionRevision,
                // Solo el dueño puede modificar y únicamente mientras no esté validada/cerrada
                editable = u != null && u.perfil == Perfil.VENDEDOR && v.vendedor == u.username &&
                    EstadoVenta.esEditable(v.estado)
            )
        }
    }

    private fun formatearMonto(m: Double) = if (m % 1.0 == 0.0) m.toLong().toString() else m.toString()

    fun onTipoServicioChange(v: String) = _estado.update { it.copy(tipoServicio = v, tipoServicioError = null) }
    fun onFechaChange(v: String) = _estado.update { it.copy(fechaOperacion = v, fechaError = null) }
    fun onClienteChange(v: String) = _estado.update { it.copy(cliente = v, clienteError = null) }
    fun onMontoChange(v: String) = _estado.update {
        it.copy(monto = v.filter { c -> c.isDigit() || c == '.' || c == ',' }, montoError = null)
    }
    fun onCodigoChange(v: String) = _estado.update { it.copy(codigoAutorizacion = v.uppercase(), codigoError = null) }
    fun onRespaldoChange(uri: String?) = _estado.update { it.copy(respaldoUri = uri, respaldoError = null) }
    fun onObservacionesChange(v: String) = _estado.update { it.copy(observaciones = v) }
    fun consumirGuardado() = _estado.update { it.copy(guardadoExitosamente = false) }

    /**
     * Guarda la venta en Room. enviar=false -> borrador / guarda cambios; enviar=true -> envía a revisión.
     * Todas las validaciones viven aquí (ViewModel + VentaValidador), no en la pantalla.
     */
    fun guardar(enviar: Boolean) {
        val u = _usuario.value ?: return
        val a = _estado.value
        if (!a.editable) return

        val errores = VentaValidador.validar(
            a.tipoServicio, a.fechaOperacion, a.cliente, a.monto,
            a.codigoAutorizacion, a.respaldoUri, exigirRespaldo = enviar
        )
        _estado.update {
            it.copy(
                tipoServicioError = errores.tipoServicio, fechaError = errores.fecha,
                clienteError = errores.cliente, montoError = errores.monto,
                codigoError = errores.codigo, respaldoError = errores.respaldo
            )
        }
        if (errores.hayErrores()) return

        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val codigo = a.codigoAutorizacion.trim()
            // Código de autorización no duplicado (consulta a Room)
            if (repository.existeCodigo(codigo, a.id)) {
                _estado.update { it.copy(guardando = false, codigoError = "Este código de autorización ya está registrado") }
                return@launch
            }
            val anterior = if (a.id != 0L) repository.obtenerVenta(a.id) else null
            if (a.id != 0L && (anterior == null || anterior.vendedor != u.username || !EstadoVenta.esEditable(anterior.estado))) {
                _estado.update { it.copy(guardando = false) }
                _mensaje.value = "No tiene permiso para modificar esta venta"
                return@launch
            }

            val estadoNuevo = when {
                anterior == null -> if (enviar) EstadoVenta.PENDIENTE else EstadoVenta.BORRADOR
                anterior.estado == EstadoVenta.BORRADOR -> if (enviar) EstadoVenta.PENDIENTE else EstadoVenta.BORRADOR
                anterior.estado == EstadoVenta.OBSERVADA -> if (enviar) EstadoVenta.CORREGIDA else EstadoVenta.OBSERVADA
                else -> anterior.estado
            }
            val venta = VentaEntity(
                id = a.id,
                tipoServicio = a.tipoServicio,
                prestador = Catalogo.prestadorDe(a.tipoServicio),
                fechaOperacion = a.fechaOperacion,
                cliente = a.cliente.trim(),
                monto = a.monto.replace(",", ".").toDouble(),
                codigoAutorizacion = codigo,
                respaldoUri = a.respaldoUri,
                estado = estadoNuevo,
                observaciones = a.observaciones.trim(),
                vendedor = anterior?.vendedor ?: u.username,
                observacionRevision = anterior?.observacionRevision ?: "",
                revisadoPor = anterior?.revisadoPor
            )
            if (anterior == null) {
                repository.crearVenta(venta, u.username, if (enviar) "Creación y envío a revisión" else "Creación de borrador")
            } else {
                val accion = when {
                    estadoNuevo == EstadoVenta.CORREGIDA -> "Corrección enviada"
                    enviar -> "Envío a revisión"
                    else -> "Modificación de antecedentes"
                }
                repository.actualizarVenta(anterior, venta, u.username, accion)
            }
            _estado.update { it.copy(guardando = false, guardadoExitosamente = true) }
            _mensaje.value = if (enviar) "Venta enviada a revisión" else "Venta guardada"
        }
    }

    /** Un vendedor solo puede eliminar sus propios borradores. */
    fun eliminarBorrador(venta: VentaEntity) {
        val u = _usuario.value ?: return
        if (venta.vendedor != u.username || venta.estado != EstadoVenta.BORRADOR) {
            _mensaje.value = "Solo se pueden eliminar borradores propios"; return
        }
        viewModelScope.launch {
            repository.eliminarVenta(venta)
            _mensaje.value = "Borrador eliminado"
        }
    }
}
