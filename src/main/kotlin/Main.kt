fun main() {
    // Builds the deck.
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
    deck.shuffle()

    print("Number of humans: ")
    val humans = readln().toInt()
    print("Number of bots: ")
    val bots = readln().toInt()
    if (humans + bots < 3) throw Exception("Too few players")
    repeat(40) { println("") } // Clear screen

    val players = MutableList(humans + bots) {
        when (it) {
            in 0 until humans -> Human(it)
            else -> Bot(it)
        }
    }

    repeat(3) { playRound(deck, players) }

    // Pudding points
    players.groupBy { it.pudding }.maxBy { it.key }.value.let {
        for (player in it) {
            player.points += 6 / it.size
            println("Player #${player.id} was awarded ${6 / it.size} points for collecting the most pudding.")
        }
    }
    players.groupBy { it.pudding }.minBy { it.key }.value.let {
        for (player in it) {
            player.points -= 6 / it.size
            println("Player #${player.id} lost ${6 / it.size} points for collecting the least pudding.")
        }
    }

    for (player in players) println("Final score of player #${player.id} was ${player.points}.")
    println("The winner was player #${players.maxBy { it.points }.id}!")
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

abstract class Player(val id: Int) {
    var tableau = mutableListOf<Card>()
    var unusedWasabi = 0
    var points = 0
    var pudding = 0
    abstract fun processTurn(hand: MutableList<Card>): Card
    abstract fun useChopsticks(): Boolean

    // Nigiri point values scored right away because order matters with wasabi
    protected fun score(selection: Card) {
        tableau.add(selection)
        points += when (selection) {
            Card.EGG_NIGIRI -> if (unusedWasabi > 0) { unusedWasabi--; 3 } else 1
            Card.SALMON_NIGIRI -> if (unusedWasabi > 0) { unusedWasabi--; 6 } else 2
            Card.SQUID_NIGIRI -> if (unusedWasabi > 0) { unusedWasabi--; 9 } else 3
            Card.WASABI -> {unusedWasabi++; 0}
            Card.PUDDING -> {pudding++; 0}
            else -> 0
        }
    }
    fun maki() = tableau.count { it == Card.MAKI_1 } + 2 * tableau.count { it == Card.MAKI_2 } + 3 * tableau.count { it == Card.MAKI_3 }
}

class Human(id: Int) : Player(id) {
    override fun processTurn(hand: MutableList<Card>): Card {
        hand.forEachIndexed { index, card ->  println("$index: $card") }
        print("Card choice: ")
        return hand.removeAt(readln().toInt()).also { score(it) }
    }

    override fun useChopsticks(): Boolean {
        print("Would you like to use your chopsticks? ")
        return readln().lowercase().let { it == "y" || it == "yes" }
    }
}

class Bot(id: Int) : Player(id) {
    // TODO - Make the bot better
    override fun processTurn(hand: MutableList<Card>): Card {
        return hand.removeAt(0).also { score(it) }
    }

    override fun useChopsticks() = false
}

fun playRound(deck: MutableList<Card>, players: List<Player>) {
    val handSize = 12 - players.size
    val pool: List<MutableList<Card>> = List(players.size) {
        deck.slice(it * handSize until (it + 1) * handSize).toMutableList()
    }
    repeat(players.size * handSize) { deck.removeAt(0) }

    var handOffset = 0
    while (pool[0].size > 0) {
        // Play a subround
        val subroundSelections: MutableMap<Int, Card> = mutableMapOf()
        for (player in players) subroundSelections[player.id] = player.processTurn(pool[(player.id + handOffset) % pool.size])
        repeat(40) { println("") }
        for (player in players) {
            if (player.tableau.contains(Card.CHOPSTICKS) && player.useChopsticks()) {
                println("Sushi Go! Player #${player.id} used chopsticks to select " +
                        "${player.processTurn(pool[(player.id + handOffset) % pool.size])}.")
                player.tableau.remove(Card.CHOPSTICKS)
                pool[(player.id + handOffset) % pool.size].add(Card.CHOPSTICKS)
            }
            println("Player #${player.id} selected ${subroundSelections[player.id]}. Their current tableau is ${player.tableau}.")
        }
        handOffset++
    }

    // Tempura, dumplings, sashimi
    for (player in players) {
        player.points += 5 * (player.tableau.count { it == Card.TEMPURA } / 2)
        player.points += 10 * (player.tableau.count { it == Card.SASHIMI } / 3)
        player.points += when(player.tableau.count { it == Card.DUMPLING }) {
            0 -> 0
            1 -> 1
            2 -> 3
            3 -> 6
            4 -> 10
            else -> 15
        }
    }

    // Maki points
    players.groupBy { it.maki() }.maxBy { it.key }.also {
        for (player in it.value) {
            player.points += 6 / it.value.size
            println("Player #${player.id} was awarded ${6 / it.value.size} points for collecting the most maki.")
        }
    }.let { win ->
        if (win.value.size == 1) players.groupBy { it.maki() }.toSortedMap().headMap(win.key).maxBy { it.key }.value.let {
            for (player in it) {
                player.points += 3 / it.size
                println("Player #${player.id} was awarded ${3 / it.size} points for collecting the second-most maki.")
            }
        }
    }

    for (player in players) println("The current score of player #${player.id} is ${player.points}.")
    println("Player #${players.maxBy { it.points }.id} is in the lead.")
    players.forEach {
        it.tableau.clear()
        it.unusedWasabi = 0
    }
}
