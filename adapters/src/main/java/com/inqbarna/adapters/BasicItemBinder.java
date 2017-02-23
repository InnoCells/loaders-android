package com.inqbarna.adapters;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 16/9/16
 */
public class BasicItemBinder implements ItemBinder {
    private final int mModelVar;

    public BasicItemBinder(int modelVar) {
        mModelVar = modelVar;
    }

    @Override
    public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
        variableBinding.bindValue(mModelVar, dataAtPos);
    }
}
