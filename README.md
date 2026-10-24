# Gestión móvil del registro y validación de ventas — DSY1105 (Evaluación Parcial 2)

App Android (Kotlin + Jetpack Compose + Material 3) con arquitectura MVVM y persistencia local con Room.
Caso: `Caso_DSY1105_004D` — Agencia de convenios y beneficios. **Todos los datos, cuentas, montos y comisiones son ficticios.**

## Integrantes
- _Nombre Apellido 1_
- _Nombre Apellido 2_

## Funcionalidades implementadas
**Perfil Vendedor**
- Inicio de sesión con cuenta ficticia (clave guardada como hash SHA-256).
- `ListaVentasScreen`: ventas propias desde Room (reactiva), búsqueda por código, filtro por estado/fecha, alerta de ventas observadas.
- `VentaFormScreen`: registrar y modificar venta (tipo de convenio, fecha con calendario, cliente, monto, código de autorización, observaciones).
- Validaciones desde el ViewModel (`VentaValidador`): obligatorios, monto numérico > 0, código con formato y **no duplicado** (consulta a Room + índice único).
- Guardar borrador / enviar a revisión / enviar corrección de una venta observada.
- Recurso nativo 1 (cámara): foto del respaldo con CameraX, guardada en almacenamiento interno y vinculada a la venta.
- Recurso nativo 2 (cámara + ML Kit): lectura de código QR ficticio para completar el código de autorización.
- Selección de imagen desde galería (solo JPG/PNG, máx. 5 MB).

**Perfil Encargado (extra para nota alta)**
- Lista de ventas recibidas con filtros por estado, vendedor y fecha, y búsqueda por código.
- `DetalleRevisionScreen`: ver antecedentes y respaldo; **Validar**, **Solicitar corrección** (Observada) o **Rechazar** con observación obligatoria.
- No puede validar ventas propias; las ventas validadas no pueden ser modificadas por el vendedor.
- Historial de cambios (estado anterior → nuevo, usuario, fecha y hora) por cada acción.
- `ComisionesScreen`: indicadores generales, comisiones **simuladas** por vendedor y ventas pendientes de derivar a prestador (envío simulado).
- Alerta interna de ventas pendientes de revisión.

## Cuentas de prueba (ficticias)
| Usuario | Clave | Perfil |
|---|---|---|
| vendedor1 | vend123 | Vendedor |
| vendedor2 | vend123 | Vendedor |
| encargado | enc123 | Encargado |

Al primer ingreso se cargan 5 ventas ficticias de ejemplo.

## Estructura
```
app/src/main/java/com/example/gestionventas/
├── data/local/        VentaEntity, HistorialEntity, VentaDao, AppDatabase (Room)
├── data/repository/   VentaRepository, AuthRepository
├── model/             VentaUiState, VentaErrores, VentaValidador, EstadoVenta, Catalogo, ...
├── camera/            CameraPermissionHelper, QrScanner, CamaraRespaldo, ImageStorage
├── ui/navigation/     AppNavigation
├── ui/venta/          Login, ListaVentas, VentaForm, DetalleRevision, Comisiones, ViewModels
└── MainActivity.kt
```
Flujo MVVM: `Screen (Compose)` → eventos `onXChange` → `VentaViewModel` (UiState + StateFlow, validaciones) → `VentaRepository` → `VentaDao` → Room.

## Cómo ejecutar
1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync* (usa las mismas versiones de Gradle/AGP/Kotlin/KSP de las clases).
2. Ejecutar en un emulador con cámara (Device Manager → Additional settings → Camera: Webcam0/Emulated) o en un teléfono.
3. Aceptar el permiso de cámara al tomar la foto o leer el QR.
4. Pruebas unitarias: `./gradlew test`.

## Notas académicas
- No se usan datos reales, pagos ni conexión a sistemas externos (Partner).
- Los archivos de respaldo se guardan en `filesDir/respaldos` y pueden borrarse con `ImageStorage.eliminarTodos`.
