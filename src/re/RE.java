package re;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import fa.*;
import fa.nfa.*;

/**
 * This class constructs an NFA for a given regular expression
 *
 * @author Zixiao Chen (Section 1), Daniel McDougall (Section 2)
 */
public class RE implements REInterface {
    String regEx;
    int stateInc = 0;

    /**
     * Constructor
     *
     * @param regEx this is the regular expression to be converted
     */
    public RE(String regEx) {
        this.regEx = regEx;
    }

    /**
     * @return The NFA built based on the regular expression
     */
    public NFA getNFA() {
        return regEx();
    }

    /**
     *  This method will convert a regular expression into an NFA
     *
     * @return NFA
     */
    private NFA regEx() {
        NFA term = term();

        //If the regex requires a union operation
        if (more() && peek() == '|') {

            eat('|');
            NFA regex = regEx();
            return union(term, regex);
            //If no union is needed, just return the NFA
        } else {
            return term;
        }
    }

    /**
     * combine 2 NFAs into 1 NFA (the order does not matter)
     *
     * @return the combined NFA
     */
    private NFA union(NFA nfa1, NFA nfa2) {
        NFA result = new NFA();

        String startState = String.valueOf(stateInc++);
        result.addStartState(startState);
        result.addNFAStates(nfa1.getStates());
        result.addNFAStates(nfa2.getStates());
        result.addTransition(startState, 'e', nfa1.getStartState().getName());
        result.addTransition(startState, 'e', nfa2.getStartState().getName());
        result.addAbc(nfa1.getABC());
        result.addAbc(nfa2.getABC());

        return result;

    }


    /**
     * build a term(NFA)  based on factor
     *
     * @return the term( empty or multiple factors) that is already in the form of an NFA
     */
    private NFA term() {
        NFA startFactor = new NFA();
        while (more() && peek() != ')' && peek() != '|') {
            NFA newFactor = factor();
            //If a term is just an empty sequence of factors
            if (startFactor.getStates().isEmpty()) {
                startFactor = newFactor;
            } else {//concatentae the term if there are multple factor
                startFactor = combine(startFactor, newFactor);
            }
        }
        return startFactor;
    }

    /**
     * combine two nfas (with order)
     * @param nfa1 - base nfa
     * @param nfa2 - nfa added to nfa1
     * @return combined nfa
     */
    private NFA combine(NFA nfa1, NFA nfa2) {
        // must use a set for multiple final states
        Set<State> nfa1FinalStates = nfa1.getFinalStates();
        //add states from nfa2 to base
        nfa1.addNFAStates(nfa2.getStates());
        nfa1.addAbc(nfa2.getABC());

        Iterator<State> itr = nfa1FinalStates.iterator();
        while (itr.hasNext()) {
            State state = itr.next();
             //states are now non-final nfa states
            ((NFAState) state).setNonFinal();
            //empty transition used to rach start of nfa2
            nfa1.addTransition(state.getName(), 'e', nfa2.getStartState().getName());
        }
        return nfa1;
    }

    /**
     * Builds factor from a base, but checks for repitition if needed
     * @return an nfa built from a base
     */
    private NFA factor() {
        NFA base = base();
        //if the regex is longer and the next char is star operator
        while (more() && peek() == '*') {
            eat('*');
            base = star(base);
        }
        return base;
    }

    /**
     * Creates a case accepting the star operator if needed
     * @param base NFA to build on
     * @return NFA that now includes a star operator
     */
    private NFA star(NFA base) {
        // new simple nfa to be returned
        NFA starBase = new NFA();
        NFAState startS = new NFAState(String.valueOf(stateInc++));
        NFAState endS = new NFAState(String.valueOf(stateInc++));
        starBase.addStartState(startS.getName());
        starBase.addFinalState(endS.getName());
        //Add all states from root to starBase
        starBase.addNFAStates(base.getStates());
        //transitions need to be empty because of star operator
        starBase.addTransition(startS.getName(), 'e', endS.getName());
        starBase.addTransition(endS.getName(), 'e', base.getStartState().getName());
        //new starting state should connect to base
        starBase.addTransition(startS.getName(), 'e', base.getStartState().getName());
        starBase.addAbc(base.getABC());

        Iterator<State> itr = base.getFinalStates().iterator();
        while (itr.hasNext()) {
            State state = itr.next();
            starBase.addTransition(state.getName(), 'e', endS.getName());
            Iterator<State> itr2 = starBase.getFinalStates().iterator();
            while (itr2.hasNext()) {
                State state2 = itr2.next();
                if (state2.getName().equals(state.getName())) {
                    ((NFAState) state2).setNonFinal();
                }
            }
        }
        return starBase;
    }

    /**
     * Base can be a character or a regex
     * @return nfa made from char or regex base
     */
    private NFA base() {
        //check parenthesis case for base
        switch (peek()) {
            case '(':
                eat('(');
                NFA reg = regEx();
                eat(')');
                return reg;
            //if not build a base, simple 2 state nfa
            default:
            NFA defNfa = new NFA();
            //fill out the 5 tuple
            NFAState startS = new NFAState(String.valueOf(stateInc++));
            NFAState endS = new NFAState(String.valueOf(stateInc++));  
            char nextChar = next();
            defNfa.addStartState(startS.getName());
            defNfa.addFinalState(endS.getName());
            defNfa.addTransition(startS.getName(), nextChar, endS.getName());
            Set<Character> alphabet = new LinkedHashSet<Character>();
            alphabet.add(nextChar);
            defNfa.addAbc(alphabet);
            return defNfa;
        }
    }

    /**
     * just peek at the next char, no action or removal
     * @return The next char in the regex
     */
    private char peek() {
        return regEx.charAt(0);
    }

    /**
     * process char and then return it (removing it as well)
     * @param c Char to process
     */
    private void eat(char c) {
        if (peek() == c) {
            this.regEx = this.regEx.substring(1);
        } else {
            System.err.println("Peek() expected a char but ran into an error");
        }
    }

    /**
     * remove the next char and return it
     * @return the next char
     */
    private char next() {
        char c = peek();
        eat(c);
        return c;
    }

    /**
     * Helper method to determine length of regex
     * @return boolean if regex has more characters
     */
    private boolean more() {
        return regEx.length() > 0;
    }


}