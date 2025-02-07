/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.osmaldym.jvr.tools;

/**
 *
 * @author Osmaldy Maldonado
 */
public class Tools {
    /**
     * Converts array to entries like 
     * {@code [[key, value], [key, value], [key, value]] }
     * @param arr
     * @return Array of arrays
     */
    public static String[][] convertToEntries(String[] arr) {
        int finalArrLength = (int) Math.ceil(arr.length/2);
        String[][] finalArr = new String[finalArrLength][2];
        int i = 0, keyPos = 0, valuePos = 1;

        while (i < finalArrLength) {
          String[] dataToAdd = { arr[keyPos], (valuePos >= arr.length ? null : arr[valuePos]) };
          finalArr[i] = dataToAdd;

          valuePos = valuePos+2;
          keyPos = keyPos+2;
          i++;
        }

        return finalArr;
    }
}
