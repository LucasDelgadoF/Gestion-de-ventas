# Informe de desarrollo y evidencias (plantilla con 10 hitos)

> Reemplace cada sección "Evidencia" por sus capturas reales. Cada hito: nombre, descripción, evidencia visual y resultado.

| # | Hito | Qué mostrar (captura) | Resultado esperado |
|---|---|---|---|
| 1 | Estructura del proyecto | Árbol de paquetes `data/ model/ camera/ ui/` en Android Studio | Responsabilidades claras por paquete |
| 2 | Arquitectura MVVM | `VentaViewModel` (UiState + StateFlow) y `VentaRepository` → `VentaDao` | Pantallas sin lógica de negocio |
| 3 | Navegación | `AppNavigation.kt` y la secuencia Login → Lista → Formulario | Navegación fluida con animaciones |
| 4 | Listado de ventas | `ListaVentasScreen` con código, servicio, monto y estado | Lista leída desde Room |
| 5 | Formulario de ventas | `VentaFormScreen` creando y modificando una venta | Registro y edición funcionando |
| 6 | Validaciones | Errores en rojo con ícono (monto 0, código duplicado, campos vacíos) | Errores controlados desde el ViewModel |
| 7 | Gestión de estado | `VentaUiState` y `collectAsState()` | UI se actualiza sola |
| 8 | Persistencia con Room | Venta guardada → cerrar app → reabrir y seguir visible | Datos persisten |
| 9 | Integración de la cámara | Foto de respaldo en el formulario y lectura de QR en el código | Respaldo vinculado a la venta |
| 10 | Pruebas de funcionamiento | Flujo completo: vendedor envía → encargado valida/observa/rechaza → historial | Trazabilidad visible |

Extras para nota 7.0: capturas de `DetalleRevisionScreen`, `ComisionesScreen`, filtros del encargado y `./gradlew test` en verde.
Recuerde: commits de **ambos** integrantes, tablero Trello y README con sus nombres.
