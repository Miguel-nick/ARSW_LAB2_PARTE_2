
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW
### Solucion del laboratiro
- Miguel Sandoval
- Laura Castillo


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

- Al ejecutar el programa inicial se observa que el productor genera un elemento aproximadamente cada segundo, mientras que el consumidor intenta consumir elementos continuamente. Debido a esta diferencia de velocidades, en muchos momentos la cola se encuentra vacía.

- El problema principal se encuentra en la implementación de la clase Consumer. El método run() contiene un ciclo infinito que verifica constantemente si existen elementos en la cola.
  
- Cuando la cola tiene elementos, el consumidor los retira correctamente. Sin embargo, cuando la cola está vacía, el hilo no espera ni se bloquea, sino que continúa ejecutando el ciclo y consultando repetidamente el tamaño de la cola. Este comportamiento se conoce como espera activa y provoca un consumo innecesario de CPU. El hilo consumidor permanece utilizando el procesador incluso cuando no tiene elementos para procesar.
  
- Por lo tanto, la clase responsable principalmente del consumo elevado de CPU es Consumer, debido a la forma en que implementa el ciclo de consumo y la comprobación de la cola.

<img width="1480" height="776" alt="Captura de pantalla 2026-08-18 184221" src="https://github.com/user-attachments/assets/f5e3e73d-3d2c-4096-a29a-e2bbd0e94364" />


2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

- Con este cambio, cuando la cola está vacía el consumidor deja de ejecutar continuamente el ciclo y queda bloqueado esperando un nuevo elemento. De esta manera se elimina la espera activa y se utiliza la CPU de forma mucho más eficiente.
  
- El problema no estaba en utilizar un ciclo while(true), sino en que dentro del ciclo el consumidor consultaba continuamente si la cola tenía elementos. Esto generaba una espera activa y un consumo innecesario de CPU. Al utilizar BlockingQueue.take(), el consumidor queda bloqueado cuando la cola está vacía y continúa únicamente cuando existe un elemento disponible.

<img width="1207" height="570" alt="Captura de pantalla 2026-08-18 184313" src="https://github.com/user-attachments/assets/ebe6a569-fbde-44c6-8a76-bca07a070eea" />


3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

- Se utilizó ArrayBlockingQueue para establecer un límite máximo de elementos en la cola. Esta estructura recibe su capacidad al momento de ser creada, por lo que en este caso se utilizó un stock de 2 elementos. Para insertar los productos se utilizó el método put(), el cual bloquea al productor cuando la cola alcanza su capacidad máxima, evitando que el número de elementos supere el límite establecido. De esta manera, el productor espera hasta que el consumidor retire un elemento y exista nuevamente espacio disponible. Al realizar la prueba con un stock pequeño se verificó que la cola no supera la capacidad definida y que no se presentan errores ni un consumo elevado de CPU por espera activa.

<img width="532" height="359" alt="Captura de pantalla 2026-08-17 180749" src="https://github.com/user-attachments/assets/6ece8ef8-5984-478b-8bcc-942924b65a24" />

<img width="1478" height="776" alt="Captura de pantalla 2026-08-18 184059" src="https://github.com/user-attachments/assets/451191a3-cf7b-4e7b-b638-d8d837accb69" />


##### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

- Problema

La versión inicial del buscador de listas negras (HostBlackListsValidator + BlackListSearchThread) dividía el rango de servidores entre varios hilos, pero cada hilo siempre revisaba su rango completo, incluso después de que, en conjunto, ya se hubiera detectado el número de ocurrencias necesario (BLACK_LIST_ALARM_COUNT) para determinar que el host no es confiable. Esto generaba trabajo innecesario: no tenía sentido seguir consultando miles de servidores adicionales si la respuesta ya estaba decidida.

- Solución implementada

Se agregó una clase nueva, SharedOccurrencesCounter, que actúa como un contador compartido y thread-safe entre todos los hilos de búsqueda:

Cada vez que un hilo encuentra una ocurrencia, la reporta mediante reportOccurrence().
Cualquier hilo puede consultar alarmReached() para saber si, en conjunto (sumando lo detectado por todos los hilos), ya se alcanzó el umbral de alarma.
Ambos métodos son synchronized sobre el mismo monitor, garantizando que el incremento y la lectura del contador sean operaciones atómicas — no hay condición de carrera entre hilos que reportan ocurrencias y hilos que consultan si deben detenerse.

- Nota: Durante las pruebas se detectó un bug preexistente en HostBlacklistsDataSourceFacade.isInBlackListServer() (clase marcada como "no tocar"): el orden original de las operaciones (compute antes de putIfAbsent) provocaba un NullPointerException en la primera llamada de cada hilo nuevo, ya que compute intentaba incrementar un valor null. Fue necesario invertir el orden de esas dos líneas (putIfAbsent primero, compute después) para que el programa pudiera ejecutarse.

##### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

11. Para finalizar, implemente la opción STOP.


### Parte III – respuesta final

Para este ejercicio, el concepto clave es el invariante de la simulación: en un instante en el que no se está realizando una actualización de salud, la suma total de los puntos de vida de todos los inmortales debe permanecer constante. Si hay N jugadores y cada uno inicia con 100 puntos, entonces el total esperado es 100 x N. Este valor representa la consistencia global del sistema y permite verificar si la ejecución concurrente mantiene la lógica del juego.

La principal condición de carrera se presentaba al ejecutar la operación “pause and check”, porque se consultaba la lista compartida de salud mientras otros hilos seguían modificando esos valores. Para corregir esta inconsistencia, la simulación debe pausar a todos los inmortales antes de calcular la suma total y, luego, permitir la reanudación del proceso. De esta forma, la información visualizada corresponde a un estado estable del sistema.

También fue necesario proteger la región crítica de los combates entre inmortales. Cada pelea debe sincronizar los dos actores involucrados en un orden fijo, evitando así que dos hilos intenten adquirir los mismos locks en distinta secuencia y produzcan deadlocks. Este mecanismo garantiza que la actualización de salud sea segura y consistente. Además, al eliminar a los inmortales muertos se utilizó una colección concurrente, lo que evita volver la simulación secuencial y elimina la posibilidad de errores por modificación simultánea de la lista.

Con estas correcciones, la simulación conserva el invariante esperado, puede pausar y reanudar correctamente, y finalmente detenerse sin generar inconsistencias en la ejecución. La estrategia de sincronización aplicada permite controlar la concurrencia de forma segura, manteniendo la lógica del juego y evitando bloqueos o condiciones de carrera.

Autora: Laura Castillo
<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->
		

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
