package re;

import java.util.LinkedHashSet;
import java.util.Set;

import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface {
    String regEx; //regular expression


    /**
     * constructor of the RE, this is the RegEx Parser
     **/
    public RE(String RegEx) {
        this.regEx = RegEx;

    }


    /**
     * @return The NFA that is built by a given regular expression
     */
    @Override
    public NFA getNFA() {
        return regEx();
    }

    /*
     * Generate an NFA from a regular expression
     *
     * */
    private NFA regEx() {


        return null; //place holder, delete later
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

        return 0;//placer holder
    }

    /**
     * consumes the next item of input, failing if not equal to input item(char c).
     */
    private void eat(char c) {
        if (peek() == c) { //peak() returns the next item of input without consuming it
            this.regEx = this.regEx.substring(1);//the item at index 0 is consumed
        } else {
            throw new
                    RuntimeException("Expected: " + c + "; got: " + peek());
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
     * @return an NFA
     */
    private NFA term() {
        NFA factor = new NFA(); //instantiate a new NFA that is empty

        //while it has not reached the boundary of a term or the end of the input:
        while (more() && peek() != ')' && peek() != '|') {
            NFA nextFactor = factor();
//            factor = new Sequence(factor,nextFactor) ;
        }
//
        return factor;

    }


    /**
     *
     */
    private NFA factor() {

//        RegEx base = base() ;
//
//        while (more() && peek() == '*') {
//            eat('*') ;
//            base = new Repetition(base) ;
//        }
//
        return null;
    }

    /**
     *
     */
    private NFA base() {
        NFA base = new NFA();

        if(peek() == '('){
                eat('(') ;
                base = regEx();//might be wrong
                eat(')') ;

        }else{
            //NFAState startState = new NFAState();
            //NFAState endState =   new NFAState();


        }


        return base;
    }


}
