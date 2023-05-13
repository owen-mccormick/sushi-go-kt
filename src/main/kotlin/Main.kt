import java.util.*

fun main() {
    // Builds the deck.
    // Is there a better way to do this?
    val deck: MutableList<Card> = MutableList(108) {
        when (it) {
            in 0..13 -> Card.TEMPURA
            in 14..27 -> Card.SASHIMI
            in 28..41 -> Card.DUMPLING
            in 42..53 -> Card.MAKI_2
            in 54..61 -> Card.MAKI_3
            in 62..67 -> Card.MAKI_1
            in 68..77 -> Card.SALMON_NIGIRI
            in 78..82 -> Card.SQUID_NIGIRI
            in 83..87 -> Card.EGG_NIGIRI
            in 88..97 -> Card.PUDDING
            in 98..103 -> Card.WASABI
            else -> Card.CHOPSTICKS
        }
    }

    // Prints deck contents to check values
    val frequencyMap: MutableMap<Card, Int> = EnumMap(Card::class.java)
    for (s in deck) {
        var count = frequencyMap[s]
        if (count == null) count = 0
        frequencyMap[s] = count + 1
    }
    println(frequencyMap)

    print("Number of humans: ")
    val humans = readln().toInt()
    print("Number of bots: ")
    val bots = readln().toInt()
    println("Humans: $humans")
    println("Bots: $bots")
}
enum class Card {
    TEMPURA,
    SASHIMI,
    DUMPLING,
    MAKI_1,
    MAKI_2,
    MAKI_3,
    SALMON_NIGIRI,
    SQUID_NIGIRI,
    EGG_NIGIRI,
    PUDDING,
    WASABI,
    CHOPSTICKS
}

abstract class Player {

    var tableau = mutableListOf<Card>()
    protected var unusedWasabi = 0
    var points = 0
    abstract fun processTurn(hand: MutableList<Card>)
}

class Human : Player() {
    override fun processTurn(hand: MutableList<Card>) {
        TODO("Not yet implemented")
    }

}

class Bot : Player() {
    override fun processTurn(hand: MutableList<Card>) {
        TODO("Not yet implemented")
    }
}
