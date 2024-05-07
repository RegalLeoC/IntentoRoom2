package com.example.intento2.coded

import com.example.intento2.R
import com.example.intento2.coded.Questions

enum class Topics(val questions: List<Questions>, val imageResourceId: Int) {
    MATHEMATICS(
        listOf(
            Questions("2 + 2 = ?", "4", listOf("3", "5", "6")),
            Questions("Square root of 9", "3", listOf("4", "2", "5")),
            Questions("Solve for X: X + 1 = 3", "2", listOf("1", "4", "5")),
            Questions("20 / 4", "5", listOf("4", "6", "8")),
            Questions("7 x 8", "56", listOf("42", "49", "64"))
        ), R.drawable.mathematics_image
    ),
    GREEK_MYTHOLOGY(
        listOf(
            Questions("Who's the god of thunder?", "Zeus", listOf("Poseidon", "Hades", "Apollo")),
            Questions("Who's the goddess of beauty?", "Aphrodite", listOf("Hera", "Athena", "Artemis")),
            Questions("What was Odysseus' Island?", "Ithaca", listOf("Crete", "Sicily", "Troy")),
            Questions("Greek Hero Weak in the heel?", "Achilles", listOf("Hercules", "Perseus", "Theseus")),
            Questions("What animal did the Achaeans use to trick the Trojans?", "Horse", listOf("Dog", "Cat", "Cow"))
        ), R.drawable.greek_mythology_image
    ),
    HISTORY(
        listOf(
            Questions("Mexican independence year", "1810", listOf("1821", "1910", "1800")),
            Questions("Grito de Dolores year", "1810", listOf("1821", "1910", "1800")),
            Questions("Year that WWII ended", "1945", listOf("1939", "1941", "1943")),
            Questions("Which country participated in the Cake Wars?", "Mexico", listOf("France", "USA", "Germany")),
            Questions(
                "Famous French figure which threatened Europe",
                "Napoleon",
                listOf("Louis XIV", "Joan of Arc", "Marie Antoinette")
            )
        ), R.drawable.history_image
    ),
    GEOGRAPHY(
        listOf(
            Questions("Which country is the Amazon forest in?", "Brazil", listOf("Peru", "Colombia", "Venezuela")),
            Questions("Which is the highest mountain?", "Mount Everest", listOf("K2", "Kangchenjunga", "Lhotse")),
            Questions("Which state doesn't exist in Mexico?", "Tlaxcala", listOf("Yucatan", "Quintana Roo", "Sonora")),
            Questions("Which country does the Nile River exist in?", "Egypt", listOf("Sudan", "Ethiopia", "Uganda")),
            Questions("Which state is the Popocatepetl in?", "Puebla", listOf("Mexico City", "Morelos", "Tlaxcala"))
        ), R.drawable.geography_image
    ),
    SPORTS(
        listOf(

            Questions("¿Quién ganó la Copa Mundial de la FIFA 2018?", "Francia", listOf("Argentina", "Brasil", "España")),
            Questions("¿Cuál es el club más exitoso en la historia de la Liga de Campeones de la UEFA?", "Real Madrid", listOf("Manchester United", "Bayern Munich", "FC Barcelona")),
            Questions("¿Quién ha ganado el mayor número de Balones de Oro?", "Lionel Messi", listOf("Cristiona Ronaldo", "Neymar", "Zinedine Zidane")),
            Questions("¿Cuál es el torneo de tenis más prestigioso en el mundo?", "Wimbledon", listOf("Abierto Francia", "Abierto Australia", "Abierto USA")),
            Questions("¿Cuál es el deporte nacional de Canadá?", "Hockey", listOf("Beisbol", "Futbol", "Baloncesto"))
        ), R.drawable.deporte
    ),
    SCIENCE(
        listOf(
            Questions("What is the chemical symbol for water?", "H2O", listOf("CO2", "O2", "NaCl")),
            Questions("What is the largest organ in the human body?", "Skin", listOf("Liver", "Heart", "Lung")),
            Questions("What is the closest planet to the Sun?", "Mercury", listOf("Venus", "Earth", "Mars")),
            Questions("What is the process by which plants make their own food called?", "Photosynthesis", listOf("Respiration", "Transpiration", "Fermentation")),
            Questions("What is the force that opposes motion?", "Friction", listOf("Gravity", "Acceleration", "Inertia"))
        ), R.drawable.science_image
    );


    companion object {
        fun updateTopics(vararg selections: Boolean): List<Topics> {
            val selectedTopics = mutableListOf<Topics>()
            values().forEachIndexed { index, topic ->
                if (index < selections.size && selections[index]) {
                    selectedTopics.add(topic)
                }
            }
            return selectedTopics
        }
    }


    /*companion object {
        fun updateTopics(vararg indices: Int): List<Topics> {
            val selectedTopics = mutableListOf<Topics>()
            for (index in indices) {
                if (index >= 0 && index < values().size) {
                    selectedTopics.add(values()[index])
                }
            }
            return selectedTopics
        }
    }*/

}


