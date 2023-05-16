import java.util.*

fun main() {
    // Builds the deck.
    // TODO - Is there a better way to do this?
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
    if (humans + bots < 3) throw Exception("Too few players")
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
    private var unusedWasabi = 0
    var points = 0
    abstract fun processTurn(hand: MutableList<Card>): Card

    // Nigiri point values are fixed and can be scored right away.
    protected fun score(selection: Card) {
        tableau.add(selection)
        points += when (selection) {
            Card.EGG_NIGIRI -> if (unusedWasabi > 0) {unusedWasabi--; 3} else 1
            Card.SALMON_NIGIRI -> if (unusedWasabi > 0) {unusedWasabi--; 6} else 3
            Card.SQUID_NIGIRI -> if (unusedWasabi > 0) {unusedWasabi--; 9} else 3
            Card.WASABI -> {unusedWasabi++; 0}
            else -> 0
        }
    }
}

class Human : Player() {
    override fun processTurn(hand: MutableList<Card>): Card {
        hand.forEachIndexed { index, card ->  println("$index: $card") }
        print("Card choice: ")
        return hand.removeAt(readln().toInt()).also { score(it) }
    }
}

class Bot : Player() {
    override fun processTurn(hand: MutableList<Card>): Card {
        // TODO - Make the bot better
        return hand.removeAt(0).also { score(it) }
    }
}

fun playRound(deck: MutableList<Card>, players: List<Player>) {
    val handSize = 12 - players.size
    val pool: List<MutableList<Card>> = List(players.size) {
        deck.slice(it * handSize until (it + 1) * handSize).toMutableList()
    }
    repeat(players.size * handSize) { deck.removeAt(0) }
    for (list in pool) assert(list.size == handSize)

    var handOffset = 0
    while (pool[0].size > 0) {
        // Play a subround
        val subroundSelections: MutableList<Card> = mutableListOf()
        for (i in players.indices) subroundSelections.add(players[i].processTurn(pool[(i + handOffset) % (pool.size)]))
        subroundSelections.forEachIndexed { index, card -> println("Player $index selected $card.")}
        handOffset++
    }

    // Score the round
    for (player in players) {
        player.points += 5 * (player.tableau.count { it == Card.TEMPURA } / 2)
        player.points += 10 * (player.tableau.count { it == Card.SASHIMI } / 3)
    }
}
