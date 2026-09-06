package com.leviathanstudio.craftstudio.client.exception;

 





public class CSResourceNotRegisteredException extends RuntimeException
{
    public CSResourceNotRegisteredException(String resource) {
        super("CraftStudio resource not registered (not loaded by CSModelRegistry yet): " + resource);
    }
}
