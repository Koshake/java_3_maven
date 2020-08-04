package main;

import java.sql.SQLOutput;
import java.util.Arrays;

public class MainApp {

    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6};
        System.out.println(Arrays.toString(getNewArray(array)));

        int[] array2 = {4, 4 , 4, 4};
        System.out.println(testArray(array2));
    }

    public static int[] getNewArray(int[] arr) throws RuntimeException {
        int lastIndex = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 4) {
                lastIndex = i;
            }
        }
        if (lastIndex == -1) {
            throw new RuntimeException("Черверок в массиве нет!");
        }

        return Arrays.copyOfRange(arr, lastIndex + 1, arr.length);
    }

    public static boolean testArray(int[] arr) {
        boolean hasFour = false;
        boolean hasOne = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 4) {
                hasFour = true;
            }
            else if (arr[i] == 1) {
                hasOne = true;
            }
            else {
                return false;
            }
        }
        return (hasFour && hasOne);
    }
}
