package com.inqbarna.adapters;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 23/02/2017
 */
public interface ItemBinder {
    void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos);
}
