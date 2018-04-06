package com.inqbarna.adapters;

import java.util.List;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 03/04/2018
 */
interface UpdateLogger {
    void debugMessage(String format, Object... args);
    void dbgPrintList(List<?> list, String title, String symbol);
}
