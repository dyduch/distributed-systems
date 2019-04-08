package com.edu.agh.ds.user;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface User {

    public void run() throws IOException;

    public void setup();
}
