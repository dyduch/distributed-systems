package com.edu.agh.ds;


import com.edu.agh.ds.user.impl.Doctor;
import com.edu.agh.ds.user.impl.Technician;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Enter type of service");
        String typeOfService = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if(typeOfService.equalsIgnoreCase("doc")) {
            new Doctor().run();
        } else if(typeOfService.equalsIgnoreCase("tech")){
            new Technician().run();
        }
    }
}