package edu.handong.csee.se.sugarbag.app.action;

import edu.handong.csee.se.sugarbag.app.SugarBag;

public class GoBackAction extends Action {
    
    public GoBackAction(SugarBag sugarBag) {
        super(sugarBag);
    }

    @Override
    public boolean act() {
        sugarBag.previous();
        sugarBag.show(sugarBag.loadPlugins(), sugarBag.loadSelected());
        
        return true;
    }
}
