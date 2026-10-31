# Control de Gastos e Ingresos — Arquitectura

**Autor:** Kervin Saul Garcia Rodriguez · Programación II · Versión 1.0.0

App Android en Kotlin, sin login, para registrar a mano gastos e ingresos y llevar un **control por mes o por año**, con balance, alertas, estadísticas y guardado en **SQLite**.

El proyecto tiene dos partes:

| Parte | Qué es | Dónde |
|---|---|---|
| **finanzas-core** (v1.0.1) | Librería Kotlin pura (sin Android): modelo, reglas del negocio y contratos de datos. Se prueba en consola. | `src/main/kotlin/edu/umg/programacion2/finanzas` y `pom.xml` (Maven), en la raíz del repositorio |
| **AppGastos** | App Android que usa la librería y le pone pantallas, SQLite, notificaciones y seguridad. | Carpeta `app` (Gradle), en Android Studio |

**Estructura del repositorio:**

```
ProyectoFinal
├── app/                  ← la app de Android (paquete edu.umg.programacion2.appgastos)
├── src/main/kotlin/...   ← la librería finanzas-core (paquete edu.umg.programacion2.finanzas)
├── pom.xml               ← compila la librería con Maven
├── build.gradle.kts, settings.gradle.kts, gradle/, gradlew   ← compilan la app con Gradle
└── ARQUITECTURA.md
```

---

## 1. Cómo correrla

1. **Instalar la librería primero** (la app la busca en `.m2`): abrir el repositorio en IntelliJ → pestaña Maven → `clean` → `install` (debe decir `BUILD SUCCESS`). O desde una terminal en la raíz: `mvn clean install`.
2. En Android Studio, abrir **AppGastos** → `Ctrl + Shift + O` (sincronizar) → ▶ Run.

Dependencias de la app (en `app/build.gradle.kts`): `finanzas-core:1.0.1` (desde `mavenLocal()`, por eso hay que hacer `install` antes), `lifecycle-runtime-ktx` (corrutinas) y `biometric` (huella).

---

## 2. Requisitos del enunciado y dónde están

| Requisito | Dónde está |
|---|---|
| Gastos, salario y ventas | `Gasto`, `Ingreso`, `TipoIngreso` (librería) · `HojaRegistro` |
| Crear los tipos de gasto que quiera | `Categoria` + `CategoriaRepository` (librería) · `DialogoCategoria`, `SeccionCategorias` |
| Campos con montos | `HojaRegistro` (descripción, monto, fecha, categoría) |
| Balance y total | `ControlGastos.balanceDelMes/balanceDelAnio` · `VistaResumen` |
| Rojo y notificación si va en negativo | `EstadoBalance.NEGATIVO` · `VistaResumen` (tarjeta roja) · `AvisosFinancieros` |
| Dólares con tipo de cambio por API REST | `TipoCambio` (open.er-api.com) · `ConversionDolares` · `SeccionMoneda` |
| Sin login, datos manuales | No hay login. El PIN es **opcional** y viene apagado. |
| Al mes un nuevo control | El mes sale de la fecha (`2026-10-03` → `2026-10`); `Periodo` y `balanceDelMes` |

---

## 3. Arquitectura en capas

```
┌──────────────────────────── App Android ────────────────────────────┐
│  ui/          Pantallas (Activities) y piezas visuales               │
│    principal/ estadisticas/ ajustes/ comun/                          │
│        │ usan                                                        │
│        ▼                                                             │
│  logica/      Reglas propias de la app, SIN Android                  │
│  avisos/  seguridad/  red/                                           │
│        │ usan                                                        │
│        ▼                                                             │
│  datos/       SQLite (repositorios) y ajustes guardados              │
└────────┬─────────────────────────────────────────────────────────────┘
         │ implementa las interfaces de
         ▼
┌──────────────────────── finanzas-core (librería) ───────────────────┐
│  servicio/ControlGastos  →  datos/*Repository (interfaces)           │
│  modelo/ (Gasto, Ingreso, Categoria, BalanceMensual…)                │
└──────────────────────────────────────────────────────────────────────┘
```

**Regla principal:** las pantallas nunca tocan SQLite directo. Todo pasa por `ControlGastos`, que solo conoce **interfaces** (patrón DAO / repositorio). Por eso pasar de "en memoria" a SQLite no cambió ni una línea de las reglas del negocio: solo se cambió qué repositorio se le pasa en `Datos.iniciar()`.

---

## 4. Paquetes y clases

Cada clase tiene **una sola responsabilidad**. Ningún archivo pasa de 200 líneas.

### `datos/` — guardar y leer
| Clase | Responsabilidad |
|---|---|
| `BaseDatos` | `SQLiteOpenHelper`: crea las tablas y hace la migración de versión |
| `GastoRepositorySQLite` / `IngresoRepositorySQLite` / `CategoriaRepositorySQLite` | Implementan los contratos de la librería con SQL (`INSERT`, `SELECT`, `UPDATE`, `DELETE`) |
| `Datos` | Crea **un solo** `ControlGastos` para toda la app con los repositorios SQLite |
| `Preferencias` | Ajustes en `SharedPreferences` (dólares, PIN cifrado, recordatorio, límites…) |

### `logica/` — reglas sin Android
| Clase | Responsabilidad |
|---|---|
| `Periodo` | Qué mes o año se ve, flechas ‹ ›, Por mes / Por año, fecha por defecto |
| `GuardadoMovimiento` | Decide si registrar, actualizar o **cambiar de tipo** (gasto ↔ ingreso) sin perder datos |
| `ConversionDolares` | Dólares → quetzales, nota "(US$ …)" y lectura de montos |
| `CalculosEstadisticas` | Últimos 6 meses, comparación con el mes anterior y proyección de fin de mes |

### `ui/principal/` — pantalla principal
| Clase | Responsabilidad |
|---|---|
| `MainActivity` | **Solo coordina**: conecta botones con las piezas y refresca |
| `VistaResumen` | Pinta título, Por mes/Por año, tarjeta de balance, dona y leyenda |
| `ListaMovimientos` | Pinta la lista; tocar = editar, mantener = eliminar |
| `HojaRegistro` | Hoja para registrar/editar (moneda, fecha, categoría nueva) |
| `SelectorMes` | Hoja para escoger mes y año; marca los meses con datos |
| `DialogoEliminar` / `DialogoHistorial` | Confirmar borrado / balance de cada mes |

### `ui/estadisticas/`
`EstadisticasActivity` (coordina) + `TarjetaBarras`, `TarjetaComparacion`, `TarjetaProyeccion` (una por tarjeta).

### `ui/ajustes/`
`AjustesActivity` (coordina) + una clase por tarjeta: `SeccionMoneda`, `SeccionCategorias`, `SeccionSeguridad` (+ `DialogoCrearPin`), `SeccionNotificaciones`, `SeccionPresupuestos` y `PermisoNotificaciones`.

### `ui/comun/` — reutilizable
`Estilo` (colores, íconos, formato de dinero), `GraficaDona` y `GraficaBarras` (dibujadas con Canvas, sin librerías), `DialogoCategoria` y `Pantallas` (tema oscuro, hojas, ayudantes).

### `seguridad/`
| Clase | Responsabilidad |
|---|---|
| `PantallaProtegida` | **Clase base** de todas las pantallas: abre datos y ajustes y revisa el PIN al entrar |
| `Sesion` | Sabe si ya se puso el PIN; lo "olvida" al salir de la app (no al girar el teléfono) |
| `PantallaBloqueo` / `DialogoRecuperarPin` | Pantalla del PIN y "¿Olvidaste tu PIN?" |
| `Biometria` | Huella o rostro con `BiometricPrompt` |

### `avisos/` y `red/`
`Notificaciones` (canales, mostrar, programar recordatorio), `RecordatorioReceiver` (suena a la hora y al prender el teléfono), `AvisosFinancieros` (80 %/100 % del presupuesto y balance negativo) y `TipoCambio` (API REST).

---

## 5. Flujo de un registro (de punta a punta)

1. El usuario toca **+** → `MainActivity` abre `HojaRegistro` con la fecha por defecto de `Periodo`.
2. Llena los campos y toca **Guardar** → `HojaRegistro` convierte dólares con `ConversionDolares` si aplica.
3. `GuardadoMovimiento` decide la operación y llama a `ControlGastos` (librería), que **valida**.
4. `ControlGastos` usa `GastoRepositorySQLite` → `INSERT` en la tabla `gasto`.
5. `MainActivity.alGuardar()` mueve el `Periodo` a esa fecha, refresca y llama a `AvisosFinancieros`.
6. Si el mes quedó en negativo: tarjeta roja + aviso en pantalla + notificación del sistema (una vez por mes).

---

## 6. Base de datos (`finanzas.db`)

```sql
gasto     (id INTEGER PK AUTOINCREMENT, descripcion TEXT, monto REAL, categoria TEXT, fecha TEXT)
ingreso   (id INTEGER PK AUTOINCREMENT, descripcion TEXT, monto REAL, tipo TEXT,      fecha TEXT)
categoria (id INTEGER PK AUTOINCREMENT, nombre TEXT UNIQUE COLLATE NOCASE, icono TEXT)
```

- Índices en `fecha`, porque casi todo se consulta por mes o por año.
- Las fechas son texto `yyyy-MM-dd`: se ordenan bien como texto y el mes es `substring(0, 7)`.
- `gasto` se lee con **`LEFT JOIN categoria`** para traer el ícono de las categorías personalizadas. Es `LEFT` porque las de fábrica no están en esa tabla.
- **Migración v1 → v2:** solo agrega la tabla `categoria`; no borra datos.
- Se puede ver en vivo: Android Studio → **App Inspection → Database Inspector**.

---

## 7. Preguntas frecuentes (decisiones técnicas)

**¿Por qué una librería aparte?** Para separar las reglas del negocio de Android: se prueban en consola, se reutilizan y la app solo pone la parte visual. Es lo que se trabajó en la clase 12.

**¿Por qué `SQLiteOpenHelper` y no Room?** No necesita plugins de Gradle extra, se escribe con SQL igual que en los laboratorios de MySQL/JDBC, y los repositorios cumplen el mismo contrato. Cambiar a Room después solo requiere otra implementación de las interfaces.

**¿Por qué `Categoria` es clase y no `enum`?** Un `enum` es fijo; el enunciado pide crear categorías propias. Las 7 de fábrica siguen como constantes (`Categoria.COMIDA`…) y la igualdad es por nombre sin importar mayúsculas.

**¿Por qué funciones `suspend`?** Leer o escribir en la base y consultar internet puede tardar. Se ejecutan en corrutinas (`Dispatchers.IO`) para no congelar la pantalla.

**¿Por qué las gráficas son propias?** Con `Canvas` no se agrega ninguna librería y se controla el diseño. Son clases `View` normales usadas desde el XML.

**¿Cómo se guarda el PIN?** No se guarda el PIN, sino su **hash SHA-256**. La respuesta secreta igual. Si se olvida: pregunta secreta o "Empezar de cero".

**¿Qué pasa sin internet?** Se usa el último tipo de cambio guardado, o se puede escribir a mano en Ajustes.

**¿Por qué el recordatorio puede llegar unos minutos tarde?** Usa `setWindow` (ventana de 10 min) para no pedir el permiso especial de alarmas exactas. Se reprograma solo al prender el teléfono.

---

## 8. Limitaciones conocidas

- El dinero usa `Double`; para un sistema contable real se usaría `BigDecimal`.
- Los datos viven solo en el teléfono (sin respaldo en la nube).
- Los íconos son emojis, para no depender de archivos de imagen.
- El clima de Jutiapa (opcional en el enunciado) no se implementó.

## 9. Convenciones del código

- Nombres y comentarios en español.
- Una responsabilidad por clase; los Activities solo coordinan.
- Ningún archivo pasa de 200 líneas y las funciones son cortas.
- La lógica sin Android va en `logica/`, separada de las pantallas.
