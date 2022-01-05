package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A <i>GameMatch</i> groups together player threads of the same
 * ability into fixed-sized groups to play matches with each other.
 * Implement the class <i>GameMatch</i> using <i>Lock</i> and
 * <i>Condition</i> to synchronize player threads into groups.
 */
public class GameMatch {

    /* Three levels of player ability. */
    public static final int abilityBeginner = 1,
            abilityIntermediate = 2,
            abilityExpert = 3;

    /**
     * Allocate a new GameMatch specifying the number of player
     * threads of the same ability required to form a match.  Your
     * implementation may assume this number is always greater than zero.
     */
    public GameMatch (int numPlayersInMatch) {
        N = numPlayersInMatch;
        levelQueues = new ArrayList<LinkedList<WaitingPlayer>>(3);
        conds = new Condition[3];
        locks = new Lock[3];
        matchID = 0;
        matchIDLock = new Lock();
        for (int i = 0; i < 3; i++) {
            locks[i] = new Lock();
            levelQueues.add(new LinkedList<WaitingPlayer>());
            conds[i] = new Condition(locks[i]);
        }
    }

    /**
     * Wait for the required number of player threads of the same
     * ability to form a game match, and only return when a game match
     * is formed.  Many matches may be formed over time, but any one
     * player thread can be assigned to only one match.
     *
     * Returns the match number of the formed match.  The first match
     * returned has match number 1, and every subsequent match
     * increments the match number by one, independent of ability.  No
     * two matches should have the same match number, match numbers
     * should be strictly monotonically increasing, and there should
     * be no gaps between match numbers.
     *
     * @param ability should be one of abilityBeginner, abilityIntermediate,
     * or abilityExpert; return -1 otherwise.
     */
    public int play (int ability) {
        if (ability < 1 || ability > 3) {
            return -1;
        }
        int idx = ability - 1;
        locks[idx].acquire();
        WaitingPlayer wp = new WaitingPlayer();
        levelQueues.get(idx).add(wp);
        if (levelQueues.get(idx).size() < N) {
            conds[idx].sleep();
        }
        else {
            matchIDLock.acquire();
            matchID ++;
            while (!levelQueues.get(idx).isEmpty())
                levelQueues.get(idx).removeFirst().matchID = matchID;
            matchIDLock.release();
            conds[idx].wakeAll();
        }
        locks[idx].release();
        return wp.matchID;
    }

    // Place GameMatch test code inside of the GameMatch class.

    public static void matchTest01 () {
        final GameMatch match = new GameMatch(3);

        // Illegal ability input
        KThread invalid1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(-1);
                Lib.assertTrue(r == -1, "expected match number of -1");
                r = match.play(0);
                Lib.assertTrue(r == -1, "expected match number of -1");
                r = match.play(4);
                Lib.assertTrue(r == -1, "expected match number of -1");
                r = match.play(100);
                Lib.assertTrue(r == -1, "expected match number of -1");
            }
        });

        // Instantiate the threads
        KThread beg1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg1 matched, number:" + r);
            }
        });

        KThread beg2 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg2 matched, number:" + r);
            }
        });

        KThread beg3 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg3 matched, number:" + r);
            }
        });

        KThread beg4 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg4 matched, number:" + r);
            }
        });
        KThread beg5 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg5 matched, number:" + r);
            }
        });

        KThread beg6 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg6 matched, number:" + r);
            }
        });

        KThread beg7 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg7 matched, number:" + r);
            }
        });
        KThread int1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                System.out.println ("int1 matched, number:" + r);
            }
        });

        KThread int2 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                System.out.println ("int2 matched, number:" + r);
            }
        });
        KThread int3 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                System.out.println ("int3 matched, number:" + r);
            }
        });
        KThread int4 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                Lib.assertNotReached("int4 should not have matched!");
            }
        });
        KThread exp1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityExpert);
                Lib.assertNotReached("exp1 should not have matched!");
            }
        });
        KThread exp2 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityExpert);
                Lib.assertNotReached("exp2 should not have matched!");
            }
        });

        // Run the threads.  The beginner threads should successfully
        // form a match, the other threads should not.  The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
        beg2.fork();
        beg3.fork();
        beg4.fork();
        beg5.fork();
        beg6.fork();
        beg7.fork();

        int1.fork();
        int2.fork();
        int3.fork();
        int4.fork();

        exp1.fork();
        exp2.fork();

        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
    }

    public static void matchTest4 () {
        final GameMatch match = new GameMatch(2);

        // Instantiate the threads
        KThread beg1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg1 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg1.setName("B1");

        KThread beg2 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println ("beg2 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg2.setName("B2");

        KThread int1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                Lib.assertNotReached("int1 should not have matched!");
            }
        });
        int1.setName("I1");

        KThread exp1 = new KThread( new Runnable () {
            public void run() {
                int r = match.play(GameMatch.abilityExpert);
                Lib.assertNotReached("exp1 should not have matched!");
            }
        });
        exp1.setName("E1");

        // Run the threads.  The beginner threads should successfully
        // form a match, the other threads should not.  The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
        int1.fork();
        exp1.fork();
        beg2.fork();

        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
    }

    public static void selfTest() {
        matchTest01();
    }

    public class WaitingPlayer {
        public int matchID;
    }
    private int N;
    private ArrayList<LinkedList<WaitingPlayer>> levelQueues;
    private Lock[] locks;
    private Condition[] conds;
    private int matchID;
    private Lock matchIDLock;
}

