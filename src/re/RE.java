package re;

import java.util.LinkedHashSet;
import java.util.Set;
import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface{
    String regEx;


    //constructor of RE
    public RE(String RegEx){
        this.regEx = RegEx;

    }

    @Override
    public NFA getNFA() {
        return null;
    }

    /*
    * Generate an NFA from a regular expression
    *
    * */
    private NFA regEx(){




        return null; //place holder, delete later
    }


    /**
     *
     *
     * ***/
    private char peek(){
            return regEx.charAt(0);

    }

}
