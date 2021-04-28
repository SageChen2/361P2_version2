package re;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface {
    String regEx; // regular expression
    int stateInc = 0;

    /**
     * constructor of the RE, this is the RegEx Parser
     **/
    public RE(String RegEx) {
        this.regEx = RegEx;
        stateInc = 0;
    }

    /**
     * @return The NFA that is built by a given regular expression
     */
    @Override
    public NFA getNFA() {
        NFA noUnion = term();

        if(more() && peek() == '|'){
            eat('|');
            NFA regEx = new NFA();
            NFA retUnion = union(noUnion, regEx);
            return retUnion;
        }
        else{
            return noUnion;
        }
    }

    /*
     * Generate an NFA from a regular expression
     *
     */
    private NFA regEx() {

        NFA term = term();//parse at least one term

        if (more() && peek() == '|') {

            eat('|');
            NFA regex = regEx(); //after '|', parse another term
            //concatenate 2 NFAs




        } else {
            return term;
        }


        return null; // place holder, delete later
    }

    /**
     * returns the next item of input without consuming it
     *
     * @return
     ***/
    private char peek() {
        return regEx.charAt(0);

    }

    /**
     * returns the next item of input and consumes it
     */
    private char next() {
        char c = peek();
        eat(c);

        return 0;// placer holder
    }

    /**
     * consumes the next item of input, failing if not equal to input item(char c).
     */
    private void eat(char c) {
        if (peek() == c) { // peak() returns the next item of input without consuming it
            this.regEx = this.regEx.substring(1);// the item at index 0 is consumed
        } else {
            throw new RuntimeException("Expected: " + c + "; got: " + peek());
        }
    }

    /**
     * checks if there is more input available.
     *
     * @return true if the length of regular expression is greater than 0
     **/
    private boolean more() {
        return regEx.length() > 0;
    }


    /**
     *  combine 2 NFAs into 1 NFA (the order does not matter)
     *
     * @return the combined NFA
     * */
    private NFA union(NFA nfa1, NFA nfa2){
            NFA result = new NFA();

            ///start state(might be wrong)
            //NFAState startState = new NFAState(String.valueOf(stateInc));

            String startState = String.valueOf(stateInc++);//might be wrong
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
     * @return an NFA
     */
    private NFA term() { //A term is a possibly empty sequence of factors.
        NFA factor = new NFA(); // instantiate a new NFA that is empty

        // while it has not reached the boundary of a term or the end of the input: (for example "term | term")
        while (more() && peek() != ')' && peek() != '|') {
            NFA nextFactor = factor();

            //if NFA has no states
            if (factor.getStates().isEmpty()) {
                factor = nextFactor;
            } else {//if there is more than one term in the NFA.

                factor = concat(factor, nextFactor);

            }


        }
        //
        return factor;

    }

    /**
     * to concatenate 2 NFAs (A regEx is a term and it is also an NFA), they are 2 terms seperated by '|'
     * the order of concatenation is to add the nfa2 onto the nfa1(order matters)
     * @return combined NFA of the first 2 NFAs
     */
    private NFA concat(NFA nfa1, NFA nfa2) {
        //concatenation of NFA requires to 'link' the final states of the 1st NFA to the start state pf the 2nd NFA
       // Set<State> finalStatesOfNfa1 = nfa1.getFinalStates(); // can be simplified
        String startStateOfNfa2 = nfa1.getStartState().getName(); // there is only 1 start state

        //need to combine alphabet, states and transitions

        //combine all the states from nfa2 to nfa1 (the order might cause problem)
        nfa1.addNFAStates(nfa2.getStates());

        //combine the alphabets of nfa1 and nfa2 and store in the alphabet of nfa1
        nfa1.addAbc(nfa2.getABC());

        //iterate through all the final states of nfa1 and set them to be non-final states
        //also add nfa1's transitions to the start of nfa2
        Iterator<State> itr = nfa1.getFinalStates().iterator();
        while (itr.hasNext()) {
            State state = itr.next(); // might be wrong
            ((NFAState) state).setNonFinal(); //cast all the State objects into NFAState and set them to be non-final
            nfa1.addTransition(state.getName(), 'e', startStateOfNfa2);//from the 'non-final' states, use an empty string e, to transit to the start state of nfa2

        }


        return nfa1;
    }


    /**
     *
     */
    private NFA factor() {

        NFA base = base();

        //A factor is a base followed by a possibly empty sequence of '*'.
        //while there is more input and the next item of input is a *
        while (more() && peek() == '*') {
            eat('*');

            Set<State> setOfStates = base.getFinalStates(); //there exist multiple end states


            //transition from an end state back to the start state
            Iterator<State> itr = setOfStates.iterator();
            while (itr.hasNext()) {
                base.addTransition(itr.next().getName(), 'e', base.getStartState().getName());// might be wrong
            }
            NFAState starter = new NFAState(String.valueOf(stateInc++));    // create a new starter state
            base.addState(starter.getName());   // add created state
            base.addTransition(starter.getName(), 'e', base.getStartState().getName());    // new state needs transition
            base.addStartState(starter.getName());  // set new starter to the start state
            base.addFinalState(starter.getName());
            // sequence is possibly empty, so new state also added to final states
            //double check last few lines\
        }

        return base;
    }

    /**
     *
     */
    private NFA base() {
        //A base is a character, an escaped character, or a parenthesized regular expression.
        NFA base = new NFA();

        if (peek() == '(') {
            eat('(');
            base = regEx();// might be wrong
            eat(')');

        } else {
            NFAState startS = new NFAState(String.valueOf(stateInc++));
            NFAState endS = new NFAState(String.valueOf(stateInc++));
            base.addStartState(startS.getName());
            base.addFinalState(endS.getName());
            base.addTransition(startS.getName(), next(), endS.getName());

        }
        return base;
    }

}
