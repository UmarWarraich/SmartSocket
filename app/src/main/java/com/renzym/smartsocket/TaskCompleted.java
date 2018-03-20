package com.renzym.smartsocket;

/**
 * Created by Hammad on 1/27/2018.
 */

public interface TaskCompleted {
    // Define data you like to return from AysncTask
    public void onTaskComplete(String result);
}
