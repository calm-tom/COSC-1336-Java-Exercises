import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console blackjack that coaches the best odds on every move.
 *
 * Layout (Murach style): main first, outcome second, then helpers in descending
 * order of importance. House rules are deliberately last (an Easter egg for winning).
 *
 * Vocabulary: a SHOE is the pile of cards, a ROUND is one hand against the dealer,
 * a TABLE is the dealer plus the players, ODDS are the coach's win/push/lose numbers.
 * Cards are 1..13 (Ace, 2-10, J, Q, K); a card's VALUE is 1..10 (face cards count 10).
 */
public class Blackjack {

    public static void main(String[] args) {
        rules();
        System.out.println();
        NumberFormat whole = NumberFormat.getInstance();
        double money = 100;
        System.out.println("Starting money: " + whole.format(money));
        System.out.println();
        Scanner sc = new Scanner(System.in);
        String choice = "y";
        while (choice.equalsIgnoreCase("y")) {
            shoe();                                  // between rounds only: reshuffle if the cut was reached
            double bet = playerBet(money, sc);
            table();                                 // deal the round
            List<Integer> dealer = hands.get(0);
            List<Integer> player = hands.get(1);     // seat 1 is the human in v1
            printTable();
            boolean stood = false;
            boolean surrendered = false;

            if (dealerPeek(dealer)) {
                reveal(dealer);                      // dealer has Blackjack: the round is over before the player acts
            }
            while (!roundOver(stood, surrendered)) {
                boolean canSurrender = lateSurrender && player.size() == 2;
                coach(canSurrender);
                String move = canSurrender
                        ? getPlayerChoice(sc, "(H)it, (S)tand or su(R)render: ", "hsr")
                        : getPlayerChoice(sc, "(H)it or (S)tand: ", "hs");
                switch (move) {
                    case "h":
                        int card = draw(true);
                        player.add(card);
                        System.out.println("You draw " + cardName(card) + ". Your hand: "
                                + handText(player) + " (" + totalText(player) + ")");
                        if (handTotal(player) > 21) {
                            System.out.println("Bust!");
                        }
                        break;
                    case "s":
                        stood = true;
                        System.out.println("You stand on " + totalText(player) + ".");
                        break;
                    case "r":
                        surrendered = true;
                        System.out.println("You surrender.");
                        break;
                }
            }

            boolean dealerActs = !surrendered && handTotal(player) <= 21
                    && !isBlackjack(player) && !isBlackjack(dealer);
            if (dealerActs) {
                dealerPlay(dealer);
            }
            reveal(dealer);                          // every round ends with the hole card face up (see coach assumptions)
            System.out.println("Dealer: " + handText(dealer) + " (" + totalText(dealer) + ")");
            System.out.println("You:    " + handText(player) + " (" + totalText(player) + ")");

            String result = outcome(player, dealer, surrendered);
            switch (result) {
                case "BLACKJACK":
                    money += bet * payout();
                    System.out.println("You got a Blackjack!");
                    break;
                case "WIN":
                    money += bet;
                    System.out.println("You won.");
                    break;
                case "PUSH":
                    System.out.println("Push. No money won or lost.");
                    break;
                case "LOSS":
                    money -= bet;
                    System.out.println("You lost.");
                    break;
                case "SURRENDER":
                    money -= bet / 2;
                    System.out.println("You surrendered. Half your bet comes back.");
                    break;
            }
            System.out.println("Total money: " + formatCurrency(money));
            System.out.println();
            if (!houseRules(money)) {
                break;
            }
            choice = getPlayerChoice(sc, "Play again? (y/n): ", "yn");
            System.out.println();
        }
        System.out.println("Bye!");
    }

    /**
     * The outcome of a finished round, from the player's seat. Only final results
     * come out of here: BLACKJACK, WIN, PUSH, LOSS or SURRENDER.
     */
    private static String outcome(List<Integer> player, List<Integer> dealer, boolean surrendered) {
        if (surrendered) return "SURRENDER";
        boolean playerBlackjack = isBlackjack(player);
        boolean dealerBlackjack = isBlackjack(dealer);
        if (playerBlackjack && dealerBlackjack) return "PUSH";
        if (playerBlackjack) return "BLACKJACK";
        if (dealerBlackjack) return "LOSS";
        int p = handTotal(player);
        int d = handTotal(dealer);
        if (p > 21) return "LOSS";                   // the player busts first, so the dealer's hand never matters
        if (d > 21) return "WIN";
        if (p > d) return "WIN";
        if (p == d) return "PUSH";
        return "LOSS";
    }

    // =====================================================================
    // ODDS: the coach's math. Exact, recursive, and aware of every card seen
    // since the last shuffle.
    // =====================================================================

    // Memo tables: odds are a pure function of (unseen cards, hands), so results can be reused.
    private static final Map<String, double[][]> oddsMemo = new HashMap<>();
    private static final Map<String, double[]> dealerMemo = new HashMap<>();

    /**
     * Odds for the human's live hand, as {stand, hit}, each {win, push, lose}.
     * Entry point: reads the cards seen so far and handles the dealer's hole card.
     */
    private static double[][] odds(int total, boolean ace, int up) {
        int[] unseen = unseenCards();                // includes the hole card, which only the dealer knows
        boolean holeMatters = dealerPeeks && (up == 1 || up == 10);
        if (!holeMatters) {
            // The hole card and the player's next draw are interchangeable from the coach's view,
            // so the dealer can simply draw it later from the unseen cards (hole = 0 means "not fixed").
            return odds(unseen, total, ace, up, 0);
        }
        // The dealer peeked and the round continued, so the hole card cannot complete a Blackjack.
        // That shifts the odds of every other unseen card, so fix the hole card and weight each choice.
        double[][] sum = new double[2][3];
        double weight = 0;
        for (int hole = 1; hole <= 10; hole++) {
            if (unseen[hole] == 0 || makesBlackjack(up, hole)) continue;
            double w = unseen[hole];
            unseen[hole]--;
            double[][] o = odds(unseen, total, ace, up, hole);
            unseen[hole]++;
            for (int a = 0; a < 2; a++) {
                for (int i = 0; i < 3; i++) {
                    sum[a][i] += w * o[a][i];
                }
            }
            weight += w;
        }
        for (int a = 0; a < 2; a++) {
            for (int i = 0; i < 3; i++) {
                sum[a][i] /= weight;
            }
        }
        return sum;
    }

    /**
     * Recursive core. For this exact state, standing is worked out directly; hitting
     * averages over every possible draw, and after each draw the odds feed back in
     * by recursing with the better of stand/hit.
     */
    private static double[][] odds(int[] unseen, int total, boolean ace, int up, int hole) {
        String key = Arrays.toString(unseen) + "|" + total + "|" + ace + "|" + up + "|" + hole;
        double[][] known = oddsMemo.get(key);
        if (known != null) return known;

        double[] dealer = dealerOdds(unseen, up + hole, up == 1 || hole == 1);
        double[] stand = standOdds(best(total, ace), dealer);
        double[] hit = new double[3];
        int cards = countCards(unseen);
        if (cards == 0) {
            hit = stand;                             // nothing left to draw (cannot happen with the reserve)
        } else {
            for (int r = 1; r <= 10; r++) {
                if (unseen[r] == 0) continue;
                double w = unseen[r] / (double) cards;
                int t = total + r;
                boolean a = ace || r == 1;
                double[] p;
                if (best(t, a) > 21) {
                    p = new double[] {0, 0, 1};      // bust
                } else {
                    unseen[r]--;
                    double[][] next = odds(unseen, t, a, up, hole);
                    unseen[r]++;
                    p = evOf(next[0]) >= evOf(next[1]) ? next[0] : next[1];
                }
                for (int i = 0; i < 3; i++) {
                    hit[i] += w * p[i];
                }
            }
        }
        double[][] result = {stand, hit};
        oddsMemo.put(key, result);
        return result;
    }

    /** Dealer's final-total distribution {17, 18, 19, 20, 21, bust} from a given dealer hand. */
    private static double[] dealerOdds(int[] unseen, int total, boolean ace) {
        int best = best(total, ace);
        if (dealerStands(best, soft(total, ace))) {
            double[] done = new double[6];
            done[best > 21 ? 5 : best - 17] = 1.0;
            return done;
        }
        String key = Arrays.toString(unseen) + "|" + total + "|" + ace;
        double[] known = dealerMemo.get(key);
        if (known != null) return known;

        double[] out = new double[6];
        int cards = countCards(unseen);
        if (cards == 0) {
            out[5] = 1.0;                            // cannot happen with the reserve; avoids a crash
            return out;
        }
        for (int r = 1; r <= 10; r++) {
            if (unseen[r] == 0) continue;
            double w = unseen[r] / (double) cards;
            unseen[r]--;
            double[] sub = dealerOdds(unseen, total + r, ace || r == 1);
            unseen[r]++;
            for (int i = 0; i < 6; i++) {
                out[i] += w * sub[i];
            }
        }
        dealerMemo.put(key, out);
        return out;
    }

    /** {win, push, lose} for standing on total t against the dealer distribution. */
    private static double[] standOdds(int t, double[] dealer) {
        double win = dealer[5];
        for (int f = 17; f <= 21; f++) {
            if (f < t) win += dealer[f - 17];
        }
        double push = t >= 17 ? dealer[t - 17] : 0.0;
        return new double[] {win, push, 1 - win - push};
    }

    private static double evOf(double[] p) {
        return p[0] - p[2];                          // win pays 1, lose costs 1, push is 0
    }

    /**
     * Prints the odds of each available move and the recommended one.
     *
     * COACH ASSUMPTIONS (v1, hard-coded):
     *  1. The coach sees only face-up cards: the player's, the dealer's upcard and every card
     *     turned over since the last shuffle. The hole card is unknown until it is revealed.
     *  2. The coach treats the hole card as drawn at random from the unseen cards. If the
     *     dealer peeked (upcard Ace or 10) and the round went on, the hole is not a Blackjack.
     *  3. After the next card the player draws, the coach assumes best play (hit or stand).
     *  4. The coach assumes the dealer follows the rules() below.
     *  5. Every round ends with the hole card turned over, so the coach learns it. A real
     *     table does not always show it after a bust or surrender.
     *  6. Cards seen reset to zero whenever the shoe is reshuffled.
     *
     * FUTURE AMBITIONS (not in v1):
     *  - Noticing the hole card (machine vision) instead of treating it as unknown.
     *  - Calculating the next draw, i.e. knowing the shuffle order.
     *  - Odds for doubling down, splitting and insurance (see the rule flags).
     */
    private static void coach(boolean canSurrender) {
        List<Integer> player = hands.get(1);
        int up = value(hands.get(0).get(0));
        double[][] o = odds(rawTotal(player), hasAce(player), up);

        System.out.println("Coach (win / push / lose, expected value per $1 bet):");
        printOdds("Stand", o[0]);
        printOdds("Hit", o[1]);
        String bestMove = "STAND";
        double bestEv = evOf(o[0]);
        if (evOf(o[1]) > bestEv) {
            bestMove = "HIT";
            bestEv = evOf(o[1]);
        }
        if (canSurrender) {
            System.out.printf("  %-9s you lose half your bet            EV %+.3f%n", "Surrender", -0.5);
            if (-0.5 > bestEv) {
                bestMove = "SURRENDER";
            }
        }
        System.out.println("Coach recommends: " + bestMove + "  (cards seen this shoe: "
                + countCards(seen) + " of " + decks * 52 + ")");
    }

    private static void printOdds(String move, double[] p) {
        System.out.printf("  %-9s %5.1f%% / %5.1f%% / %5.1f%%   EV %+.3f%n",
                move, 100 * p[0], 100 * p[1], 100 * p[2], evOf(p));
    }

    // =====================================================================
    // DEALER: written per hand, so the same methods serve any number of tables.
    // =====================================================================

    /** The dealer's one rule of play. Total is the best total; soft means an Ace is counting 11. */
    private static boolean dealerStands(int total, boolean soft) {
        if (total > 17) return true;
        if (total < 17) return false;
        return !(soft && dealerHitsSoft17);          // only a soft 17 is up to the dealer-behavior rule
    }

    /** Dealer turns over the hole card and draws until the dealer must stand. */
    private static void dealerPlay(List<Integer> hand) {
        reveal(hand);
        System.out.println("Dealer turns over the hole card: " + handText(hand)
                + " (" + totalText(hand) + ")");
        while (!dealerStands(handTotal(hand), isSoft(hand))) {
            int card = draw(true);
            hand.add(card);
            System.out.println("Dealer draws " + cardName(card) + " (" + totalText(hand) + ")");
        }
    }

    /**
     * With an Ace or ten-value card showing, the dealer checks the hole card for Blackjack.
     * Returns true if the dealer has it. Insurance (not in v1) would be offered before this
     * check when the upcard is an Ace and settled by the hole card here at runtime.
     */
    private static boolean dealerPeek(List<Integer> hand) {
        int up = value(hand.get(0));
        if (!dealerPeeks || (up != 1 && up != 10)) return false;
        System.out.println("Dealer checks the hole card...");
        if (isBlackjack(hand)) {
            System.out.println("Dealer has Blackjack: " + handText(hand));
            return true;
        }
        System.out.println("No Blackjack.");
        return false;
    }

    private static boolean makesBlackjack(int up, int hole) {
        return (up == 1 && hole == 10) || (up == 10 && hole == 1);
    }

    // =====================================================================
    // ROUND: who is still to act, and the hand math everything else relies on.
    // =====================================================================

    /** The player's turn ends on a stand, a surrender, 21 or more, or a revealed dealer Blackjack. */
    private static boolean roundOver(boolean stood, boolean surrendered) {
        return stood || surrendered || !holeHidden || handTotal(hands.get(1)) >= 21;
    }

    private static int value(int card) {
        return Math.min(card, 10);
    }

    private static int rawTotal(List<Integer> hand) {
        int sum = 0;
        for (int card : hand) sum += value(card);
        return sum;                                  // every Ace counted as 1
    }

    private static boolean hasAce(List<Integer> hand) {
        for (int card : hand) {
            if (card == 1) return true;
        }
        return false;
    }

    private static int best(int total, boolean ace) {
        return ace && total + 10 <= 21 ? total + 10 : total;
    }

    private static boolean soft(int total, boolean ace) {
        return ace && total + 10 <= 21;
    }

    private static int handTotal(List<Integer> hand) {
        return best(rawTotal(hand), hasAce(hand));
    }

    private static boolean isSoft(List<Integer> hand) {
        return soft(rawTotal(hand), hasAce(hand));
    }

    private static boolean isBlackjack(List<Integer> hand) {
        return hand.size() == 2 && handTotal(hand) == 21;
    }

    // =====================================================================
    // TABLE: the dealer and the players, and the cards in front of them.
    // =====================================================================

    // Seat 0 is the dealer; seats 1..players are players. The dealer's second card is the hole card.
    private static final List<List<Integer>> hands = new ArrayList<>();
    // True while the dealer's hole card is face down. Insurance/peek logic reads the hole at runtime.
    private static boolean holeHidden = true;

    /** Sets up the table and deals a round: one card to each seat, the dealer's upcard, then again. */
    private static void table() {
        hands.clear();
        for (int seat = 0; seat <= players; seat++) {
            hands.add(new ArrayList<>());
        }
        holeHidden = true;
        for (int pass = 0; pass < 2; pass++) {
            for (int seat = 1; seat <= players; seat++) {
                hands.get(seat).add(draw(true));
            }
            hands.get(0).add(draw(pass == 0));       // upcard face up, hole card face down
        }
    }

    private static void printTable() {
        List<Integer> dealer = hands.get(0);
        System.out.println("Dealer shows: " + cardName(dealer.get(0)) + " [?]");
        for (int seat = 1; seat <= players; seat++) {
            List<Integer> hand = hands.get(seat);
            String label = seat == 1 ? "Your hand" : "Seat " + seat;
            System.out.println(label + ": " + handText(hand) + " (" + totalText(hand) + ")");
        }
    }

    /** Turns the dealer's hole card face up (once), which is when the coach gets to see it. */
    private static void reveal(List<Integer> dealer) {
        if (holeHidden) {
            holeHidden = false;
            seen[value(dealer.get(1))]++;
        }
    }

    // =====================================================================
    // SHOE: the pile of cards, the cut, and the reshuffle.
    // =====================================================================

    private static final List<Integer> shoe = new ArrayList<>();   // cards left to deal; the top is the end of the list
    private static final int[] seen = new int[11];                 // face-up cards seen since the last shuffle, by value (1..10)

    /** Between rounds: reshuffles when the cut is reached. Also builds the very first shoe. */
    private static void shoe() {
        if (cutReached()) {
            reshuffle(false);
            System.out.println("Shuffling " + decks + (decks == 1 ? " deck." : " decks."));
            System.out.println();
        }
    }

    /**
     * The cut is reached when either the penetration limit is dealt OR fewer cards than the
     * reserve remain, whichever comes first. With one deck dealt to 100%, the reserve decides.
     */
    private static boolean cutReached() {
        int total = decks * 52;
        int dealt = total - shoe.size();
        int reserve = reserveBase + reservePerSeat * players;
        return shoe.size() < reserve || dealt >= penetration * total;
    }

    /**
     * Rebuilds and shuffles the shoe, and clears what the coach has seen.
     * midRound is the draw guard: cards on the table stay out of the new shoe, and the
     * face-up ones count as seen (the hole card, if still hidden, does not).
     */
    private static void reshuffle(boolean midRound) {
        shoe.clear();
        for (int d = 0; d < decks; d++) {
            for (int card = 1; card <= 13; card++) {
                for (int suit = 0; suit < 4; suit++) {
                    shoe.add(card);
                }
            }
        }
        Arrays.fill(seen, 0);
        if (midRound) {
            for (int seat = 0; seat < hands.size(); seat++) {
                List<Integer> hand = hands.get(seat);
                for (int i = 0; i < hand.size(); i++) {
                    shoe.remove(Integer.valueOf(hand.get(i)));
                    boolean hiddenHole = seat == 0 && i == 1 && holeHidden;
                    if (!hiddenHole) seen[value(hand.get(i))]++;
                }
            }
        }
        Collections.shuffle(shoe);
        oddsMemo.clear();
        dealerMemo.clear();
    }

    /** Deals the top card. faceUp cards are recorded as seen; the hole card is not until revealed. */
    private static int draw(boolean faceUp) {
        if (shoe.isEmpty()) {
            // Draw guard: the cut should keep this from ever happening mid-round, but never crash.
            System.out.println("The shoe ran out mid-round, so the other cards are reshuffled.");
            reshuffle(true);
        }
        int card = shoe.remove(shoe.size() - 1);
        if (faceUp) {
            seen[value(card)]++;
        }
        return card;
    }

    /** Cards the coach has not seen, by value (index 1..10): a full shoe minus the cards seen. */
    private static int[] unseenCards() {
        int[] unseen = new int[11];
        for (int v = 1; v <= 9; v++) {
            unseen[v] = 4 * decks - seen[v];
        }
        unseen[10] = 16 * decks - seen[10];
        return unseen;
    }

    private static int countCards(int[] counts) {
        int sum = 0;
        for (int v = 1; v <= 10; v++) sum += counts[v];
        return sum;
    }

    private static String cardName(int card) {
        switch (card) {
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(card);
        }
    }

    private static String handText(List<Integer> hand) {
        StringBuilder text = new StringBuilder();
        for (int card : hand) {
            if (text.length() > 0) text.append(" ");
            text.append(cardName(card));
        }
        return text.toString();
    }

    private static String totalText(List<Integer> hand) {
        return (isSoft(hand) ? "soft " : "") + handTotal(hand);
    }

    // =====================================================================
    // RULES: hard-coded for v1 and annotated. In v2 these will be asked at launch.
    // They are non-final on purpose so a settings(sc) method can assign them later.
    // =====================================================================

    private static int decks = 1;                    // number of 52-card decks in the shoe
    private static int players = 1;                  // seats besides the dealer. v1: only seat 1 (the human) acts
    private static double penetration = 1.0;         // share of the shoe that may be dealt before a reshuffle
    private static int reserveBase = 10;             // cards that must stay in the shoe to start a round...
    private static int reservePerSeat = 5;           // ...plus this many per player seat (the cut also reshuffles here)
    private static boolean dealerHitsSoft17 = false; // dealer behavior: false = stands on every 17
    private static boolean dealerPeeks = true;       // dealer checks the hole card with an Ace or ten showing
    private static boolean lateSurrender = true;     // surrender allowed as the first move, after the peek
    private static int payoutTop = 3;                // Blackjack pays 3:2
    private static int payoutBottom = 2;

    // Hooks for later. Not used by v1; the coach and the move menu would need to learn each one.
    private static boolean doubleDownAllowed = false;
    private static boolean splitAllowed = false;
    private static boolean insuranceAllowed = false; // settled by the dealer's hole card at runtime

    private static double payout() {
        return (double) payoutTop / payoutBottom;
    }

    /** Prints the rule sheet at launch. */
    private static void rules() {
        System.out.println("BLACKJACK!");
        System.out.println("Blackjack pays " + payoutTop + ":" + payoutBottom);
        System.out.println("Dealer " + (dealerHitsSoft17 ? "hits" : "stands on") + " soft 17"
                + (dealerPeeks ? " and checks for Blackjack" : ""));
        System.out.println("Moves: hit, stand" + (lateSurrender ? ", surrender (first move only)" : ""));
        System.out.println("Shoe: " + decks + (decks == 1 ? " deck" : " decks")
                + ", reshuffled when fewer than " + (reserveBase + reservePerSeat * players)
                + " cards remain");
    }

    // =====================================================================
    // CONSOLE INPUT
    // =====================================================================

    private static String getPlayerChoice(Scanner sc, String prompt, String valid) {
        while (true) {
            System.out.print(prompt);
            String choice = sc.nextLine().trim().toLowerCase();
            if (choice.length() == 1 && valid.contains(choice)) {
                return choice;
            }
            StringBuilder options = new StringBuilder();
            for (int i = 0; i < valid.length(); i++) {
                if (i > 0) options.append(i == valid.length() - 1 ? " or " : ", ");
                options.append(Character.toUpperCase(valid.charAt(i)));
            }
            System.out.println("Please enter " + options + ".");
        }
    }

    private static String formatCurrency(double money) {
        return NumberFormat.getCurrencyInstance().format(money);
    }

    private static double playerBet(double money, Scanner sc) {
        final double MIN_BET = 5.0;
        final double MAX_BET = 1_000.0;
        while (true) {
            System.out.print("Bet amount: ");
            String line = sc.nextLine().trim();
            try {
                double bet = Double.parseDouble(line);
                if (bet < MIN_BET) {
                    System.out.println("Bet must be at least $" + (int) MIN_BET + ".");
                } else if (bet > MAX_BET) {
                    System.out.println("Bet cannot exceed $" + (int) MAX_BET + ".");
                } else if (bet > money) {
                    System.out.println("You don't have enough money for that bet.");
                } else {
                    return bet;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }

    // =====================================================================
    // HOUSE RULES: the Easter egg. Keep these last.
    // =====================================================================

    private static boolean houseRules(double money) {
        final double MIN_MONEY = 5.0;
        final double MAX_MONEY = 10_000.0;
        // NOTE: the textbook test was money <= MIN_MONEY, which ended the game at exactly $5.00
        // even though $5 is a legal bet. Removing the = (as here) starts game over at $4.99.
        // Put the = back when testing the minimum-bet boundary.
        if (money < MIN_MONEY) {
            System.out.println("You're out of cash, so get out of the Casino.");
            return false;
        }
        if (money >= MAX_MONEY) {
            System.out.println("The Casino's owner doesn't like you taking all our cash! Game Over!");
            return false;
        }
        return true;
    }
}
