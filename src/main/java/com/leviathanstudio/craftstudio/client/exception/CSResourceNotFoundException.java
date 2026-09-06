package com.leviathanstudio.craftstudio.client.exception;

 



public class CSResourceNotFoundException extends RuntimeException
{
    public CSResourceNotFoundException(String resource) {
        super("CraftStudio resource not found: " + resource);
    }
}
