package com.example.myapplication.data

import com.example.myapplication.domain.modelo.Cancion

object RepositorioCanciones {

    private val canciones = mutableListOf<Cancion>()

    init {

        canciones.add(Cancion("j","j"))
        canciones.add(Cancion("a","a"))
        canciones.add(Cancion("b","b"))
        canciones.add(Cancion("c","c"))
        canciones.add(Cancion("d","d"))
        canciones.add(Cancion("e","e"))
        canciones.add(Cancion("f","f"))
        canciones.add(Cancion("g","g"))
        canciones.add(Cancion("h","h"))
        canciones.add(Cancion("i","i"))
        canciones.add(Cancion("k","k"))
        canciones.add(Cancion("l","l"))
        canciones.add(Cancion("m","m"))
        canciones.add(Cancion("n","n"))
        canciones.add(Cancion("ave maria","david bisbal"))
        canciones.add(Cancion("halo","beyonce"))
        canciones.add(Cancion("rolling in the deep","adele"))
        canciones.add(Cancion("like a prayer","madonna"))
        canciones.add(Cancion("thriller","michael jackson"))
        canciones.add(Cancion("y","y"))


    }

    fun size() = canciones.size

    val numCanciones: Int
        get() = canciones.size
    fun getCancion(id:Int) = canciones[id]
    fun addCancion(cancion: Cancion) = canciones.add(cancion)
    fun getCanciones(): List<Cancion> = canciones.toList()
    fun mezclar() {
        canciones.shuffle()
    }

    fun borrar(cancion: Cancion) {
        canciones.removeIf({ cancion.titulo == it.titulo })

    }

//    fun getCancion(id:Int) : Cancion
//    {
//        return canciones[id]
//    }
}