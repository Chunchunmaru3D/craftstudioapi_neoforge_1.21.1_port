package com.leviathanstudio.craftstudio.client.exception;

 



public class CSMalformedJsonException extends RuntimeException
{
    public CSMalformedJsonException(String fieldName, String expectedType, String resource) {
        super("Malformed CraftStudio json: expected field '" + fieldName + "' of type " + expectedType + " in " + resource);
    }

    public CSMalformedJsonException(String blockName, String resource) {
        super("Malformed CraftStudio json: could not read block '" + blockName + "' in " + resource);
    }
}
