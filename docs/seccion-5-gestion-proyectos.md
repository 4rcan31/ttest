# Sección 5: Gestión de Proyectos

## 1. ¿En qué momento inicia un Sprint en Scrum?

Un Sprint **inicia con la *Sprint Planning*** y **comienza inmediatamente después de que termina el
Sprint anterior**, sin pausas entre uno y otro (Guía de Scrum 2020: *"A new Sprint starts
immediately after the conclusion of the previous Sprint"* y *"Sprint Planning initiates the
Sprint"*).

En la *Sprint Planning*, el equipo Scrum define:

1. **Por qué** es valioso el Sprint: el **Objetivo del Sprint** (*Sprint Goal*).
2. **Qué** se puede terminar: los elementos del *Product Backlog* seleccionados.
3. **Cómo** se hará el trabajo: el plan, que junto con lo anterior forma el *Sprint Backlog*.

El Sprint tiene una duración fija de un mes o menos (normalmente 1 a 4 semanas) y contiene todos
los eventos: *Sprint Planning*, *Daily Scrum*, *Sprint Review* y *Sprint Retrospective*. Termina
con la Retrospectiva e inmediatamente empieza el siguiente.

## 2. Mencione 3 roles involucrados en la metodología Scrum

La Guía de Scrum 2020 los llama **responsabilidades** (*accountabilities*) dentro del Equipo Scrum:

| Rol | Responsabilidad principal |
|---|---|
| **Product Owner** | Maximizar el valor del producto. Gestiona y prioriza el *Product Backlog*, define el Objetivo del Producto y es la voz del negocio y de los usuarios. Es una sola persona, no un comité. |
| **Scrum Master** | Asegurar que Scrum se entienda y se aplique. Es un líder al servicio del equipo: facilita los eventos, elimina impedimentos, entrena en autogestión y ayuda a la organización a adoptar Scrum. |
| **Developers** (equipo de desarrollo) | Crear un incremento utilizable y que cumpla la *Definition of Done* en cada Sprint. Planifican el *Sprint Backlog*, se autogestionan y son multifuncionales (desarrollo, QA, diseño, etc.). |

Los *stakeholders* (clientes, usuarios, patrocinadores) participan, por ejemplo en la *Sprint
Review*, pero no forman parte del Equipo Scrum.

## 3. Según el PMBOK, ¿qué significa SPI y qué representa su valor igual a 1?

**SPI (*Schedule Performance Index*, Índice de Desempeño del Cronograma)** es un indicador de la
**Gestión del Valor Ganado (EVM)** que mide la eficiencia con la que el proyecto avanza respecto al
cronograma:

```
SPI = EV / PV
```

- **EV (*Earned Value*, valor ganado):** valor presupuestado del trabajo realmente completado a la fecha.
- **PV (*Planned Value*, valor planificado):** valor presupuestado del trabajo que se planeó
  completar a esa fecha.

| Valor | Interpretación |
|---|---|
| **SPI = 1** | **El proyecto va exactamente según el cronograma:** el trabajo completado es igual al planificado a la fecha. |
| SPI > 1 | Adelantado: se completó más trabajo del planificado. |
| SPI < 1 | Atrasado: se completó menos trabajo del planificado. |

**Ejemplo:** a la semana 8 se planificó completar trabajo por USD 40,000 (PV) y se completó
exactamente ese trabajo (EV = 40,000). Entonces SPI = 40,000 / 40,000 = **1**: el proyecto está
al día. Si EV fuera 30,000, el SPI sería 0.75: el proyecto avanza al 75 % de la velocidad
planificada.

Indicadores relacionados: la variación del cronograma **SV = EV − PV** (SV = 0 equivale a SPI = 1)
y el **CPI = EV / AC**, que mide la eficiencia en costos. Una limitación conocida del SPI es que
tiende a 1 al final del proyecto aunque haya habido atrasos, porque EV termina igualando a PV. Por
eso a veces se complementa con la técnica de *Earned Schedule*.
