package com.inqbarna.libsamples.vm;

import com.inqbarna.adapters.TypeMarker;
import com.inqbarna.libsamples.R;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 24/03/2018
 */
public class TestVM implements TypeMarker {
    public final String value;

    public TestVM(int idx) {
        value = "Cell number: " + idx;
    }

    @Override
    public int getItemType() {
        return R.layout.main_test_item;
    }

    @Override
    public String toString() {
        return value;
    }
}
