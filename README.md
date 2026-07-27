![resources/images/LogoUPS.png](resources/images/LogoUPS.png)
# <center>UNIVERSIDAD POLITECNICA SALECIANA</center>

### <center>Carrera: </center>

<center>Computacion </center>

### <center>Tema:</center>

<center> Proyecto Final: Implementacion y visualizacion de rutas en un mapa de calles mediante BFS y DFS</center>

### <center> Integrantes: </center>

<center> William Joel Berrezueta Mendieta</center>
<center> Oliver Alejandro Valdiviezo Arévalo </center>

### <center> Correos Institucionales: </center>

<center> agregar el del william </center>
<center> ovaldiviezoa@est.ups.edu.ec </center>

## Indice

#### 1. Portada................................................................................................1
#### 2. Objetivo..............................................................................................2
#### 3. Introduccion y Descripcion del problema..................................3
#### 4. Marco Teorico ...................................................................................4
#### 5. Arquitectura ......................................................................................5
#### 6. Diagrama UML .................................................................................6
#### 7. Ejemplos ............................................................................................7
#### 8. Tabla comparativa ...........................................................................8
#### 9. Concluciones ....................................................................................9
#### 10. Recomendaciones ........................................................................10




## Objetivos

- Desarrollar una aplicación en Java que permita modelar un mapa de calles como un grafo.

- Representar las intersecciones mediante nodos posicionados manualmente sobre una imagen de fondo y las calles mediante aristas.

- Implementar los algoritmos de búsqueda BFS y DFS para encontrar una ruta entre un nodo de inicio y un nodo de destino.

- Visualizar el comportamiento de ambos algoritmos en modo exploración y en modo ruta final.

- Aplicar estructuras de datos, persistencia de información, patrón MVC, control de versiones y documentación técnica.

- Comparar el comportamiento y los tiempos de ejecución de BFS y DFS sobre diferentes configuraciones del grafo.

## Introduccion y Descripcion del Problema

El problema principal de nuestro proyecto es lograr demostrar la utilidad de los grafos y como estos se aplican en mapas que nos ayudan a encotrar el camino mas corto desde un punto A a un punto B. Otro desafio va a ser crear una interfaz interactiva y facil de entender para que el usuario tenga la facilitad en la interactuacion, debe de ser simple, para hacerlo debemos lograr utilizar el raton para generar los grafos visualmente donde queremos y conectarlos de esa misma forma. Tambien los grafos que se van a ver visualmente van a tener colores los cuales muestran el estado en el que estan, rojo sin recorrido,azul recorrido, verde el camino directo hacia el princio y fin, demostrando con una pequeña animacion el recorrido que genera el algroritmo seleccionado.
## Marco Teorico
### Grafos: 
Son estructuras logicas que estan compuestas por una lista de nodos los cuales ayudan a conectar los grafos directamente, esa coneccion se llama vertices, estos grafos pueden tener 0 conexiones, 1 conexion (unidireccional) o 2 conexiones (bidireccional), de forma simple es como si una calle tenga 2 sentidos, 1 sentido o no tenga salida.

En codigo no es mas que un mapa generico con un nodo generico y un set de nodos genericos.
### Busqueda en anchura (BFS):
Este metodo de busqueda de grafos explora a estos mismos por niveles, primero visita a los vecinos que son directos del grafo inicial y luego va a sus vecinos, asi constantemente (es un metodo recursivo). Este metodo se implementa con colas porque se necesita sacar un los 2 listas, visitados y camino final, sirve para poder comprobar si existe un camino, quitar los nodos no directos y generar la lista del camino final, normalmente es un metodo más rapido pero este usa más memoria por necesitar memoria axuliar para funcionar a comparacion del DFS.

### Busqueda en Profundidad (DFS):
Este metodo de busqueda de grafos explora siguiendo una sola rama hasta el final hasta que no pueda ir mas (su orden a donde ir esta definido por el codigo) y si no llega a su destino tiene que regresar para poder recorrer todos los caminos y ver si encuentra el nodo final, como se puede observar es un metodo recursivo pero este en general es más largo por tener que ir al final de todos los nodos, pero utiliza menos memoria al necesitar menos memoria auxiliar.
## Arquitectura
#### Esta es la estructura y archivos que utilizamos en el proyecto
````text
src/
├── app/
│   └── App.java
├── controllers/
│   └── MapController.java
├── models/
│   ├── EdgeRecord.java
│   ├── MapPoint.java
│   └── VisualizationMode.java
├── persistence/
│   ├── FileGraphRepository.java
│   ├── GraphData.java
│   └── GraphRepository.java
├── structures/
│   ├── graphs/
│   │   ├── implementations/
│   │   ├── Graph.java
│   │   ├── PathFinder.java
│   │   └── PathResult.java
│   └── node/
│       └── Node.java
└── views/
    ├── MainFrame.java
    └── MapPanel.java

README.md
results.csv

````
#### Creamos 6 paquetes principales para poder organizar las tareas en el proyecto 
#### Paquetes:
- App: Solo contiene el archivo .java que hacer arrancar el proyecto.

- Controllers: Contienen toda las logica que hace funcionar el proyecto.

- Models: Son los modelos que nos ayudan a generar los grafos visualmente, donde esta ubicado en el mapa en coordenadas cartecianas y poder definir como se va a ver la animacion.

- Persistence: Guardar y cargar archivos de los grafos para facilitar las pruebas y tener datos guardados directamente en el disco duro.

- Strutures: Tiene 3 subcarpetas "graphs", "node" y "graphs/implements". Esta carpeta tiene estructura de datos genericas que definen el comportamiento de los grafos (que utilizan nodos como base) y tambien contienen los algoritmos de busqueda de los grafos.

- Views: Son todos los archivos que sirven para poder mostrar visualmente al usuario lo que queremos que vea y que pueda interactuar directamente en ese espacio.

- Results: Es un archivo que guarda los resultados de las pruebas que puedes generar al ejecutar el proyecto guardandolo en .csv para poder acumular de forma mas simple los registros.

## Diagrama UML

## Ejemplos 

imagen 1

descripcion 

imagen 2 

descripcion

## Tabla comparativa

## Concluciones

## Recomendaciones