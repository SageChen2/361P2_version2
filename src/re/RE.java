package re;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

/**
 * This class constructs an NFA for a given regular expression
 *
 * @author Zixiao Chen, Daniel McDougall
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
     * Looks at the regex to determine what it needs to build
     *
     * @return parsed portion of the regex in the form of an NFA
     */
    private NFA term() {
        NFA factor = new NFA();

        while (more() && peek() != ')' && peek() != '|') {
            NFA fact = factor();

            //If RE tries to process regex and there no operations to perform, just return simplest NFA
            if (factor.getStates().isEmpty()) {
                factor = fact;
                //If there are multiple terms following each other, perform concatenate operation on the NFAs
            } else {
                factor = concat(factor, fact);

            }
        }

        return factor;
    }

    /**
     * Concatenates the two NFAs, when two NFAs are in the regex together
     *
     * @param reg1 - the NFA we are adding reg2 onto
     * @param reg2 - additional NFA that follows reg1
     * @return NFA that has been concatenated
     */
    private NFA concat(NFA reg1, NFA reg2) {
        //Get final states and name of start state in second NFA
        String reg2Start = reg2.getStartState().getName();
        Set<State> reg1Finals = reg1.getFinalStates();

        //Add all states from second NFA to first NFA
        reg1.addNFAStates(reg2.getStates());

        //Make sure both alphabets are included
        reg1.addAbc(reg2.getABC());

        Iterator<State> itr = reg1Finals.iterator();
        while (itr.hasNext()) {
            State state = itr.next(); // might be wrong
            ((NFAState) state).setNonFinal(); //cast all the State objects into NFAState and set them to be non-final
            reg1.addTransition(state.getName(), 'e', reg2Start);//from the 'non-final' states, use an empty string e, to transit to the start state of nfa2

        }


        return reg1;
    }

    /**
     * A factor is a base followed by a possibly empty sequence of '*'.
     *
     * @return root NFA or root with the star operator if the regex has a '*'
     */
    private NFA factor() {
        NFA root = base();

        //If star op is needed, descend into recursion
        while (more() && peek() == '*') {
            eat('*');
            root = star(root);
        }
        return root;
    }

    /**
     * Handles the star operator
     *
     * @param base - NFA we are building upon
     * @return NFA - the previous NFA has now incorporated the star operator
     */
    private NFA star(NFA base) {

        NFA retNFA = new NFA();

        NFAState startS = new NFAState(String.valueOf(stateInc++));
        NFAState endS = new NFAState(String.valueOf(stateInc++));

        //add new start state
        retNFA.addStartState(startS.getName());

        //add new final state
        retNFA.addFinalState(endS.getName());

        //Add all states from root to new NFA
        retNFA.addNFAStates(base.getStates());

        //Add empty transitions because star allows for 0 occurrences of term/NFA
        retNFA.addTransition(startS.getName(), 'e', endS.getName());
        retNFA.addTransition(endS.getName(), 'e', base.getStartState().getName());

        //Tie new start to root NFA
        retNFA.addTransition(startS.getName(), 'e', base.getStartState().getName());

        //Make sure old alphabet is included
        retNFA.addAbc(base.getABC());


        Iterator<State> itr = base.getFinalStates().iterator();
        while (itr.hasNext()) {
            State state = itr.next();
            retNFA.addTransition(state.getName(), 'e', endS.getName());

            Iterator<State> itr2 = retNFA.getFinalStates().iterator();
            while (itr2.hasNext()) {
                State state2 = itr2.next();
                if (state2.getName().equals(state.getName())) {
                    ((NFAState) state2).setNonFinal();
                }

            }


        }


        return retNFA;
    }

    /**
     * Root is a character, an escaped character, or a parenthesized regular expression.
     *
     * @return an NFA built from the next symbol or within the parenthesis
     */
    private NFA base() {
        //Check if next symbol requires changing precedent using '()'
        switch (peek()) {
            case '(':
                eat('(');
                NFA reg = regEx();
                eat(')');
                return reg;
            default:
            NFA nfa = new NFA();

            //Make a new simple NFA with 2 states and a transition on char c
            NFAState startS = new NFAState(String.valueOf(stateInc++));
    
            NFAState endS = new NFAState(String.valueOf(stateInc++));
    
            nfa.addStartState(startS.getName());
            nfa.addFinalState(endS.getName());
            //get the next char to make a transition, but only call next() once
            char nextChar = next();
            nfa.addTransition(startS.getName(), nextChar, endS.getName());
    
            Set<Character> alphabet = new LinkedHashSet<Character>();
            alphabet.add(nextChar);
            nfa.addAbc(alphabet);
            return nfa;
        }
    }

    /**
     * Peeks at the first index of the regex
     *
     * @return The next unprocessed character in the regex
     */
    private char peek() {
        return regEx.charAt(0);
    }

    /**
     * Processes the character and removes from the regex
     *
     * @param c Character to process
     */
    private void eat(char c) {
        if (peek() == c) {
            this.regEx = this.regEx.substring(1);
        } else {
            throw new RuntimeException("Received: " + peek() + "\n" + "Expected: " + c);
        }
    }

    /**
     * Moves the processed character, removing the character from the regex and returning it
     *
     * @return the character that was processed
     */
    private char next() {
        char c = peek();
        eat(c);
        return c;
    }

    /**
     * Evaluates if there are more characters to assess in the regex
     *
     * @return boolean
     */
    private boolean more() {
        return regEx.length() > 0;
    }


}