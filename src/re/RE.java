package re;

import fa.nfa.NFA;

public class RE implements REInterface{
    String RegEx;
    public RE(String RegEx){
        this.RegEx = RegEx;

    }

    @Override
    public NFA getNFA() {
        return null;
    }
}
