# Mapa de clases y metodos

Este documento resume la responsabilidad de cada clase y sus metodos en el proyecto.

## Paquete `com.example.gamecenter`

### `SplashActivity`
- Proposito: pantalla de inicio que muestra un splash y navega al login.
- Metodos:
  - `onCreate(Bundle)`: configura la UI, aplica insets y navega a `LoginActivity` tras un delay.

### `LoginActivity`
- Proposito: autenticacion basica (usuario y contrasena) y alta del usuario si no existe.
- Metodos:
  - `onCreate(Bundle)`: inicializa vistas, repositorio y listener del boton de login.
  - `attemptLogin()`: valida campos, crea/obtiene usuario y navega a `MenuActivity`.

### `MainActivity`
- Proposito: pantalla base (no usada como entrada principal en el flujo actual).
- Metodos:
  - `onCreate(Bundle)`: configura la UI e insets.

### `MenuActivity`
- Proposito: menu principal, muestra bienvenida y puntos totales; navega a juegos y puntuaciones.
- Metodos:
  - `onCreate(Bundle)`: carga usuario, configura botones y repositorio.
  - `onResume()`: recalcula puntos totales del usuario.

### `GamesActivity`
- Proposito: listado de juegos disponibles.
- Metodos:
  - `onCreate(Bundle)`: configura cards y navega a `Game2048Activity`, `PuzzleBobbleActivity` y `ArcanoidActivity`.

### `ScoresActivity`
- Proposito: pantalla de puntuaciones con busqueda, filtros, orden y borrado por swipe.
- Metodos:
  - `onCreate(Bundle)`: inicializa spinners, recycler, adaptador y callbacks.
  - `onResume()`: recarga listado.
  - `onDestroy()`: libera el cursor.
  - `loadScores()`: consulta la base de datos segun filtros.
  - `mapOrder(int)`: mapea el indice del spinner a criterio de orden.

### `ScoresAdapter`
- Proposito: adaptador `RecyclerView` para renderizar la lista de puntuaciones desde un `Cursor`.
- Metodos:
  - `ScoresAdapter(Cursor, OnScoreClickListener)`: constructor.
  - `swapCursor(Cursor)`: reemplaza el cursor y refresca la lista.
  - `getScoreId(int)`: devuelve el id de una fila.
  - `onCreateViewHolder(...)`: infla `item_score`.
  - `onBindViewHolder(...)`: bindea datos a la tarjeta.
  - `getItemCount()`: cantidad de filas.

### `ScoreDetailActivity`
- Proposito: detalle de una puntuacion seleccionada.
- Metodos:
  - `onCreate(Bundle)`: valida id y carga datos.
  - `bindScore(long)`: consulta y formatea datos del score.
  - `updateUI(...)`: escribe datos en la UI.

### `Game2048Activity`
- Proposito: logica del juego 2048 con timer regresivo, deshacer y guardado de puntuacion.
- Metodos principales:
  - `onCreate(Bundle)`: configura grid, gestos, botones y arranca partida.
  - `setupGrid()`: crea celdas y layout del tablero.
  - `setupGestures()`: maneja swipes para mover fichas.
  - `resetGame()`: reinicia tablero, puntuacion y tiempo.
  - `saveUndoState()`: guarda el estado previo para deshacer.
  - `undoMove()`: restaura el estado previo si existe.
  - `move(Direction)`: aplica movimiento y merges.
  - `mergeLine(int[])`: combina fichas de una fila/columna.
  - `addRandomTile()`: agrega una ficha aleatoria.
  - `updateUi()`: actualiza la UI del tablero.
  - `isGameOver()`: verifica fin de juego por falta de movimientos.
  - `finishGame()`: guarda puntuacion y cierra partida.
  - `finishGameByTimeout()`: fin de partida por tiempo agotado.

### `PuzzleBobbleActivity`
- Proposito: contenedor del juego Puzzle Bobble, gestiona tiempo y guardado de puntuacion.
- Metodos:
  - `onCreate(Bundle)`: inicializa vistas y listener del juego.
  - `startNewGame()`: reinicia partida y timer.
  - `onScoreChanged(int)`: actualiza puntuacion.
  - `onGameOver(int)`: guarda score y detiene timer.
  - `onDestroy()`: limpia callbacks.

### `PuzzleBobbleView`
- Proposito: vista custom que implementa la jugabilidad de Puzzle Bobble.
- Metodos:
  - `setGameListener(GameListener)`: listener de score/fin.
  - `startNewGame()`: inicializa tablero y estado.
  - `onSizeChanged(...)`: recalcula tamanos y posicion del shooter.
  - `onDraw(Canvas)`: renderiza tablero, mira y burbuja.
  - `onTouchEvent(MotionEvent)`: apunta y dispara.
  - `launchBubble(...)`: calcula velocidad del disparo.
  - `updateBubble(float)`: fisica de movimiento y colision.
  - `placeBubble()`: fija burbuja al tablero.
  - `resolveMatches(...)`: elimina clusters >= 3 y puntua.
  - `removeFloatingClusters()`: elimina burbujas sin anclaje.

### `ArcanoidActivity`
- Proposito: contenedor del juego Arcanoid, gestiona tiempo y guardado de puntuacion.
- Metodos:
  - `onCreate(Bundle)`: inicializa vistas y listener del juego.
  - `startNewGame()`: reinicia partida y timer.
  - `onScoreChanged(int)`: actualiza puntuacion.
  - `onLivesChanged(int)`: actualiza vidas.
  - `onGameOver(int)`: guarda score y detiene timer.
  - `onDestroy()`: limpia callbacks.

### `ArcanoidView`
- Proposito: vista custom que implementa la jugabilidad de Arcanoid.
- Metodos:
  - `setGameListener(GameListener)`: listener de score/vidas/fin.
  - `getLives()`: devuelve vidas actuales.
  - `startNewGame()`: inicializa ladrillos, bola y loop.
  - `onSizeChanged(...)`: calcula dimensiones de paddle/bola/ladrillos.
  - `onDraw(Canvas)`: dibuja fondo, ladrillos, paddle y bola.
  - `onTouchEvent(MotionEvent)`: mueve el paddle.
  - `updateBall(float)`: fisica y colisiones.
  - `handleBrickCollisions()`: rompe ladrillos y puntua.
  - `areBricksCleared()`: valida fin por tablero limpio.

### `SquareGridLayout`
- Proposito: `GridLayout` cuadrado para el tablero 2048.
- Metodos:
  - `onMeasure(int, int)`: fuerza altura = ancho.

### `TimeFormatter`
- Proposito: utilitario para formatear tiempo en mm:ss.
- Metodos:
  - `formatMillis(long)`: convierte milisegundos a mm:ss.

### `ChallengesActivity`
- Proposito: placeholder de retos.
- Metodos:
  - `onCreate(Bundle)`: configura UI.

### `Game2Activity`
- Proposito: minijuego de taps con duracion fija (no enlazado en el menu actual).
- Metodos:
  - `onCreate(Bundle)`: prepara UI y listeners.
  - `startGame()`: inicia partida.
  - `finishGame()`: guarda puntuacion y finaliza.
  - `onDestroy()`: limpia callbacks.

## Paquete `com.example.gamecenter.data`

### `DbContract`
- Proposito: contrato de tablas y columnas de SQLite.
- Clases internas:
  - `UserEntry`: columnas de usuarios.
  - `GameEntry`: columnas de juegos.
  - `ScoreEntry`: columnas de puntuaciones.

### `GameCenterDbHelper`
- Proposito: gestiona creacion/actualizacion de la base de datos.
- Metodos:
  - `onCreate(SQLiteDatabase)`: crea tablas e inserta juegos base.
  - `onUpgrade(SQLiteDatabase, int, int)`: recrea tablas al subir version.

### `GameCenterRepository`
- Proposito: capa de acceso a datos para usuarios, juegos y puntuaciones.
- Metodos:
  - `getOrCreateUserId(String)`: devuelve id o crea usuario.
  - `getGameIdByName(String)`: busca id de juego.
  - `insertScore(long, long, int, long)`: inserta puntuacion.
  - `deleteScore(long)`: elimina puntuacion.
  - `getTotalPointsForUser(long)`: suma puntos del usuario.
  - `queryScores(String, String, String)`: consulta scores con filtros y orden.
  - `getScoreById(long)`: obtiene detalle de un score.

