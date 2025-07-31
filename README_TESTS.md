# Pruebas Unitarias - EISC UNO Game

Este proyecto incluye pruebas unitarias completas para las clases principales del juego EISC UNO utilizando JUnit 5.

## Clases de Prueba Implementadas

### 1. CardTest
Pruebas para la clase `Card` que incluyen:
- Creación de cartas con diferentes tipos y colores
- Validación de tipos de cartas (salvaje, +2, +4, skip, reverse)
- Verificación de cartas especiales
- Lógica de compatibilidad entre cartas (`canBePlayedOn`)
- Cambio de colores en cartas salvajes
- Manejo de excepciones

### 2. DeckTest
Pruebas para la clase `Deck` que incluyen:
- Creación e inicialización del mazo
- Tomar cartas individuales y múltiples
- Manejo de mazo vacío
- Reposición del mazo excluyendo cartas específicas
- Verificación de propiedades de las cartas tomadas
- Verificación del barajado del mazo

### 3. GameHandlerTest
Pruebas para la clase `GameHandler` que incluyen:
- Creación y configuración del manejador del juego
- Inicio del juego y reparto de cartas
- Lógica de comer cartas
- Verificación de cartas jugables
- Manejo de clics de cartas del jugador humano
- Verificación de ganadores
- Gestión de turnos y estados del juego

## Cómo Ejecutar las Pruebas

### Usando Maven
```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas específicas
mvn test -Dtest=CardTest
mvn test -Dtest=DeckTest
mvn test -Dtest=GameHandlerTest

# Ejecutar con reporte detallado
mvn test -Dtest=CardTest -Dsurefire.useFile=false
```

### Usando IDE
1. Abre el proyecto en tu IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navega a la carpeta `src/test/java`
3. Ejecuta las clases de prueba individualmente o todas juntas

### Usando línea de comandos
```bash
# Compilar y ejecutar pruebas
mvn clean compile test

# Ver reportes de cobertura (si tienes plugins de cobertura)
mvn test jacoco:report
```

## Estructura de las Pruebas

Cada clase de prueba sigue las mejores prácticas:

- **@BeforeEach**: Configuración inicial para cada prueba
- **@Test**: Métodos de prueba individuales
- **@DisplayName**: Descripciones claras en español
- **Assertions**: Verificaciones específicas usando JUnit 5

## Cobertura de Pruebas

Las pruebas cubren:

### Card
- ✅ Creación de cartas
- ✅ Identificación de tipos de cartas
- ✅ Lógica de compatibilidad
- ✅ Manejo de colores
- ✅ Manejo de excepciones

### Deck
- ✅ Inicialización del mazo
- ✅ Operaciones de tomar cartas
- ✅ Reposición del mazo
- ✅ Manejo de errores
- ✅ Verificación de propiedades

### GameHandler
- ✅ Configuración del juego
- ✅ Lógica de juego
- ✅ Manejo de jugadores
- ✅ Verificación de ganadores
- ✅ Gestión de turnos

## Notas Importantes

1. **Dependencias**: Las pruebas utilizan las clases reales del proyecto, no mocks
2. **JavaFX**: Las pruebas incluyen inicialización automática de JavaFX para evitar errores de "Internal graphics not initialized yet"
3. **Recursos**: Las pruebas utilizan rutas de recursos reales del proyecto
4. **Aleatoriedad**: Algunas pruebas (como el barajado) pueden fallar ocasionalmente por naturaleza aleatoria

## Solución de Problemas Comunes

### Error: "Internal graphics not initialized yet"
Si ves este error al ejecutar las pruebas, significa que JavaFX no se ha inicializado correctamente. Las pruebas ya incluyen la inicialización automática a través de la clase `BaseTest`, pero si persiste el problema:

1. **Asegúrate de que todas las clases de prueba extiendan de `BaseTest`**
2. **Verifica que la dependencia `javafx-swing` esté en el `pom.xml`**
3. **Ejecuta `mvn clean compile` antes de ejecutar las pruebas**

## Agregando Nuevas Pruebas

Para agregar nuevas pruebas:

1. Crea una nueva clase en `src/test/java/org/example/eiscuno/model/`
2. Usa la anotación `@Test` para métodos de prueba
3. Usa `@DisplayName` para descripciones claras
4. Sigue el patrón AAA (Arrange, Act, Assert)

## Ejemplo de Nueva Prueba

```java
@Test
@DisplayName("Debería hacer algo específico")
void testSpecificBehavior() {
    // Arrange
    Card card = new Card("/path/to/image.png", "5", "RED");
    
    // Act
    boolean result = card.isSpecial();
    
    // Assert
    assertFalse(result);
}
```

## Troubleshooting

### Problemas Comunes

1. **Error de JavaFX**: Si hay errores relacionados con JavaFX, asegúrate de que el módulo esté configurado correctamente
2. **Rutas de recursos**: Verifica que las rutas de las imágenes sean correctas
3. **Dependencias**: Asegúrate de que JUnit 5 esté en el classpath

### Logs de Pruebas

Las pruebas incluyen logs informativos para ayudar en el debugging:
- Información sobre el estado del mazo
- Detalles sobre cartas jugadas
- Estado del juego durante las pruebas 